package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.TestCase;
import oracle.jdbc.OracleDriver;

import org.junit.Ignore;

@Ignore
public class DomainSerializerIntegrationTest extends TestCase {
	private Connection conn;

	public DomainSerializerIntegrationTest(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		conn = SharedApplication.getConnectionFromCommandLineParams();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		conn.close();
		conn = null;
	}

//	/**
//	 * @see DomainSerializer#writeModels(XMLStreamWriter,List)
//	 */
//	public void testWriteModelsStream() throws SQLException, XMLStreamException {
//		List<SparrowModelBuilder> models = JDBCUtil.loadModelMetaData(conn);
//
//		DomainSerializer ds = new DomainSerializer();
//
//		ds.writeModels(System.out, models);
//	}

//	/**
//	 * @see gov.usgswim.sparrow.service.DomainSerializer#writeModels(javax.xml.stream.XMLEventWriter,List)
//	 */
//	public void testWriteModelsEvents() throws SQLException, XMLStreamException {
//		List<SparrowModelBuilder> models = JDBCUtil.loadModelMetaData(conn);
//		XMLOutputFactory fact = XMLOutputFactory.newInstance();
//
//		WstxEventWriter evtWriter = (WstxEventWriter) fact.createXMLEventWriter(System.out);
//
//		DomainSerializer ds = new DomainSerializer();
//
//		ds.writeModels(evtWriter, models);
//	}
}
