package gov.usgswim.sparrow.service;

import gov.usgswim.ThreadSafe;
import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.service.pipeline.PipelineRequest;
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
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.ctc.wstx.stax.WstxOutputFactory;

//TODO:  No caching is done of model data
@ThreadSafe
public class ModelService implements HttpRequestHandler<ModelRequest> {
	protected static Logger log =
		Logger.getLogger(ModelService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	
	
	public ModelService() {}
	
	
	public void dispatch(ModelRequest req, HttpServletResponse response) throws Exception {
		response.setContentType(RESPONSE_MIME_TYPE);
		dispatch(req, response.getOutputStream());
	}
	
	public void dispatch(PipelineRequest request, HttpServletResponse response) throws Exception {
		// TODO remove this simple redirection method by better use of generics
		dispatch((ModelRequest) request, response);
	}
	
	public void dispatch(ModelRequest req, OutputStream outStream) throws Exception {
																																 
		synchronized (factoryLock) {
			if (xoFact == null) {
				xoFact = WstxOutputFactory.newInstance();
			}
		}
		

		XMLEventWriter xw = xoFact.createXMLEventWriter(outStream);																										 
		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(getConnection());
		DomainSerializer ds = new DomainSerializer();
		ds.writeModels(xw, models);
		

	}
	
	
	protected Connection getConnection() throws NamingException, SQLException {
		return SharedApplication.getInstance().getConnection();
	}

	public void shutDown() {
		xoFact = null;
	}


	public XMLStreamReader getXMLStreamReader(PipelineRequest o, boolean needsCompleteFirstRow) throws Exception{
		return getXMLStreamReader((ModelRequest) o, needsCompleteFirstRow);
	}

	public XMLStreamReader getXMLStreamReader(ModelRequest o, boolean needsCompleteFirstRow) throws Exception{
		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(getConnection());
		DomainSerializer2 serializer = new DomainSerializer2(models);
		if (needsCompleteFirstRow) {
			serializer.setOutputCompleteFirstRow();
		}
		return serializer;
	}
}
