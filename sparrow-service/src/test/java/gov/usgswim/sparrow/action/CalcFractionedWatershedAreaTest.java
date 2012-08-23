package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class CalcFractionedWatershedAreaTest extends CalcFractionalAreaBaseTest {
		
		
	@Test
	public void TestSetupShouldNotHaveNullIncrementalAreas() throws Exception {
		assertNotNull(incrementalAreaTable);
	}
	
	/**
	 * This test assumes that he incremental area for ALL REACHES IS 2.
	 * 
	 * (2 IS LOADED AS AN AREA FOR EACH REACH FROM A TEXT FILE)
	 * 
	 * Refer to CalcReachAreaFractionMapTest resource files for the sample
	 * network this is run on.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void AreaFractionsShouldMatchPdfFileExampleInResources() throws Exception {
		
		// 11: The reach in the pdf sample table
		CalcReachAreaFractionMap areaMapAction = new CalcReachAreaFractionMap(testTopo, 11L);
		ReachRowValueMap areaMap = areaMapAction.run();
		
		CalcFractionedWatershedArea areaAction = new CalcFractionedWatershedArea(areaMap, incrementalAreaTable);
		Double area = areaAction.run();
		
		

		assertEquals(9.92D, area, COMP_ERROR);

	}
	




}
