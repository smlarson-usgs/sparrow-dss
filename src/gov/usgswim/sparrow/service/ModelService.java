package gov.usgswim.sparrow.service;


import com.ctc.wstx.stax.WstxEventFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.sparrow.domain.DomainSerializer;
import gov.usgswim.sparrow.domain.Model;

import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;

import java.sql.Connection;

import java.sql.SQLException;

import java.util.List;

import javax.naming.InitialContext;

import javax.naming.NamingException;

import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;


public class ModelService implements HttpServiceHandler, ServiceHandler,
			RequestParser<ModelRequest>, RequestHandler<ModelRequest> {
	protected static Logger log =
		Logger.getLogger(ModelService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	
	
	public ModelService() {}
	
	public void dispatch(XMLStreamReader in,
											 HttpServletResponse response) throws XMLStreamException, IOException {
											 
		ModelRequest req = parse(in);
		response.setContentType(RESPONSE_MIME_TYPE);
		dispatch(req, response.getOutputStream());
	}

	public void dispatch(XMLStreamReader in, OutputStream out) throws XMLStreamException, IOException {
																															
		ModelRequest req = parse(in);
		dispatch(req, out);
	}
	
	public void dispatch(ModelRequest req, OutputStream outStream) throws IOException {
																																 
		synchronized (factoryLock) {
			if (xoFact == null) {
				xoFact = WstxOutputFactory.newInstance();
			}
		}
		
		try {
		
			XMLEventWriter xw = xoFact.createXMLEventWriter(outStream);																										 
			List<ModelBuilder> models = JDBCUtil.loadModelMetaData(getConnection());
			DomainSerializer ds = new DomainSerializer();
			ds.writeModels(xw, models);
			
		} catch (SQLException e) {
			throw new IOException("Error in query");
		} catch (NamingException e) {
			throw new IOException("Could not create jndi connection");
		} catch (XMLStreamException e) {
			throw new IOException("xml streaming error");
		}
	}
	
	public ModelRequest parse(XMLStreamReader reader) throws XMLStreamException {
		ModelRequest req = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("model".equals(lName)) {
					req = new ModelRequest();
					
					if (reader.getAttributeCount() > 0) {
						for (int i = 0; i < reader.getAttributeCount(); i++)  {
							String name = reader.getAttributeLocalName(i);
							String val = reader.getAttributeValue(i);
							if ("public".equals(name)) {
								req.setPublic(val);
							} else if ("approved".equals(name)) {
								req.setApproved(val);
							} else if ("archived".equals(name)) {
								req.setArchived(val);
							}
						}
						
						
					}
					
					
				} else if ("source".equals(lName)) {
					req.setSources(true);
				}
				
				
				break;
			}
		}
		
		return req;
	}
	
	protected Connection getConnection() throws NamingException, SQLException {
		return SharedApplication.getInstance().getConnection();
	}


}
