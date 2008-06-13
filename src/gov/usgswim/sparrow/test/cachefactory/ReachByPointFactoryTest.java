package gov.usgswim.sparrow.test.cachefactory;

import gov.usgswim.sparrow.cachefactory.ReachByPointFactory;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.service.idbypoint.Reach;

import java.awt.geom.Point2D.Double;

import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;

public class ReachByPointFactoryTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testCreateEntry() throws Exception {
		ReachByPointFactory factory = new ReachByPointFactory();
		IDByPointRequest req = new IDByPointRequest(22L, new Double(-90, 45));

		Reach reach = (Reach) factory.createEntry(req);
		
		assertNotNull(reach);
		assertEquals(19283, reach.getId());
		assertEquals("BLACK CR", reach.getName());
		assertEquals(1641, reach.getDistanceInMeters());
		
	}

	public void testGetTextString() throws Exception {
		ReachByPointFactory factory = new ReachByPointFactory();
		
		assertTrue(factory.getText("FindReach").startsWith("SELECT * FROM ("));
	}

	
	
}
