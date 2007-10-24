package gov.usgswim.sparrow.service;

import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.ThreadSafe;
import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.io.OutputStream;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;

import javax.naming.NamingException;

import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;

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
	
	
	public void dispatch(IDByPointRequest req, HttpServletResponse response) throws Exception {
		response.setContentType(RESPONSE_MIME_TYPE);
		dispatch(req, response.getOutputStream());
	}
	
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
}
