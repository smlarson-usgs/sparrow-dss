package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.domain.HUCType;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.HUCRequest;

import java.util.List;

import org.junit.Test;

/**
 * Tests LoadHUC Actions
 * 
 * @author eeverman
 */
public class LoadHUCLongRunTest extends SparrowDBTestBaseClass {

	/**
	 * Loads all public models (1) from the test db.
	 * @throws Exception
	 */
	@Test
	public void testLoadHUC01() throws Exception {
		
		HUCRequest req = new HUCRequest("01");
		
		LoadHUC action = new LoadHUC(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getOrdinates();
		
		assertNull(action.getPostMessage());
		assertEquals("01", huc.getHucCode());
		assertEquals("NEW ENGLAND", huc.getName());
		assertEquals(HUCType.HUC2, huc.getHucType());
		assertEquals(false, huc.getGeometry().isLinear());
		assertEquals(true, huc.getGeometry().isClosed());
		assertEquals(-73.73509d, huc.getGeometry().getMinLong(), .0001d);
		assertEquals(40.94826d, huc.getGeometry().getMinLat(), .0001d);
		assertEquals(-66.954d, huc.getGeometry().getMaxLong(), .0001d);
		assertEquals(47.46188d, huc.getGeometry().getMaxLat(), .0001d);
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//System.out.println(ords.length);
	}
	
	@Test
	public void testLoadHUC0101() throws Exception {
		
		HUCRequest req = new HUCRequest("0101");
		
		LoadHUC action = new LoadHUC(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getOrdinates();
		
		assertNull(action.getPostMessage());
		assertEquals("0101", huc.getHucCode());
		assertEquals("ST. JOHN", huc.getName());
		assertEquals(HUCType.HUC4, huc.getHucType());
		assertEquals(false, huc.getGeometry().isLinear());
		assertEquals(true, huc.getGeometry().isClosed());
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//System.out.println(ords.length);
	}
	
	@Test
	public void testLoadHUC010100() throws Exception {
		
		HUCRequest req = new HUCRequest("010100");
		
		LoadHUC action = new LoadHUC(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getOrdinates();
		
		assertNull(action.getPostMessage());
		assertEquals("010100", huc.getHucCode());
		assertEquals("ST. JOHN", huc.getName());
		assertEquals(HUCType.HUC6, huc.getHucType());
		assertEquals(false, huc.getGeometry().isLinear());
		assertEquals(true, huc.getGeometry().isClosed());
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//System.out.println(ords.length);
	}
	
	@Test
	public void testLoadHUC06020002() throws Exception {
		
		HUCRequest req = new HUCRequest("06020002");
		
		LoadHUC action = new LoadHUC(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getOrdinates();
		
		assertNull(action.getPostMessage());
		assertEquals("06020002", huc.getHucCode());
		assertEquals("HIWASSEE", huc.getName());
		assertEquals(HUCType.HUC8, huc.getHucType());
		assertEquals(false, huc.getGeometry().isLinear());
		assertEquals(true, huc.getGeometry().isClosed());
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//System.out.println(ords.length);
	}
	
	
}

