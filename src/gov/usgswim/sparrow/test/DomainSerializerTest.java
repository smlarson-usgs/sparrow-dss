package gov.usgswim.sparrow.test;

import com.ctc.wstx.evt.WstxEventWriter;

import gov.usgswim.sparrow.domain.DomainSerializer;

import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.io.OutputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import oracle.jdbc.OracleDriver;

public class DomainSerializerTest extends TestCase {
	private Connection conn;
	
	public DomainSerializerTest(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}
	
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

	/**
	 * @see DomainSerializer#writeModels(XMLStreamWriter,List)
	 */
	public void testWriteModelsStream() throws SQLException, XMLStreamException {
		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(conn);
		
		DomainSerializer ds = new DomainSerializer();
		
		ds.writeModels(System.out, models);
	}
	
	/**
	 * @see DomainSerializer#writeModels(javax.xml.stream.XMLEventWriter,List)
	 */
	public void testWriteModelsEvents() throws SQLException, XMLStreamException {
		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(conn);
		XMLOutputFactory fact = XMLOutputFactory.newInstance();
		
		WstxEventWriter evtWriter = (WstxEventWriter) fact.createXMLEventWriter(System.out);
		
		DomainSerializer ds = new DomainSerializer();
		
		ds.writeModels(evtWriter, models);
	}
}
