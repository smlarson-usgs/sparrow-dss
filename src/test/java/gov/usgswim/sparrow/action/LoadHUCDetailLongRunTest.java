package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.domain.HUCType;
import gov.usgswim.sparrow.domain.Segment;
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
public class LoadHUCDetailLongRunTest extends SparrowDBTestBaseClass {

	/**
	 * Loads all public models (1) from the test db.
	 * @throws Exception
	 */
	@Test
	public void testLoadHUC01() throws Exception {
		
		HUCRequest req = new HUCRequest("01");
		
		LoadHUCDetail action = new LoadHUCDetail(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals("01", huc.getHucCode());
		assertEquals("NEW ENGLAND", huc.getName());
		assertEquals(HUCType.HUC2, huc.getHucType());
		assertEquals(false, huc.getGeometry().getSegments()[0].isLinear());
		assertEquals(true, huc.getGeometry().getSegments()[0].isPolygon());
		assertEquals(-73.73509d, huc.getGeometry().getMinLong(), .0001d);
		assertEquals(40.94826d, huc.getGeometry().getMinLat(), .0001d);
		assertEquals(-66.954d, huc.getGeometry().getMaxLong(), .0001d);
		assertEquals(47.46188d, huc.getGeometry().getMaxLat(), .0001d);
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		
//		printStats(huc.getGeometry());
//		printStats(huc.getSimpleGeometry());
//		printStats(huc.getConvexGeometry());
	}
	
	@Test
	public void testLoadHUC0101() throws Exception {
		
		HUCRequest req = new HUCRequest("0101");
		
		LoadHUCDetail action = new LoadHUCDetail(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals("0101", huc.getHucCode());
		assertEquals("ST. JOHN", huc.getName());
		assertEquals(HUCType.HUC4, huc.getHucType());
		assertEquals(false, huc.getGeometry().getSegments()[0].isLinear());
		assertEquals(true, huc.getGeometry().getSegments()[0].isPolygon());
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//System.out.println(ords.length);
	}
	
	@Test
	public void testLoadHUC010100() throws Exception {
		
		HUCRequest req = new HUCRequest("010100");
		
		LoadHUCDetail action = new LoadHUCDetail(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals("010100", huc.getHucCode());
		assertEquals("ST. JOHN", huc.getName());
		assertEquals(HUCType.HUC6, huc.getHucType());
		assertEquals(false, huc.getGeometry().getSegments()[0].isLinear());
		assertEquals(true, huc.getGeometry().getSegments()[0].isPolygon());
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//System.out.println(ords.length);
	}
	
	@Test
	public void testLoadHUC06020002() throws Exception {
		
		HUCRequest req = new HUCRequest("06020002");
		
		LoadHUCDetail action = new LoadHUCDetail(req);
		HUC huc = action.run();

		float[] ords = huc.getGeometry().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals("06020002", huc.getHucCode());
		assertEquals("HIWASSEE", huc.getName());
		assertEquals(HUCType.HUC8, huc.getHucType());
		assertEquals(false, huc.getGeometry().getSegments()[0].isLinear());
		assertEquals(true, huc.getGeometry().getSegments()[0].isPolygon());
		
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//System.out.println(ords.length);
	}
	
	public void printStats(Geometry geom) {
		Segment[] segs = geom.getSegments();
		System.out.println("Number of segments: " + segs.length);
		
		for (int i=0; i<segs.length; i++) {
			System.out.println("  -Segment[" + i + "] length: " + segs[i].getCoordinates().length);
		}
	}
	
	
}

