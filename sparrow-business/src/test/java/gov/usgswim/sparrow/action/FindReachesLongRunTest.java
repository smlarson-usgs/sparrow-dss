package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgs.cida.datatable.DataTable;
import static gov.usgswim.sparrow.SparrowTestBase.TEST_MODEL_ID;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.service.idbypoint.FindReachRequest;

import org.junit.Test;

public class FindReachesLongRunTest extends SparrowTestBaseWithDB {

	@Test
	public void searchByHiLowContribAreaWithMultipleResults() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.totContributingAreaHi = "100";
		req.totContributingAreaLo = "99.5";

		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(50);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 15);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("7126", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("BIG SANDY CR", result.getString(0, result.getColumnByName("REACH_NAME")));
		assertEquals("81.02", result.getString(0, result.getColumnByName("MEANQ")));
		assertEquals("99.82", result.getString(0, result.getColumnByName("TOT_CONTRIB_AREA")));

		assertEquals("03", result.getString(0, result.getColumnByName("HUC2")));
		assertEquals("0307", result.getString(0, result.getColumnByName("HUC4")));
		assertEquals("030701", result.getString(0, result.getColumnByName("HUC6")));
		assertEquals("03070103", result.getString(0, result.getColumnByName("HUC8")));
	}
	
	/**
	 * The contributing area for this reach is different than the total area for
	 * this reach, so this test ensures that we are search on the correct column.
	 * 
	 * **Note that the areas in the DEV db (the one being queries for these tests)
	 * is different than the values in the TEST db.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void searchByHiLowContribAreaWithSingleResultAndEnsureContribAreaNotTotalArea() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.totContributingAreaHi = "113285";
		req.totContributingAreaLo = "113284";

		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(50);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 1);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("9682", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("MOBILE R", result.getString(0, result.getColumnByName("REACH_NAME")));
		assertEquals("64063.55", result.getString(0, result.getColumnByName("MEANQ")));
		assertEquals("113284.268214", result.getString(0, result.getColumnByName("TOT_CONTRIB_AREA")));

		assertEquals("03", result.getString(0, result.getColumnByName("HUC2")));
		assertEquals("0316", result.getString(0, result.getColumnByName("HUC4")));
		assertEquals("031602", result.getString(0, result.getColumnByName("HUC6")));
		assertEquals("03160204", result.getString(0, result.getColumnByName("HUC8")));
	}
	
	@Test
	public void searchByHiLowContribAreaValidationCheck() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.totContributingAreaHi = "10";
		req.totContributingAreaLo = "11";

		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(50);

		DataTable result = action.run();

		assertNull(result);
		assertEquals(1, action.getValidationErrors().length);
	}

	@Test
	public void meanFlow() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.meanQHi = "35.5";
		req.meanQLo = "34.5";

		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(10);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 10);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4669", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("*A", result.getString(0, result.getColumnByName("REACH_NAME")));
		assertEquals("35.41", result.getString(0, result.getColumnByName("MEANQ")));
		assertEquals("99.17", result.getString(0, result.getColumnByName("CATCH_AREA")));

		assertEquals("03", result.getString(0, result.getColumnByName("HUC2")));
		assertEquals("0301", result.getString(0, result.getColumnByName("HUC4")));
		assertEquals("030101", result.getString(0, result.getColumnByName("HUC6")));
		assertEquals("03010103", result.getString(0, result.getColumnByName("HUC8")));
	}

	@Test
	public void reachId() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.reachIDs = "81163";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(7);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 1);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("81163", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void reachMultipleIds() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.reachIDs = " 4669, 81163 ";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(7);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 2);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4669", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("81163", result.getString(1, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void reachMultipleIdsWithEmptyEntries() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.reachIDs = ", 4669,, , 81163, ";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(7);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 2);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4669", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("81163", result.getString(1, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void reachName() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.reachName = "app";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(7);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 7);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("81163", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("8164", result.getString(6, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void eadCode() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.edaCode = "S010x,S060x";

		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(10);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 10);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4563", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("5469", result.getString(9, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void eadCodeWithSpaces() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.edaCode = "S010x,  S060x";

		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(10);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 10);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4563", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("5469", result.getString(9, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void eadName() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.edaName = "Albemarle Sound,  Winyah Bay";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(10);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 10);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4563", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("5469", result.getString(9, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void huc() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.huc = "030101";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(10);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 10);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4563", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("4630", result.getString(9, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void mixedCriteria() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.edaName = " , ,, Albemarle Sound, Altamaha River, ";
		req.meanQLo = "50";
		req.totContributingAreaLo = "100";
		req.totContributingAreaHi = "150";
		req.reachName = "cr";
		req.huc = "030102";

		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(10);

		DataTable result = action.run();

		assertTrue(result.getRowCount() == 4);
		assertEquals(0, action.getValidationErrors().length);

		assertEquals("4915", result.getString(0, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("4851", result.getString(1, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("4840", result.getString(2, result.getColumnByName("FULL_IDENTIFIER")));
		assertEquals("4850", result.getString(3, result.getColumnByName("FULL_IDENTIFIER")));
	}

	@Test
	public void noModelSpecified() throws Exception {
		FindReachRequest req = new FindReachRequest();

		//req.modelID = TEST_MODEL_ID.toString();
		req.reachName = "app";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(7);

		DataTable result = action.run();

		assertNull(result);
		assertEquals(1, action.getValidationErrors().length);

	}

	@Test
	public void noCriteriaSpecified() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		//req.reachName = "app";
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(7);

		DataTable result = action.run();

		assertNull(result);
		assertEquals(1, action.getValidationErrors().length);

	}

	@Test
	public void badReachId() throws Exception {
		FindReachRequest req = new FindReachRequest();

		req.modelID = TEST_MODEL_ID.toString();
		req.reachIDs = "81163 9876";	//commas required for separation
		FindReaches action = new FindReaches();
		action.setReachRequest(req);
		action.setPageSize(7);

		DataTable result = action.run();

		assertNull(result);
		assertEquals(1, action.getValidationErrors().length);

	}
}
