package gov.usgswim.sparrow.test;

import gov.usgswim.service.RequestHandler;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.service.IDByPointComputable;
import gov.usgswim.sparrow.service.IDByPointParser;
import gov.usgswim.sparrow.service.IDByPointRequest;
import gov.usgswim.sparrow.service.ModelParser;
import gov.usgswim.sparrow.service.ModelRequest;
import gov.usgswim.sparrow.service.ModelService;
import gov.usgswim.sparrow.util.SparrowUtil;

import java.awt.Point;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.stax2.XMLInputFactory2;


public class IDByPointParserTest extends TestCase {

	public IDByPointParserTest(String sTestName) {
		super(sTestName);
	}

	
	public void testBasic() throws Exception {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/id-point-request-1.xml"));
		
		IDByPointParser parser = new IDByPointParser();
		
		IDByPointRequest req = parser.parse(xsr);

		assertEquals(5, req.getNumberOfResults());
		assertEquals(22, req.getModelId());
		assertEquals(-100d, req.getPoint().getX());
		assertEquals(40d, req.getPoint().getY());
	}
	
}
