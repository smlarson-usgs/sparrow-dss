package gov.usgswim.service.pipeline;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.XML;
import gov.usgswim.service.HttpRequestHandler;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class EchoPipeline implements Pipeline {

	private final String xmlRequest;
	private String fileName = "request";
	

	public EchoPipeline(String request) {
		this.xmlRequest = request;
	}

	public void dispatch(PipelineRequest o, HttpServletResponse response)
			throws Exception {
		response.setContentType(XML.getMimeType());
		response.addHeader( "Content-Disposition","attachment; filename=" + fileName + "." + XML.getFileSuffix() );
		PrintWriter out = response.getWriter();
		out.write(o.getXMLRequest());
		out.flush();
		out.close();
	}

	public void setHandler(HttpRequestHandler handler) {
		// no handler needed
	}

}
