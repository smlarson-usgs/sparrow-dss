package gov.usgswim.sparrow.service;

import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.ThreadSafe;
import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.service.pipeline.PipelineRegistry;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.PredictData;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
//TODO not complete
@ThreadSafe
public class IDByPointService implements HttpRequestHandler<IDByPointRequest> {
	protected static Logger log =
		Logger.getLogger(ModelService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
	
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	
	protected Data2DSerializer serializer = new Data2DSerializer();
	
	public IDByPointService() {}
	
	
	public void dispatch(PipelineRequest request, HttpServletResponse response) throws Exception {
		// TODO remove this simple redirection method by better use of generics
		dispatch((IDByPointRequest) request, response);
	}
	
	/** (non-Javadoc)
	 * @see gov.usgswim.service.HttpRequestHandler#dispatch(java.lang.Object, javax.servlet.http.HttpServletResponse)
	 * @deprecated
	 */
	public void dispatch(IDByPointRequest req, HttpServletResponse response) throws Exception {
		response.setContentType(RESPONSE_MIME_TYPE);
		dispatch(req, response.getOutputStream());
	}
	
	/** (non-Javadoc)
	 * @see gov.usgswim.service.RequestHandler#dispatch(java.lang.Object, java.io.OutputStream)
	 * @deprecated
	 */
	public void dispatch(IDByPointRequest req, OutputStream outStream) throws Exception {
																																 
		Int2DImm result = SharedApplication.getInstance().getIdByPointCache().compute(req);
		

		serializer.writeResponse(outStream, result);

	}
	
	
	protected Connection getConnection() throws NamingException, SQLException {
		return SharedApplication.getInstance().getConnection();
	}

	public void shutDown() {
		xoFact = null;
	}


	public XMLStreamReader getXMLStreamReader(PipelineRequest o, boolean isNeedsFlattening)  throws Exception{
		return getXMLStreamReader((IDByPointRequest) o, isNeedsFlattening);
	}
	
	public XMLStreamReader getXMLStreamReader(IDByPointRequest req, boolean isNeedsFlattening) throws Exception {
		// TODO isNeedsFlattening ignored for now because using custom flattener
		Int2DImm result = SharedApplication.getInstance().getIdByPointCache().compute(req);
		boolean isFlatMimeType = PipelineRegistry.flatMimeTypes.contains(req.getMimeType());
		BasicXMLStreamReader reader = new Data2DSerializer2(req, result);
		return reader;
	}


}
