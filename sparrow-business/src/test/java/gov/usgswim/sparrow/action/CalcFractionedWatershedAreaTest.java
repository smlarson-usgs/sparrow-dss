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
		assertNotNull(network1_inc_area);
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
	public void FractionedAreaShouldMatchPdfFileExampleInResources() throws Exception {
		
		// 11: The reach in the pdf sample table
		CalcReachAreaFractionMap areaMapAction = new CalcReachAreaFractionMap(network1_topo, 11L, false);
		ReachRowValueMap areaMap = areaMapAction.run();
		
		CalcFractionedWatershedArea areaAction = new CalcFractionedWatershedArea(areaMap, network1_inc_area);
		Double area = areaAction.run();
		
		
		//This area assumes that each reach has a catchment area of 2.
		assertEquals(9.92D, area, COMP_ERROR);

	}
	
	/**
	 * This test assumes that he incremental area for ALL REACHES IS 2.
	 * It also forces the action into a debug mode that assumes that the fraction
	 * for each reach included in the fraction map is 1.  Thus, it just adds up the
	 * areas.
	 * 
	 * (2 IS LOADED AS AN AREA FOR EACH REACH FROM A TEXT FILE)
	 * 
	 * Refer to CalcReachAreaFractionMapTest resource files for the sample
	 * network this is run on.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void UnfractionedAreaShouldMatchPdfFileExampleInResources() throws Exception {
		
		// 11: The reach in the pdf sample table
		CalcReachAreaFractionMap areaMapAction = new CalcReachAreaFractionMap(network1_topo, 11L, false);
		ReachRowValueMap areaMap = areaMapAction.run();
		
		CalcFractionedWatershedArea areaAction = new CalcFractionedWatershedArea(areaMap, network1_inc_area, true, false);
		Double area = areaAction.run();
		
		

		assertEquals(12D, area, COMP_ERROR);

	}
	




}
