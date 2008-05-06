package gov.usgswim.sparrow.test;

import java.io.IOException;
import java.awt.geom.Point2D.Double;

import gov.usgswim.sparrow.cachefactory.ReachByPointFactory;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest2;
import gov.usgswim.sparrow.service.idbypoint.Reach;
import junit.framework.TestCase;

public class ReachByPointFactoryTest extends TestCase {

	public void testCreateEntry() throws Exception {
		ReachByPointFactory factory = new ReachByPointFactory();
		IDByPointRequest2 req = new IDByPointRequest2(22, new Double(-90, 45), 1);

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
