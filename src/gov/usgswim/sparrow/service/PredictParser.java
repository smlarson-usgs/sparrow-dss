package gov.usgswim.sparrow.service;

import gov.usgswim.service.AbstractHttpRequestParser;

import gov.usgswim.service.RequestParser;

import gov.usgswim.sparrow.Adjustment;
import gov.usgswim.sparrow.AdjustmentSet;
import gov.usgswim.sparrow.AdjustmentSetBuilder;
import gov.usgswim.sparrow.PredictRequest;

import java.awt.Point;
import java.awt.geom.Point2D;
import gov.usgswim.sparrow.Adjustment.AdjustmentType;

import javax.servlet.http.HttpServletRequest;

import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

public class PredictParser extends AbstractHttpRequestParser<PredictServiceRequest> implements RequestParser<PredictServiceRequest> {

	public PredictParser() {
	}

	public PredictServiceRequest parse(HttpServletRequest request) throws Exception {
		XMLStreamReader reader = getXMLStream(request);
		return parse(reader);
	}

	public PredictServiceRequest parse(String in) throws Exception {
		XMLStreamReader reader = getXMLStream(in);
		return parse(reader);
	}
	
	public PredictServiceRequest parse(XMLStreamReader reader) throws Exception {
		PredictServiceRequest req = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("predict".equals(lName)) {
					parsePredict(reader, req);
				} else if ("response-options".equals(lName)) {
					parseOptions(reader, req);
				} else if ("sparrow-prediction-request".equals(lName)) {
					req = new PredictServiceRequest();
				}
				
				
				break;
			}
		}
		
		return req;
	}
	
	/**
	 * Assumes the reader is looking at a 'predict' element
	 * 
	 * @param reader
	 * @param req
	 * @throws Exception
	 */
	protected void parsePredict(XMLStreamReader reader, PredictServiceRequest req) throws Exception {
		Long modelId = Long.parseLong(reader.getAttributeValue(null, "model-id"));	//require attrib
		AdjustmentSet adjSet = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("raw-value".equals(lName)) {
						req.setPredictType(PredictServiceRequest.PredictType.VALUES);
					} else if ("change-from-nominal".equals(lName)) {
						if (reader.getAttributeValue(null, "type") != null) {
							req.setPredictType(PredictServiceRequest.PredictType.find(reader.getAttributeValue(null, "type"))
							);
						} else {
							req.setPredictType(PredictServiceRequest.PredictType.PERC_CHG_FROM_NOMINAL);
						}
						
					} else if ("source-adjustments".equals(lName)) {
						adjSet = parseAdjustmentsSection(reader);
						
					}
					
				}
				break;
			
			case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if ("predict".equals(lName)) {
						PredictRequest pr = new PredictRequest(modelId, adjSet);
						req.setPredictRequest(pr);
						return;
					}
				}
				break;
			}
		}
	}
	
	
	/**
	 * Reads just the 'response-options' portion of the request and then returns.
	 * 
	 * @param reader
	 * @param req
	 * @throws XMLStreamException
	 */
	protected void parseOptions(XMLStreamReader reader, PredictServiceRequest req) throws Exception {
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("result-filter".equals(lName)) {
						parseFilterSection(reader, req);
					} else if ("data-series".equals(lName)) {
						//This would be nested inside a 'result-content' element
						req.setDataSeries(PredictServiceRequest.DataSeries.find(StringUtils.trimToEmpty(reader.getElementText()))
						);
					} 
					
				}
				break;
			
			case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if ("response-options".equals(lName)) {
						return;
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Expecting 'result-filter' element
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
					PredictServiceRequest.ResponseFilter filter = gov.usgswim.sparrow.service.PredictServiceRequest.ResponseFilter.find(lName);
					
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
					if ("result-filter".equals(lName)) {
						return;
					}
				}
				break;
			}
		}
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
