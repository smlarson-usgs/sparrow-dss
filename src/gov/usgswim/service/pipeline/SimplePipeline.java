package gov.usgswim.service.pipeline;

import gov.usgs.webservices.framework.formatter.DataFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.ZipFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;

import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.service.ResponseFormat;

import gov.usgswim.sparrow.service.IDByPointRequest;
import gov.usgswim.sparrow.service.ModelRequest;
import gov.usgswim.sparrow.service.PredictServiceRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLStreamReader;

public class SimplePipeline implements Pipeline {
	
 /**
  * The handler is responsible for creating the source of XMLStreamReader events
  */
	private HttpRequestHandler handler;
	
	public SimplePipeline() {
	}

	public void setHandler(HttpRequestHandler handler) {
		this.handler = handler;
	}
	

	public void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception {
		// Generators have to behave differently if flattening is needed
		// TODO refactor this into a query of the formatter.
		ResponseFormat responseFormat = o.getResponseFormat();
		boolean isNeedsFlattening = PipelineRegistry.flatMimeTypes.contains(responseFormat.getMimeType());
		XMLStreamReader reader = handler.getXMLStreamReader(o, isNeedsFlattening);
		//
		boolean isSparrowXML = (o instanceof PredictServiceRequest )||(o instanceof IDByPointRequest) ;
		boolean isModelXML = (o instanceof ModelRequest);
//				TODO this code doesn't belong here. Refactor later. use generics
		IFormatter formatter = null;
		if (isSparrowXML) {
			switch (responseFormat.getOutputType()) {
				case CSV:
				case TAB:
				case EXCEL:
				case HTML:
					if ("zip".equals(responseFormat.getCompression())) {
						formatter = new ZipFormatter(new SparrowFlatteningFormatter(responseFormat.getOutputType()));
					} else {
						formatter = new SparrowFlatteningFormatter(responseFormat.getOutputType());
					}
					
					break;
				case JSON:
					formatter = new JSONFormatter();
					break;
				case XML:
					// XML is the default case
				default:
					formatter = new XMLPassThroughFormatter();
				break;
			}
		} else if (isModelXML) {
			switch (responseFormat.getOutputType()) {
				case CSV:
				case TAB:
				case EXCEL:
				case HTML:
					DataFlatteningFormatter df = new DataFlatteningFormatter(responseFormat.getOutputType());
					df.setRowElementName("source");
					df.setKeepElderInfo(true);
					formatter = ("zip".equals(responseFormat.getCompression()))? new ZipFormatter(df): (IFormatter) df;
					break;
				case JSON:
					formatter = new JSONFormatter();
					break;
				case XML:
					// XML is the default case
				default:
					formatter = new XMLPassThroughFormatter();
				break;
			}
		}
		formatter.setFileName(responseFormat.fileName);
		formatter.dispatch(reader, response);

	}

	public PipelineRequest parse(HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void setXMLParamName(String xmlParamName) {
		// TODO Auto-generated method stub
		
	}


}
