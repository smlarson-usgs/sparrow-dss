package gov.usgswim.service.pipeline;

import java.util.Arrays;
import java.util.List;

import gov.usgs.webservices.framework.formatter.DataFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.sparrow.service.IDByPointRequest;
import gov.usgswim.sparrow.service.ModelRequest;
import gov.usgswim.sparrow.service.PredictServiceRequest;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

public class PipelineRegistry {
	public static final List<String> flatMimeTypes = Arrays.asList(new String[] {"csv", "tab", "excel", "html"});

	public static Pipeline lookup(PipelineRequest o) {
		if (o.isEcho()) {
			return new EchoPipeline(o.getXMLRequest());
		}
		// otherwise ...
		return new Pipeline(){

			/**
			 * The handler is responsible for creating the source of XMLStreamReader events
			 */
			private HttpRequestHandler handler;

			public void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception {
				// Generators have to behave differently if flattening is needed
				// TODO refactor this into a query of the formatter.
				boolean isNeedsFlattening = flatMimeTypes.contains(o.getMimeType());
				XMLStreamReader reader = handler.getXMLStreamReader(o, isNeedsFlattening);
				//
				boolean isSparrowXML = (o instanceof PredictServiceRequest )||(o instanceof IDByPointRequest) ;
				boolean isModelXML = (o instanceof ModelRequest);
				OutputType outputType = Enum.valueOf(OutputType.class, o.getMimeType().toUpperCase());
//				TODO this code doesn't belong here. Refactor later. use generics
				IFormatter formatter = null;
				if (isSparrowXML) {
					switch (outputType) {
						case CSV:
						case TAB:
						case EXCEL:
						case HTML:
							formatter = new SparrowFlatteningFormatter(outputType);
							break;
						case XML:
							// XML is the default case
						default:
							formatter = new XMLPassThroughFormatter();
						break;
					}
				} else if (isModelXML) {
					switch (outputType) {
						case CSV:
						case TAB:
						case EXCEL:
						case HTML:
							DataFlatteningFormatter df = new DataFlatteningFormatter(outputType);
							df.setRowElementName("source");
							df.setKeepElderInfo(true);
							formatter = df;
							break;
						case XML:
							// XML is the default case
						default:
							formatter = new XMLPassThroughFormatter();
						break;
					}
				}
				formatter.setFileName(o.getFileName());
				formatter.dispatch(reader, response);

			}

			public void setHandler(HttpRequestHandler handler) {
				this.handler = handler;
			}

		};

	}


}
