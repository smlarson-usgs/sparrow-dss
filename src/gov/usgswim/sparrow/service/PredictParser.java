package gov.usgswim.sparrow.service;

import gov.usgswim.service.AbstractHttpRequestParser;

import gov.usgswim.service.RequestParser;

import gov.usgswim.sparrow.Adjustment;
import gov.usgswim.sparrow.AdjustmentSet;
import gov.usgswim.sparrow.AdjustmentSetBuilder;
import gov.usgswim.sparrow.PredictionRequest;

import java.awt.Point;
import java.awt.geom.Point2D;
import gov.usgswim.sparrow.Adjustment.AdjustmentType;

import javax.servlet.http.HttpServletRequest;

import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

public class PredictParser extends AbstractHttpRequestParser<PredictRequest> implements RequestParser<PredictRequest> {

	public PredictParser() {
	}

	public PredictRequest parse(HttpServletRequest request) throws Exception {
		XMLStreamReader reader = getXMLStream(request);
		return parse(reader);
	}

	public PredictRequest parse(String in) throws Exception {
		XMLStreamReader reader = getXMLStream(in);
		return parse(reader);
	}
	
	public PredictRequest parse(XMLStreamReader reader) throws Exception {
		PredictRequest req = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("value-prediction".equals(lName)) {
					req.setPredictType(gov.usgswim.sparrow.service.PredictRequest.PredictType.VALUES);
					PredictionRequest pr = parsePredictSection(reader);
					req.setPredictionRequest(pr);
				} else if ("change-from-nominal".equals(lName)) {
					if (reader.getAttributeValue(null, "type") != null) {
						req.setPredictType(gov.usgswim.sparrow.service.PredictRequest.PredictType.find(reader.getAttributeValue(null, "type"))
						);
					} else {
						req.setPredictType(gov.usgswim.sparrow.service.PredictRequest.PredictType.PERC_CHG_FROM_NOMINAL);
					}
					
					PredictionRequest pr = parsePredictSection(reader);
					req.setPredictionRequest(pr);
				} else if ("response".equals(lName)) {
					parseResponseSection(reader, req);
				} else if ("sparrow-prediction-request".equals(lName)) {
					req = new PredictRequest();
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
	protected void parseResponseSection(XMLStreamReader reader, PredictRequest req) throws Exception {
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("filter".equals(lName)) {
						parseFilterSection(reader, req);
					} else if ("data-series".equals(lName)) {
						req.setDataSeries(gov.usgswim.sparrow.service.PredictRequest.DataSeries.find(StringUtils.trimToEmpty(reader.getElementText()))
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
	protected void parseFilterSection(XMLStreamReader reader, PredictRequest req) throws Exception {
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					PredictRequest.ResponseFilter filter = gov.usgswim.sparrow.service.PredictRequest.ResponseFilter.find(lName);
					
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


}
