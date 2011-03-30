package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.Segment;
import gov.usgswim.sparrow.domain.ReachGeometry;
import gov.usgswim.sparrow.request.ReachID;

import org.junit.Test;

/**
 * Tests LoadUpstreamCatchment Actions
 * 
 * @author eeverman
 */
public class LoadReachWatershedDetailLongRunTest extends SparrowTestBaseWithDB {

	/**
	 * Loads all public models (1) from the test db.
	 * @throws Exception
	 */
	@Test
	public void testLoad8153() throws Exception {
		
		ReachID reach = new ReachID(50L, 8153L);
		
		LoadReachWatershedDetail action = new LoadReachWatershedDetail();
		action.setReach(reach);
		
		ReachGeometry upstream = action.run();

		float[] ords = upstream.getGeometry().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals(new Long(8153L), upstream.getId());
		assertEquals(new Long(50L), upstream.getModelId());
		assertEquals(false, upstream.getGeometry().getSegments()[0].isLinear());
		assertEquals(true, upstream.getGeometry().getSegments()[0].isPolygon());
		
		assertTrue(ords.length > 0);
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		
//		printStats(upstream.getGeometry());
//		printStats(upstream.getSimpleGeometry());
//		printStats(upstream.getConvexGeometry());
	}
	
	
	public void printStats(Geometry geom) {
		Segment[] segs = geom.getSegments();
		System.out.println("Number of segments: " + segs.length);
		
		for (int i=0; i<segs.length; i++) {
			System.out.println("  -Segment[" + i + "] length: " + segs[i].getCoordinates().length);
		}
	}
	
	
}

