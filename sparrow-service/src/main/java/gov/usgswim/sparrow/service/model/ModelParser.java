package gov.usgswim.sparrow.service.model;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.RequestParser;
import gov.usgswim.sparrow.parser.ResponseFormat;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

public class ModelParser 
	extends AbstractHttpRequestParser<ModelRequest> 
	implements RequestParser<ModelRequest> {

	public ModelParser() {
	}
	
	public ModelRequest parse(XMLStreamReader reader) throws Exception {
		ModelRequest req = null;


		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("model".equals(lName)) {
					req = new ModelRequest();
					
					if (reader.getAttributeCount() > 0) {
						for (int i = 0; i < reader.getAttributeCount(); i++)  {
							String name = reader.getAttributeLocalName(i);
							String val = reader.getAttributeValue(i);
							if ("public".equals(name)) {
								req.setPublic(val);
							} else if ("approved".equals(name)) {
								req.setApproved(val);
							} else if ("archived".equals(name)) {
								req.setArchived(val);
							} else if ("id".equals(name)) {
								req.setId(val);
							}
						}
						
						
					}
					
					
				} else if (ResponseFormat.isTargetMatch(lName)) {
					ResponseFormat respFormat = new ResponseFormat();
					respFormat.setMimeType("xml"); // default
					respFormat.parse(reader);
					req.setResponseFormat(respFormat);
				}
				
				
				break;
			}
		}
		
		return req;
	}

	@Override
	public ModelRequest parse(HttpServletRequest request)throws Exception {
		ModelRequest result = super.parse(request);
		ResponseFormat respFormat = result.getResponseFormat();
		
		String mimeType = request.getParameter("mimetype");
		if (mimeType != null) {
			respFormat.setMimeType(mimeType);
		}
		if (respFormat.getMimeType() == null){
			respFormat.setMimeType("xml"); // defaults to xml
		}

		String compress = request.getParameter("compress");
		if (compress != null && compress.equals("zip")) {
			respFormat.setCompression("zip");
		}
		return result;
	}
}
