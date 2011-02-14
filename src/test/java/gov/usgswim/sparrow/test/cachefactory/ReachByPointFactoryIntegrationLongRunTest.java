package gov.usgswim.sparrow.test.cachefactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;
import gov.usgswim.sparrow.cachefactory.ReachByPointFactory;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import java.awt.Point;

import javax.xml.stream.XMLInputFactory;

import org.junit.Test;

public class ReachByPointFactoryIntegrationLongRunTest extends SparrowDBTestBaseClass {

    protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	

    
	@Test public void testCreateEntry() throws Exception {
		ReachByPointFactory factory = new ReachByPointFactory();
		ModelPoint req = new ModelPoint(
				TEST_MODEL_ID,
				new Point.Double(-84.9D, 32.9D));

		
		ReachInfo reach = factory.createEntry(req);
		
		assertNotNull(reach);
		assertEquals(7887L, reach.getId());
		assertEquals("FLAT SHOAL CR", reach.getName());
		assertEquals(5292, reach.getDistanceInMeters());
	}

	
	
}
