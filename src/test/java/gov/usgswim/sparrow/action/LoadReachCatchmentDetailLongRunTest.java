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
 * Tests LoadReachCatchmentDetail Action
 * 
 * @author eeverman
 */
public class LoadReachCatchmentDetailLongRunTest extends SparrowTestBaseWithDB {


	@Test
	public void testLoad8278() throws Exception {
		
		ReachID reach = new ReachID(50L, 8278L);
		
		LoadReachCatchmentDetail action = new LoadReachCatchmentDetail();
		action.setReach(reach);
		
		ReachGeometry upstream = action.run();

		float[] ords = upstream.getSimpleGeometry().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals(new Long(8278L), upstream.getId());
		assertEquals(new Long(50L), upstream.getModelId());
		assertEquals(false, upstream.getSimpleGeometry().getSegments()[0].isLinear());
		assertEquals(true, upstream.getSimpleGeometry().getSegments()[0].isPolygon());
		
		assertTrue(ords.length > 0);
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//Uncomment to spit out the complete geometry, including individual points.
//		printStats(upstream.getSimpleGeometry());
//		printStats(upstream.getSimpleGeometry());
//		printStats(upstream.getConvexGeometry());
//		printSegment(upstream.getSimpleGeometry(), 0);
	}
	
	@Test
	public void testLoad7677() throws Exception {
		
		ReachID reach = new ReachID(50L, 7677L);
		
		LoadReachCatchmentDetail action = new LoadReachCatchmentDetail();
		action.setReach(reach);
		
		ReachGeometry upstream = action.run();

		float[] ords = upstream.getSimpleGeometry().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals(new Long(7677L), upstream.getId());
		assertEquals(new Long(50L), upstream.getModelId());
		assertEquals(false, upstream.getSimpleGeometry().getSegments()[0].isLinear());
		assertEquals(true, upstream.getSimpleGeometry().getSegments()[0].isPolygon());
		
		assertTrue(ords.length > 0);
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//Uncomment to spit out the complete geometry, including individual points.
//		printStats(upstream.getSimpleGeometry());
//		printStats(upstream.getSimpleGeometry());
//		printStats(upstream.getConvexGeometry());
//		printSegment(upstream.getSimpleGeometry(), 0);
	}
	
	
	public void printStats(Geometry geom) {
		Segment[] segs = geom.getSegments();
		System.out.println("Number of segments: " + segs.length);
		
		for (int i=0; i<segs.length; i++) {
			System.out.println("  -Segment[" + i + "] length: " + segs[i].getCoordinates().length);
		}
	}
	
	public void printSegment(Geometry geom, int segmentIndex) {
		Segment[] segs = geom.getSegments();
		System.out.println("Coords for segment " + segmentIndex);
		
		Segment seg = segs[segmentIndex];
		float[] coords = seg.getCoordinates();
		
		for (int i=0; i<coords.length; i+=2) {
			System.out.println("" + coords[i] + "," + coords[i+1]);
		}
	}
	
	
}

