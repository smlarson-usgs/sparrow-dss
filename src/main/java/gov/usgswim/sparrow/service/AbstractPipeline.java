package gov.usgswim.sparrow.service;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.ZipFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ResponseFormat;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.NotImplementedException;

public abstract class AbstractPipeline<T extends PipelineRequest> implements Pipeline {

	protected final HttpService<T> handler;
	protected final AbstractHttpRequestParser<T> parser;
	public static final List<String> flatMimeTypes = Arrays.asList(new String[] {"csv", "tab", "excel", "html"});
	
	/**
	 * @param handler: if this is null, getXMLStreamReader() should be overridden by subclass
	 * @param parser: if this is null, parse(String) and parse(HttpServletRequest) should be overridden by subclass
	 */
	protected AbstractPipeline(HttpService<T> handler, AbstractHttpRequestParser<T> parser) {
		this.handler = handler;
		this.parser = parser;
	}

	public void dispatch(PipelineRequest o, OutputStream response) throws Exception {

		// TODO [IK] allow o to be null
		ResponseFormat respFormat = o.getResponseFormat();
		boolean isNeedsFlattening = AbstractPipeline.flatMimeTypes.contains(respFormat.getMimeType());
		XMLStreamReader reader = (handler != null)? handler.getXMLStreamReader((T)o, isNeedsFlattening): getXMLStreamReader((T)o, isNeedsFlattening);
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
				
				formatter = getConfiguredJSONFormatter();
				formatter = (formatter == null)? new JSONFormatter(): formatter;
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
	
	public void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception {
		
		// Generators have to behave differently if flattening is needed
		// TODO refactor this into a query of the formatter.
		ResponseFormat respFormat = o.getResponseFormat();
		boolean isNeedsFlattening = AbstractPipeline.flatMimeTypes.contains(respFormat.getMimeType());
		XMLStreamReader reader = (handler != null)? handler.getXMLStreamReader((T)o, isNeedsFlattening)
				:getXMLStreamReader((T)o, isNeedsFlattening);
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
				
				formatter = getConfiguredJSONFormatter();
				formatter = (formatter == null)? new JSONFormatter(): formatter;
				break;
			case XML:
				// XML is the default case for the pipeline
			default:
				formatter = new XMLPassThroughFormatter();
			break;
		}
	
		formatter.setFileName(respFormat.fileName);
		formatter.dispatch(reader, response, respFormat.isAttachement());
	
	}

	protected abstract IFormatter getCustomFlatteningFormatter(OutputType outputType);
	
	public IFormatter getConfiguredJSONFormatter() {
		return new JSONFormatter(); // unconfigured JSON formatter by default
	}

	public T parse(HttpServletRequest request) throws Exception {
		return parser.parse(request); // TODO allow nulls
	}

	public T parse(String xmlRequest) throws Exception {
	  return parser.parse(xmlRequest); // TODO allow nulls
  }

	public void setXMLParamName(String xmlParamName) {
		if (parser != null) {
			parser.setXmlParam(xmlParamName);
		}
		// otherwise, ignored because parser is null
	}
	
	/**
	 * getXMLStreamReader() of the Pipeline is called only in the case that no handler was provided for the pipeline
	 * @param o
	 * @param isNeedsCompleteFirstRow
	 * @return
	 * @throws Exception
	 */
	public XMLStreamReader getXMLStreamReader(T request, boolean isNeedsCompleteFirstRow) throws Exception{
		throw new NotImplementedException("You need to implement this method if you do not provide the Pipeline with a handler");
	};
	
	// TODO [IK] makeDefaultResponseFormat


}
