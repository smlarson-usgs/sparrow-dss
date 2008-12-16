package gov.usgswim.sparrow.deprecated;

import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.service.DataTableSerializer;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.model.ModelService;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
//TODO not complete
@ThreadSafe
public class IDByPointService implements HttpService<IDByPointRequest_old> {
	protected static Logger log =
		Logger.getLogger(ModelService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
	
	
	//They promise these factories are threadsafe
	@SuppressWarnings("unused")
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	

	
	public IDByPointService() {}
	
	
public void shutDown() {
		xoFact = null;
	}
	
	public XMLStreamReader getXMLStreamReader(IDByPointRequest_old req, boolean isNeedsFlattening) throws Exception {
		// TODO isNeedsFlattening ignored for now because using custom flattener
		DataTable result = SharedApplication.getInstance().getIdByPointCache().compute(req);
		//boolean isFlatMimeType = PipelineRegistry.flatMimeTypes.contains(req.getMimeType());
		BasicXMLStreamReader reader = new DataTableSerializer(req, result);
		return reader;
	}
	
	public void dispatch(PipelineRequest request, HttpServletResponse response) throws Exception {
		// TODO remove this simple redirection method by better use of generics
		dispatch((IDByPointRequest_old) request, response);
	}


}
