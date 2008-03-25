package gov.usgswim.sparrow.service;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.pipeline.PipelineRequest;

import java.awt.Point;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;


public class IDByPointParser extends AbstractHttpRequestParser<IDByPointRequest> {
	public IDByPointParser() {
	}

	public IDByPointRequest parse(HttpServletRequest request) throws Exception {
		if ("GET".equals(request.getMethod())) {
		
			if (request.getParameter(getXmlParam()) != null) {
				return super.parse(request);
			} else {
				String[] paramChain = parseExtraPath(request);
				
				
				if (paramChain.length == 1 && StringUtils.isNumeric(paramChain[0])) {
				
					Long id = Long.parseLong(paramChain[0]);
					int numResults = AbstractHttpRequestParser.parseParamAsLong(request, "result-count", 3L).intValue();
					if (numResults > 100) numResults = 100;
					
					Point.Double point = new Point.Double();	//required
					point.x = parseParamAsDouble(request, "long");
					point.y = parseParamAsDouble(request, "lat");
					
					IDByPointRequest result = new IDByPointRequest(id, point, numResults);
					result.setXMLRequest(""); // no xml request, RESTlike
					return result;
				} else {
					throw new Exception("The IDByPoint Request must contain exactly one argument as part of the URL - the model ID.");
				}
				
			}
			
		} else {
			return super.parse(request);
		}
	}
	
	public IDByPointRequest parse(XMLStreamReader reader) throws Exception {

		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("id-point-request".equals(lName)) {
					return parseMain(reader, null);
				}
				
				
				break;
			}
		}
		
		throw new Exception("No 'id-point-request' element found.");
	}
	
	/**
	 * Reads just the actual id-by-point portion of the xml document.
	 * 
	 * Currently, this is just based on the 'near-point' portion of the predict
	 * request.
	 * 
	 * @param reader
	 * @param modelId Optional:  If the model-id is already know (and not contained
	 *   in the 'id-by-point' portion of the request), it can be passed.
	 * @throws Exception
	 */
	public IDByPointRequest parseMain(XMLStreamReader reader, Long modelId) throws Exception {
		String mainElement = reader.getLocalName();	//save to know when we are complete
		Long id;	//default id may be passed - this is sort of a multi-use parser
		int numResults;
		Point.Double point = new Point.Double();	//required
		
		id = parseAttribAsLong(reader, "model-id", modelId);
		numResults = parseAttribAsInt(reader, "result-count", 3);
		if (numResults > 100) numResults = 100;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				{
					String lName = reader.getLocalName();
					
					if ("point".equals(lName)) {
						point.x = parseAttribAsDouble(reader, "long", true);
						point.y = parseAttribAsDouble(reader, "lat", true);
					} 
					
				}
				break;
			
			case XMLStreamReader.END_ELEMENT:
				{
					String lName = reader.getLocalName();
					if (mainElement.equals(lName)) {
						IDByPointRequest req = new IDByPointRequest(id, point, numResults);
						return req;
					}
				}
				break;
			}
		}
		
		throw new Exception("Badly formed document - no end element found for '" + mainElement + "'");
	}

	public PipelineRequest parseForPipeline(HttpServletRequest request)throws Exception {
		PipelineRequest result = parse(request);
		result.setMimeType(request.getParameter("mimetype"));
		result.setEcho(request.getParameter("echo"));
		return result;
	}
}
