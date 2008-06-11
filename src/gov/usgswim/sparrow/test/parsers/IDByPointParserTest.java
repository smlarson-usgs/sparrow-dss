package gov.usgswim.sparrow.test.parsers;

import java.io.InputStream;

import gov.usgswim.sparrow.deprecated.IDByPointParser;
import gov.usgswim.sparrow.deprecated.IDByPointRequest_old;
import gov.usgswim.sparrow.service.idbypoint.IDByPointPipeline;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.test.TestHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.stax2.XMLInputFactory2;


public class IDByPointParserTest extends TestCase {

	public static final String ID_BY_POINT_REQ_1 = "/gov/usgswim/sparrow/test/sample/id-point-request-1.xml";


	public IDByPointParserTest(String sTestName) {
		super(sTestName);
	}

	
	public void testSimpleRequest() throws Exception {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream(ID_BY_POINT_REQ_1));
		
		IDByPointParser parser = new IDByPointParser();
		
		IDByPointRequest_old req = parser.parse(xsr);

		assertEquals(5, req.getNumberOfResults());
		assertEquals(22, req.getModelId().intValue());
		assertEquals(-100d, req.getPoint().getX());
		assertEquals(40d, req.getPoint().getY());
	}


	public static IDByPointRequest buildIDByPointRequest1() throws Exception {
		InputStream is = IDByPointParserTest.class.getResourceAsStream(ID_BY_POINT_REQ_1);
		String xml = TestHelper.readToString(is);
		
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
}
