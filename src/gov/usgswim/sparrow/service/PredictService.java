package gov.usgswim.sparrow.service;

import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.ThreadSafe;
import gov.usgswim.sparrow.Adjustment;
import gov.usgswim.sparrow.Adjustment.AdjustmentType;
import gov.usgswim.sparrow.AdjustmentSet;
import gov.usgswim.sparrow.AdjustmentSetBuilder;
import gov.usgswim.sparrow.AdjustmentSetImm;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DPercentCompare;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.PredictionRequest;

import java.awt.Point;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * A service that accepts requests for SPARROW predictions and returns an
 * xml document containing the predictions.
 *
 * In order to support local requests on the map server, this service also
 * has a runPrediction() method that returns raw data in a Data2D format,
 * so that no inter-server communication is needed if the service is running
 * within the MapViewer server.  (similar to the EJB local interface)
 */
@ThreadSafe
public class PredictService implements HttpServiceHandler,
			RequestParser<PredictServiceRequest>, HttpRequestHandler<PredictServiceRequest> {
			
			
	protected static Logger log =
		Logger.getLogger(PredictService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
		
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	
	
	public PredictService() {
	}
	



	public void dispatch(XMLStreamReader in,
											 HttpServletResponse response) throws Exception {
											 
		PredictServiceRequest req = parse(in);
		dispatch(req, response);
	}

	public void dispatch(XMLStreamReader in, OutputStream out) throws Exception {
																							
		PredictServiceRequest req = parse(in);
		dispatch(req, out);
	}
	
	public void dispatch(PredictServiceRequest req, HttpServletResponse response) throws Exception {
		response.setContentType(RESPONSE_MIME_TYPE);
		dispatch(req, response.getOutputStream());
	}
	
	/**
	 * This is really a test method and not part of any of the standard interfaces.
	 * 
	 * It provides direct access to the raw prediction data for an imcoming xml
	 * request, w/o requiring the data to be serialized back to XML.
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public Data2D dispatch(XMLStreamReader in) throws Exception {
		PredictServiceRequest req = parse(in);
		return runPrediction(req);
	}
	
	public void dispatch(PredictServiceRequest req, OutputStream outStream) throws Exception {
																																 
		synchronized (factoryLock) {
			if (xoFact == null) {
				xoFact = WstxOutputFactory.newInstance();
			}
		}
		
		Data2D result = runPrediction(req);

		PredictionSerializer ps = new PredictionSerializer();
		ps.writeResponse(outStream, req, result);

	}
	
	public Data2D runPrediction(PredictServiceRequest req) {
		Data2D result = null;		//The prediction result
		
		Long modelId = req.getPredictionRequest().getModelId();
		long startTime = System.currentTimeMillis();	//Time started

		try {

			Data2D adjResult = SharedApplication.getInstance().getPredictResultCache().compute(req.getPredictionRequest());
			
			if (req.getPredictType().isComparison()) {
				//need to run the base prediction and the adjusted prediction
				AdjustmentSetImm noAdj = new AdjustmentSetImm();
				PredictionRequest noAdjRequest = new PredictionRequest(modelId, noAdj);
				Data2D noAdjResult = SharedApplication.getInstance().getPredictResultCache().compute(noAdjRequest);
	
				result = new Data2DPercentCompare(
						noAdjResult, adjResult,
						req.getPredictType().equals(PredictServiceRequest.PredictType.DEC_CHG_FROM_NOMINAL));
						
			} else {
				//need to run only the adjusted prediction
				result = adjResult;
			}
			
			log.debug("Predict service done for model #" + modelId + " (" + result.getRowCount() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");
			
			if (req.getDataSeries() == PredictServiceRequest.DataSeries.ALL) {
				//return all results
				return result;
			} else if (req.getDataSeries().getAggColumnIndex() > -1){
				//Return only the requested series
				int firstAggCol = result.getColCount() - 2;
				int dataCol = firstAggCol + req.getDataSeries().getAggColumnIndex();
				return new Data2DView(result, dataCol, 1);
			} else {
				//should handle these special - for now return all data
				return result;
			}
			
		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer, so throwing a runtime exception.", e);
			throw new RuntimeException(e);
		}
		

	}
	
	public PredictServiceRequest parse(XMLStreamReader reader) throws Exception {
		PredictServiceRequest req = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("value-prediction".equals(lName)) {
					req.setPredictType(gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.VALUES);
					PredictionRequest pr = parsePredictSection(reader);
					req.setPredictionRequest(pr);
				} else if ("change-from-nominal".equals(lName)) {
					if (reader.getAttributeValue(null, "type") != null) {
						req.setPredictType(
							PredictServiceRequest.PredictType.find(reader.getAttributeValue(null, "type"))
						);
					} else {
						req.setPredictType(PredictServiceRequest.PredictType.PERC_CHG_FROM_NOMINAL);
					}
					
					PredictionRequest pr = parsePredictSection(reader);
					req.setPredictionRequest(pr);
				} else if ("response".equals(lName)) {
					parseResponseSection(reader, req);
				} else if ("sparrow-prediction-request".equals(lName)) {
					req = new PredictServiceRequest();
				}
				
				
				break;
			}
		}
		
		return req;
	}
	
	/**
	 * Reads just the response portion of the request and then returns.
	 * 
	 * @param reader
	 * @param req
	 * @throws XMLStreamException
	 */
	protected void parseResponseSection(XMLStreamReader reader, PredictServiceRequest req) throws Exception {
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("filter".equals(lName)) {
						parseFilterSection(reader, req);
					} else if ("data-series".equals(lName)) {
						req.setDataSeries(PredictServiceRequest.DataSeries.find(StringUtils.trimToEmpty(reader.getElementText()))
						);
					} 
					
				}
				break;
			
			case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if ("response".equals(lName)) {
						return;
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Reads just the response/filter portion of the request and then returns.
	 * 
	 * @param reader
	 * @param request
	 * @throws XMLStreamException
	 */
	protected void parseFilterSection(XMLStreamReader reader, PredictServiceRequest req) throws Exception {
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					PredictServiceRequest.ResponseFilter filter = PredictServiceRequest.ResponseFilter.find(lName);
					
					switch (filter) {
					case ALL:
						req.setResponseType(filter);
						break;
					case NEAR_POINT:
						req.setResponseType(filter);
						Integer n = parseAttribAsInt(reader, "number-of-results", false);
						if (n != null) {
							req.setNumberOfResults(n);
						}
						
						reader.nextTag();	//MUST be a point element
						
						Point.Double pt = new Point.Double();
						pt.x = parseAttribAsDouble(reader, "long");
						pt.y = parseAttribAsDouble(reader, "lat");
						req.setFilterPoint(pt);
						
						break;
					default:
						throw new Exception("Could not parse filter type '" + lName + "'");
					
					} 
					
				}
				break;
			
			case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if ("filter".equals(lName)) {
						return;
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Reads just the prediction portion of the request and then returns.
	 * 
	 * @param reader
	 * @param request
	 * @throws XMLStreamException
	 */
	protected PredictionRequest parsePredictSection(XMLStreamReader reader) throws Exception {
		Long modelId = null;
		AdjustmentSet adjSet = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("model-id".equals(lName)) {
						modelId = Long.parseLong(reader.getElementText());
					} else if ("source-adjustments".equals(lName)) {
						adjSet = parseAdjustmentsSection(reader);
						return new PredictionRequest(modelId, adjSet);
					}
					
				}
				break;
			}
		}
		
		return new PredictionRequest(modelId, adjSet);	//should not get here
	}
	
	/**
	 * Reads just the adjustments portion of the request and then returns.
	 * 
	 * @param reader
	 * @throws XMLStreamException
	 */
	protected AdjustmentSet parseAdjustmentsSection(XMLStreamReader reader) throws Exception {
		AdjustmentSetBuilder adj = new AdjustmentSetBuilder();
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					AdjustmentType type = AdjustmentType.find(lName);

					switch (type) {
					case GROSS_SRC_ADJUST: {
						int src = parseAttribAsInt(reader, "src");
						double coef = parseAttribAsDouble(reader, "coef");
						adj.addAdjustment(new Adjustment(AdjustmentType.GROSS_SRC_ADJUST, src, coef));
						break;
					}
					case SPECIFIC_ADJUST: {
						int src = parseAttribAsInt(reader, "src");
						int reach = parseAttribAsInt(reader, "reach");
						double val = parseAttribAsDouble(reader, "value");
						adj.addAdjustment(new Adjustment(AdjustmentType.SPECIFIC_ADJUST, src, reach, val));
						break;
					}
					default:
						throw new Exception("Unsupported adjustment type");
					}
					
				}
				break;
				
				case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if (lName.equals("source-adjustments")) {
						return adj.getImmutable();
					}
					
				}
			}
		}
		
		return adj.getImmutable();	//shouldn't get here
	}
	
	//TODO:  These util methods could easily be part of a parse util class.
	
	/**
	 * Returns the integer value found in the specified attribute of the current
	 * element.  If require is true and the attribute does not exist, an error
	 * is thrown.
	 * @param reader
	 * @param attrib
	 * @return
	 * @throws Exception
	 */
	public static int parseAttribAsInt(
			XMLStreamReader reader, String attrib) throws Exception {
			
		return parseAttribAsInt(reader, attrib, true);
	}
	
	/**
	 * Returns the Integer value found in the specified attribute of the current
	 * element.  If require is true and the attribute does not exist, an error
	 * is thrown.  If the attribute does not exist or is empty and require is
	 * not true, null is returned.
	 * @param reader
	 * @param attrib
	 * @param require
	 * @return
	 * @throws Exception
	 */
	public static Integer parseAttribAsInt(
			XMLStreamReader reader, String attrib, boolean require) throws Exception {
		
		String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );
		
		if (v != null) {
			int iv = 0;
			
			try {
				return Integer.parseInt(v);
			} catch (Exception e) {
				throw new Exception("The '" + attrib + "' attribute for element '" + reader.getLocalName() + "' must be an integer");
			}
			
		} else if (require) {
			throw new Exception("The '" + attrib + "' attribute must exist for element '" + reader.getLocalName() + "'");
		} else {
			return null;
		}
		
	}
	
	
	/**
	 * Returns the double value found in the specified attribute of the current
	 * element.  If the attribute does not exist or cannot be parsed as a number,
	 * an error is thrown.
	 * 
	 * @param reader
	 * @param attrib
	 * @return
	 * @throws Exception
	 */
	public static double parseAttribAsDouble(
			XMLStreamReader reader, String attrib) throws Exception {
			
		return parseAttribAsDouble(reader, attrib, true);
	}
	/**
	 * Returns the Double value found in the specified attribute of the current
	 * element.  If require is true and the attribute does not exist, an error
	 * is thrown.  If the attribute does not exist or is empty and
	 * require is not true, null is returned.
	 * @param reader
	 * @param attrib
	 * @param require
	 * @return
	 * @throws Exception
	 */
	public static Double parseAttribAsDouble(
			XMLStreamReader reader, String attrib, boolean require) throws Exception {
		
		String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );
		
		if (v != null) {
			int iv = 0;
			
			try {
				return Double.parseDouble(v);
			} catch (Exception e) {
				throw new Exception("The '" + attrib + "' attribute for element '" + reader.getLocalName() + "' must be a number");
			}
			
		} else if (require) {
			throw new Exception("The '" + attrib + "' attribute must exist for element '" + reader.getLocalName() + "'");
		} else {
			return null;
		}
		
	}

}
