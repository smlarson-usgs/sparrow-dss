package gov.usgswim.sparrow.service.json;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.JSON;
import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.service.AbstractPipeline;
import gov.usgswim.sparrow.service.model.ModelPipeline;
import gov.usgswim.sparrow.service.predict.PredictPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;

import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * Accepts known xml formats and outputs JSON
 * 
 * @author ilinkuo
 * 
 */
public class JSONifyPipeline extends AbstractPipeline implements Pipeline {

	private String xmlParamName;
	private String requestString;

	@SuppressWarnings("unchecked")
	public JSONifyPipeline() {
		super(null, null);
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		throw new RuntimeException(this.getClass().getSimpleName() + " does not flatten. Error in delegation.");
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
				// do nothing
			}

			public void setXMLRequest(String request) {
				requestString = request;
			}
			
		};
	}

	@Override
	public void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception {
		response.setContentType(JSON.getMimeType());
		
		// Configure the JSON formatter
		JSONFormatter jFormatter = new JSONFormatter();
		PredictPipeline.configure(jFormatter);
		ModelPipeline.configure(jFormatter);
		PredictContextPipeline.configure(jFormatter);
		
		PrintWriter out = response.getWriter();
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader in = inFact.createXMLStreamReader(new StringReader(o.getXMLRequest()));
		jFormatter.dispatch(in, out);
		
		// TODO might have to remove these
		out.flush();
		out.close();
	}

	@Override
	public void setXMLParamName(String xmlParamName) {
		this.xmlParamName = xmlParamName;
	}
}
