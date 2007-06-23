package gov.usgswim.sparrow.test;

import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.sparrow.domain.DomainSerializer;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.service.MetaRequestHandler;

import gov.usgswim.sparrow.service.ModelRequest;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.io.StringReader;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.List;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import javax.xml.stream.XMLStreamReader;

import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import oracle.jdbc.OracleDriver;

import org.codehaus.stax2.XMLInputFactory2;

public class MetaRequestHandlerTest extends TestCase {
	//private Connection conn;

	public MetaRequestHandlerTest(String sTestName) {
		super(sTestName);
	}
	/*
	protected void setUp() throws Exception {
		super.setUp();
		

		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		conn = DriverManager.getConnection(thinConn,username,password);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		conn.close();
		conn = null;
	}
	
*/

	/**
	 * @see MetaRequestHandler#printEventInfo(XMLStreamReader)
	 */
	public void testPrintEventInfo() throws SQLException, XMLStreamException {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/model_request_1.xml"));

		MetaRequestHandler handler = new MetaRequestHandler();
		
		ModelRequest req = (ModelRequest) handler.handleRequest(xsr);
		
		this.assertTrue(req.isPublic());
		this.assertTrue(req.isApproved());
		this.assertFalse(req.isArchived());
		this.assertTrue(req.isSources());
		
		
		/// Test w/ opposite values
		xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/model_request_2.xml"));

		req = (ModelRequest) handler.handleRequest(xsr);
		
		this.assertFalse(req.isPublic());
		this.assertFalse(req.isApproved());
		this.assertTrue(req.isArchived());
		this.assertFalse(req.isSources());
		
	}
}
