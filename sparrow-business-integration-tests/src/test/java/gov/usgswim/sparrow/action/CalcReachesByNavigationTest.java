package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgs.cida.datatable.impl.SparseDoubleColumnData;
import gov.usgs.cida.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.CriteriaRelationType;
import gov.usgswim.sparrow.domain.CriteriaType;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Verifies functionality of CalcReachesByNavigation.
 *
 * @author eeverman
 */
public class CalcReachesByNavigationTest extends SparrowTestBaseWithDB {

	/**
	 * Test to check that reach id  #17142 has this list of reach upstream
	 * of it:
	 * 17142 (the reach itself)
	 * 17143
	 * 17144
	 * 17145
	 * 17146
	 * @throws Exception
	 *
	 */
	@Test
	public void verifyUpstreamList() throws Exception {
		Criteria criteria = new Criteria(TEST_MODEL_ID, CriteriaType.REACH,
				CriteriaRelationType.UPSTREAM, "17142");

		CalcReachesByNetwork action = new CalcReachesByNetwork(criteria);

		long[] reaches = action.run();

		assertEquals(17142, reaches[0]);
		assertEquals(17143, reaches[1]);
		assertEquals(17144, reaches[2]);
		assertEquals(17145, reaches[3]);
		assertEquals(17146, reaches[4]);
		assertEquals(5, reaches.length);
	}

	@Test
	public void invalidCriteriaType() throws Exception {
		Criteria criteria = new Criteria(TEST_MODEL_ID, CriteriaType.HUC2,
				CriteriaRelationType.UPSTREAM, "17142");

		CalcReachesByNetwork action = new CalcReachesByNetwork(criteria);

		long[] reaches = action.run();

		assertNull(reaches);
		assertTrue(0 != action.getValidationErrors().length);
	}

	@Test
	public void invalidCriteriaRelationType() throws Exception {
		Criteria criteria = new Criteria(TEST_MODEL_ID, CriteriaType.REACH,
				CriteriaRelationType.IN, "17142");

		CalcReachesByNetwork action = new CalcReachesByNetwork(criteria);

		long[] reaches = action.run();

		assertNull(reaches);
		assertTrue(0 != action.getValidationErrors().length);
	}

}

