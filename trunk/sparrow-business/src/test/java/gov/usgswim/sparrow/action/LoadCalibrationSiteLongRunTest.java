package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.domain.CalibrationSite;

public class LoadCalibrationSiteLongRunTest extends SparrowTestBaseWithDB{

	/**
	 * The model filter was not implemented - this test verifies that even if
	 * a cal site in a different model is closest to where the users clicks,
	 * only the site closest to the click in the selected model is returned.
	 * 
	 * @throws Exception
	 */
	@Test
	public void loadModel50CalibrationSiteUsingLatLongOfSiteInModel49() throws Exception {
		LoadCalibrationSite action = new LoadCalibrationSite(28.0769D, -80.7703D, 50L);
		CalibrationSite result = action.run();
		
		assertEquals("02232500", result.getStationId());
		assertEquals("664270", result.getReachId());
	}
}
