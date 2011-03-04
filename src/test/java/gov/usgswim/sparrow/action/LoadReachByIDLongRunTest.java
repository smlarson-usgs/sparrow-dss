package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import org.apache.log4j.Level;
import org.junit.Test;


/**
 * There is one 'hole' in this set of tests.  To save a bit of work, we did not
 * manually load all upstream values into the .tab files - we stopped at reach 9681.
 * For incremental tests, we turn off transport for 9681 allowing the test to
 * validate that all reaches not listed in the .tab are zero.  For total
 * comparisons where the upstream values are important we can't do that
 * w/o generating values that can't be matched to what you would be able to
 * see/validate in the UI, so we leave the transport for reach 9681 ON.
 * 
 * As a consequence, total comparison are not able to exhaustively exclude that
 * there may be non-upstream values which are non-zero, as well as upstream
 * reaches which could be incorrect.
 * 
 * @author eeverman
 */
public class LoadReachByIDLongRunTest  extends SparrowDBTestBaseClass {
	
	static ReachID reachId;
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		
		//Uncomment to debug
		setLogLevel(Level.DEBUG);
		reachId = new ReachID(SparrowDBTestBaseClass.TEST_MODEL_ID, 6000L);
	}
	
	@Test
	public void basicTest() throws Exception {

		LoadReachByID action = new LoadReachByID();
		action.setReachId(reachId);
		ReachInfo info = action.run();
		assertEquals(6000L, info.getId());
		assertEquals("LITTLE R, W FK", info.getName());
		assertEquals("03", info.getHuc2());
		assertEquals("0305", info.getHuc4());
		assertEquals("030501", info.getHuc6());
		assertEquals("03050106", info.getHuc8());
		
		//spatial attribs
		assertEquals(-81.27737d, info.getMarkerLong(), .00001);
		assertEquals(34.46994d, info.getMarkerLat(), .00001);
		assertEquals(-81.29193d, info.getMinLong(), .00001);
		assertEquals(34.41154d, info.getMinLat(), .00001);
		assertEquals(-81.24072d, info.getMaxLong(), .00001);
		assertEquals(34.52984d, info.getMaxLat(), .00001);
	}
	
	
}

