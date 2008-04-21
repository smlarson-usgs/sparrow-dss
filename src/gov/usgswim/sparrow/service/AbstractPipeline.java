package gov.usgswim.sparrow.service;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.ZipFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.service.ResponseFormat;
import gov.usgswim.service.pipeline.EchoPipeline;
import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRegistry;
import gov.usgswim.service.pipeline.PipelineRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

public abstract class AbstractPipeline<T extends PipelineRequest> implements Pipeline {

	protected final HttpRequestHandler<T> handler;
	protected final AbstractHttpRequestParser<T> parser;
	
	protected AbstractPipeline(HttpRequestHandler<T> handler, AbstractHttpRequestParser<T> parser) {
		this.handler = handler;
		this.parser = parser;
	}

	public void setHandler(HttpRequestHandler handler) {
		// TODO Auto-generated method stub
	}

	public void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception {
		if (o.isEcho()) {
			Pipeline pipeline = new EchoPipeline();
			pipeline.dispatch(o, response);
			return;
		}
		
		// Generators have to behave differently if flattening is needed
		// TODO refactor this into a query of the formatter.
		ResponseFormat respFormat = o.getResponseFormat();
		boolean isNeedsFlattening = PipelineRegistry.flatMimeTypes.contains(respFormat.getMimeType());
		XMLStreamReader reader = handler.getXMLStreamReader((T)o, isNeedsFlattening);
		// TODO worry about genericize later
		OutputType outputType = respFormat.getOutputType();
		IFormatter formatter = null;
	
		switch (outputType) {
			case CSV:
			case TAB:
			case EXCEL:
			case HTML:
				IFormatter df = getCustomFlatteningFormatter(outputType);
				formatter = ("zip".equals(respFormat.getCompression()))? new ZipFormatter(df): (IFormatter) df;
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
	
		formatter.setFileName(respFormat.fileName);
		formatter.dispatch(reader, response);
	
	}

	protected abstract IFormatter getCustomFlatteningFormatter(OutputType outputType);
	
	public PipelineRequest parse(HttpServletRequest request) throws Exception {
		return parser.parseForPipeline(request);
	}

	public void setXMLParamName(String xmlParamName) {
		parser.setXmlParam(xmlParamName);
	}

}
