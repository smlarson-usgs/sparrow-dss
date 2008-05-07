package gov.usgswim.sparrow.service.idbypoint;

import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.service.DataTableSerializer;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.model.ModelService;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
//TODO not complete
@ThreadSafe
public class IDByPointService2 implements HttpService<IDByPointRequest2> {
	protected static Logger log =
		Logger.getLogger(ModelService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
	
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	

	
	public IDByPointService2() {}
	
	
//	public void dispatch(PipelineRequest request, HttpServletResponse response) throws Exception {
//		// TODO remove this simple redirection method by better use of generics
//		dispatch((IDByPointRequest2) request, response);
//	}
	

	protected Connection getConnection() throws NamingException, SQLException {
		return SharedApplication.getInstance().getConnection();
	}

	public void shutDown() {
		xoFact = null;
	}


	public XMLStreamReader getXMLStreamReader(PipelineRequest o, boolean isNeedsFlattening)  throws Exception{
		return getXMLStreamReader((IDByPointRequest2) o, isNeedsFlattening);
	}
	
	public XMLStreamReader getXMLStreamReader(IDByPointRequest2 req, boolean isNeedsFlattening) throws Exception {
		// TODO isNeedsFlattening ignored for now because using custom flattener
		// HACK [eric or IK] temporarily converting into old request format. Remove when done.
	//	IDByPointRequest oldReq = new IDByPointRequest(req.getModelID(), req.getPoint(), req.getNumberOfResults());
	    IDByPointRequest oldReq = new IDByPointRequest(req.getModelID(), req.getPoint(), 7);
		// TODO [eric] update this old cache code. Should use IDByPointRequest2 rather than IDByPointRequest
		DataTable result = SharedApplication.getInstance().getIdByPointCache().compute(oldReq);
		BasicXMLStreamReader reader = new DataTableSerializer(oldReq, result);
		return reader;
	}


}
