package gov.usgswim.sparrow.test;

import java.sql.Connection;
import java.sql.DriverManager;

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

//	/**
//	 * @see DomainSerializer#writeModels(XMLStreamWriter,List)
//	 */
//	public void testWriteModelsStream() throws SQLException, XMLStreamException {
//		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(conn);
//		
//		DomainSerializer ds = new DomainSerializer();
//		
//		ds.writeModels(System.out, models);
//	}
	
//	/**
//	 * @see gov.usgswim.sparrow.service.DomainSerializer#writeModels(javax.xml.stream.XMLEventWriter,List)
//	 */
//	public void testWriteModelsEvents() throws SQLException, XMLStreamException {
//		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(conn);
//		XMLOutputFactory fact = XMLOutputFactory.newInstance();
//		
//		WstxEventWriter evtWriter = (WstxEventWriter) fact.createXMLEventWriter(System.out);
//		
//		DomainSerializer ds = new DomainSerializer();
//		
//		ds.writeModels(evtWriter, models);
//	}
}
