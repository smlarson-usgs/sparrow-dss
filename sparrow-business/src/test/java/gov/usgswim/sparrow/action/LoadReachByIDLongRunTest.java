package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.request.ReachClientId;
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
public class LoadReachByIDLongRunTest  extends SparrowTestBaseWithDB {
	
	static ReachClientId clientReachId;
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
		clientReachId = new ReachClientId(SparrowTestBaseWithDB.TEST_MODEL_ID, "6000");
	}
	
	@Test
	public void basicTest() throws Exception {

		LoadReachByID action = new LoadReachByID();
		action.setReachId(clientReachId);
		ReachInfo info = action.run();
		assertEquals(new Long(6000L), info.getReachId());
		assertEquals("LITTLE R, W FK", info.getName());
		assertEquals("03", info.getHuc2());
		assertEquals("0305", info.getHuc4());
		assertEquals("030501", info.getHuc6());
		assertEquals("03050106", info.getHuc8());
	
		
		//spatial attribs
		assertEquals(-81.31852d, info.getGeometry().getBasin().getMinLong(), .002);
		assertEquals(34.41093d, info.getGeometry().getBasin().getMinLat(), .002);
		assertEquals(-81.23758d, info.getGeometry().getBasin().getMaxLong(), .002);
		assertEquals(34.58522d, info.getGeometry().getBasin().getMaxLat(), .002);

		
		//System.out.println(info.toIdentificationXML());
	}
	
	
}

