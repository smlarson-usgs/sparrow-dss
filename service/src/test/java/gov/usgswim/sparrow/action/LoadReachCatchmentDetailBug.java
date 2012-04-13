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
public class LoadReachCatchmentDetailBug extends SparrowTestBaseWithDB {

	@Override
	protected void doOneTimeGeneralSetup() throws Exception {
		// TODO Auto-generated method stub
		super.doOneTimeGeneralSetup();
		System.setProperty(SYS_PROP_USE_PRODUCTION_DB, "true");
	}

	@Override
	protected void doOneTimeCustomTearDown() throws Exception {
		// TODO Auto-generated method stub
		super.doOneTimeCustomTearDown();
		System.clearProperty(SYS_PROP_USE_PRODUCTION_DB);
	}

	@Test
	public void testLoad19322() throws Exception {
		
		ReachID reach = new ReachID(41L, 19322L);
		
		LoadReachCatchmentDetail action = new LoadReachCatchmentDetail();
		action.setReach(reach);
		
		ReachGeometry upstream = action.run();

		float[] ords = upstream.getBasin().getSegments()[0].getCoordinates();
		
		assertNull(action.getPostMessage());
		assertEquals(new Long(19322L), upstream.getId());
		assertEquals(new Long(41L), upstream.getModelId());
		assertEquals(false, upstream.getBasin().getSegments()[0].isLinear());
		assertEquals(true, upstream.getBasin().getSegments()[0].isPolygon());
		
		assertTrue(ords.length > 0);
		//the number of ords should always be even
		assertTrue(
				Math.abs(ords.length / 2 - ((float) (ords.length) / 2f)) < .1f);
		
		//Uncomment to spit out the complete geometry, including individual points.
		printStats(upstream.getBasin());
		printStats(upstream.getBasin());
		printStats(upstream.getBasin());
		printSegment(upstream.getBasin(), 0);
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

