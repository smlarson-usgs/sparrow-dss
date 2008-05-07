package gov.usgswim.sparrow.service.echo;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.XML;
import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.service.AbstractPipeline;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoPipeline extends AbstractPipeline implements Pipeline {

	private String xmlParamName;
	private String requestString;

	public EchoPipeline() {
		super(null, null);
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		// no flattening for PredictContext pipeline
		return new XMLPassThroughFormatter();
	}

	@Override
	public PipelineRequest parse(HttpServletRequest request) throws Exception {
		requestString = AbstractHttpRequestParser.defaultReadXMLRequest(request, xmlParamName);
		
		return new PipelineRequest() {

			public ResponseFormat getResponseFormat() {
				return null;
			}

			public String getXMLRequest() {
				return requestString;
			}

			public void setResponseFormat(ResponseFormat respFormat) {

			}

			public void setXMLRequest(String request) {
				requestString = request;
			}
			
		};
	}

	@Override
	public void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception {
		response.setContentType(XML.getMimeType());
//		response.addHeader( "Content-Disposition","attachment; filename=" + fileName + "." + XML.getFileSuffix() );
		PrintWriter out = response.getWriter();
		out.write(o.getXMLRequest());
		out.flush();
		out.close();
	}

	@Override
	public void setXMLParamName(String xmlParamName) {
		this.xmlParamName = xmlParamName;
	}

}
