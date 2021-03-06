package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.domain.ReachRowValueMap;


import org.junit.Test;

/**
 * Compares calculated area fractions to hand calculated values based on a
 * hypothetical river reach topology shown in a pdf resources file:  Look for a
 * file of the same name as this class .pdf.
 * 
 * There is also a .txt file that defines a topo table that is used to load the
 * reach topology.
 * 
 * @author eeverman
 */
public class CalcReachAreaFractionMapTest extends CalcFractionalAreaBaseTest {
		
	
	@Test
	public void TestSetupShouldNotHaveNullTopo() throws Exception {
		assertNotNull(network1_topo);
		assertNotNull(testTopo2);
		assertNotNull(testTopoCorrected);
	}
	
	@Test
	public void AreaFractionsShouldMatchPdfFileExampleInResources() throws Exception {
		
		// 11: The reach in the pdf sample table
		// This test file has all the FRACs totalling to 1
		CalcReachAreaFractionMap action = new CalcReachAreaFractionMap(network1_topo, 11L, false, false);
		ReachRowValueMap areaMap = action.run();
		doAreaFractionsShouldMatchPdfFileExampleInResources(areaMap);
		
		// This test file has some of the FRACs not totalling to 1, but in ways that we can correct.
		// Note that we never correct single reaches (non-diversions) that don't total to one
		// because that is likely a water utility.
		action = new CalcReachAreaFractionMap(testTopoCorrected, 11L, false, false);
		areaMap = action.run();
		doAreaFractionsShouldMatchPdfFileExampleInResources(areaMap);

	}
	
	public void doAreaFractionsShouldMatchPdfFileExampleInResources(ReachRowValueMap areaMap) throws Exception {
		
		assertNull(areaMap.getFraction(0));
		assertEquals(.72D, (double)areaMap.getFraction(1), COMP_ERROR);
		assertEquals(.72D, (double)areaMap.getFraction(2), COMP_ERROR);
		assertNull(areaMap.getFraction(3));
		assertNull(areaMap.getFraction(4));
		assertEquals(.72D, (double)areaMap.getFraction(5), COMP_ERROR);
		assertNull(areaMap.getFraction(6));
		assertNull(areaMap.getFraction(7));
		assertEquals(.9D, (double)areaMap.getFraction(8), COMP_ERROR);
		assertNull(areaMap.getFraction(9));
		assertEquals(.9D, (double)areaMap.getFraction(10), COMP_ERROR);
		assertEquals(1D, (double)areaMap.getFraction(11), COMP_ERROR);
		assertNull(areaMap.getFraction(12));
		assertNull(areaMap.getFraction(13));
	}
		
	@Test
	public void ShoreReachesHaveNoUpstreamAreaFractions() throws Exception {
		
		//13: A shore reach
		CalcReachAreaFractionMap action = new CalcReachAreaFractionMap(network1_topo, 13L, false, false);
		ReachRowValueMap areaMap = action.run();
		
		assertEquals(1D, (double)areaMap.getFraction(13), COMP_ERROR);
		assertEquals(1, areaMap.size());
	}
	
	@Test
	public void ANonTransmittingReachImmediatelyUpstreamOfTheTargetIsNotIncluded() throws Exception {

		//10: Has a non-transmitting reach (7) immediately upstream and a
		//transmitting reach (8).
		CalcReachAreaFractionMap action = new CalcReachAreaFractionMap(network1_topo, 10L, false, false);
		ReachRowValueMap areaMap = action.run();
		
		//The reach itself
		assertEquals(1D, (double)areaMap.getFraction(10), COMP_ERROR);
		
		//these reaches are on the non-transmitting branch and should not be included
		assertNull(areaMap.getFraction(3));
		assertNull(areaMap.getFraction(4));
		assertNull(areaMap.getFraction(6));
		assertNull(areaMap.getFraction(7));
		
		//These are the reaches on the transmitting branch
		assertEquals(1D, (double)areaMap.getFraction(8), COMP_ERROR);
		assertEquals(.8D, (double)areaMap.getFraction(5), COMP_ERROR);
		assertEquals(.8D, (double)areaMap.getFraction(2), COMP_ERROR);
		assertEquals(.8D, (double)areaMap.getFraction(1), COMP_ERROR);
		
		assertEquals(5, areaMap.size());
	}
	
	//See reference pdf file CalcReachAreaFractionMapTest2.pdf
	@Test
	public void braidedStreamShouldHaveAJoinedFracValueUpstream_TargetReach7() throws Exception {
		CalcReachAreaFractionMap action = new CalcReachAreaFractionMap(testTopo2, 7L, false, false);
		ReachRowValueMap areaMap = action.run();

		assertNull(areaMap.getFraction(0));
		assertNull(areaMap.getFraction(1));
		assertNull(areaMap.getFraction(2));
		assertEquals(1D, (double)areaMap.getFraction(3), COMP_ERROR);
		assertEquals(1D, (double)areaMap.getFraction(4), COMP_ERROR);
		assertEquals(1D, (double)areaMap.getFraction(5), COMP_ERROR);
		assertEquals(1D, (double)areaMap.getFraction(6), COMP_ERROR);
		assertEquals(1D, (double)areaMap.getFraction(7), COMP_ERROR);
		assertNull(areaMap.getFraction(8));
		assertNull(areaMap.getFraction(9));
		assertNull(areaMap.getFraction(10));
	}
	
	
	@Test
	public void braidedStreamShouldHaveAJoinedFracValueUpstream_TargetReach8() throws Exception {
		CalcReachAreaFractionMap action = new CalcReachAreaFractionMap(testTopo2, 8L, false, false);
		ReachRowValueMap areaMap = action.run();

		assertNull(areaMap.getFraction(0));
		assertNull(areaMap.getFraction(1));
		assertNull(areaMap.getFraction(2));
		assertEquals(.6D, (double)areaMap.getFraction(3), COMP_ERROR);
		assertEquals(.6D, (double)areaMap.getFraction(4), COMP_ERROR);
		assertEquals(.6D, (double)areaMap.getFraction(5), COMP_ERROR);
		assertEquals(.6D, (double)areaMap.getFraction(6), COMP_ERROR);
		assertEquals(.6D, (double)areaMap.getFraction(7), COMP_ERROR);
		assertEquals(1D, (double)areaMap.getFraction(8), COMP_ERROR);
		assertNull(areaMap.getFraction(9));
		assertNull(areaMap.getFraction(10));
	}
	

	
}

