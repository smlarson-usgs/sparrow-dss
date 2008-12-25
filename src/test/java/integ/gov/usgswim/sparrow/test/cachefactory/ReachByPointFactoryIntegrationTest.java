package gov.usgswim.sparrow.test.cachefactory;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.cachefactory.ReachByPointFactory;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import java.awt.Point;

import javax.xml.stream.XMLInputFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReachByPointFactoryIntegrationTest {
    public static LifecycleListener lifecycle = new LifecycleListener();

    protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
    @BeforeClass public static void setUpOnce() {
        lifecycle.contextInitialized(null, true);
    }

    @AfterClass public static void tearDownOnce() {
        lifecycle.contextDestroyed(null, true);
    }
    
	@Test public void testCreateEntry() throws Exception {
		ReachByPointFactory factory = new ReachByPointFactory();
		ModelPoint req = new ModelPoint(22L, new Point.Double(-90, 45));

		ReachInfo reach = (ReachInfo) factory.createEntry(req);
		
		assertNotNull(reach);
		assertEquals(19283, reach.getId());
		assertEquals("BLACK CR", reach.getName());
		assertEquals(1641, reach.getDistanceInMeters());
		
	}

	@Test public void testGetTextString() throws Exception {
		ReachByPointFactory factory = new ReachByPointFactory();
		
		assertTrue(factory.getText("FindReach").startsWith("SELECT * FROM ("));
	}

	
	
}
