package gov.usgswim.sparrow.test;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.Adjustment;
import gov.usgswim.sparrow.AdjustmentSetBuilder;
import gov.usgswim.sparrow.Adjustment.AdjustmentType;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
public class SourceAdjustments_Test extends TestCase {
	DataTable data;

	int[][] rowIndexData =
		new int[][] {
			{ 100, 2, 1 },
			{ 200, 2, 1 },
			{ 300, 4, 1 },
			{ 400, 4, 1 },
			{ 500, 6, 1 },
			{ 600, 6, 1 },
			{ 700, 7, 1 },
	};

	public SourceAdjustments_Test(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		BufferedReader fileStream = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/src.txt")
		));
		data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		data = null;
	}

	/**
	 * @see gov.usgswim.sparrow.AdjustmentSet#setAdjustment(String,String)
	 *
	 * The test file src.txt contains 11 sources.  Here we'll adjust some randomly
	 * and see if the coefficient adjustment matches normal multiplied values.
	 *
	 * Also, we'll set some specific values using a minimal index table into
	 * the the first 10 rows.
	 */

	public void testSetGrossAdjustment() throws Exception {
		//Adjustments per source (source #, coef).
		//Any skipped ones are assumed to be 1.
		String adjustString = "1,.5, 4,.1, 7,1, 8,0, 9,0 10,.5";
		Map<String, String> adjMap = new HashMap<String, String>(11);
		adjMap.put(AdjustmentType.GROSS_SRC_ADJUST.toString(), adjustString);

		AdjustmentSetBuilder sas = new AdjustmentSetBuilder();

		sas.addGrossSrcAdjustments(adjMap);

		Adjustment[] adjList = sas.getAdjustments();

		//Test a few of the adjustment values
		//these values will be sorted by ID, so 10 will be the last ID and .5 the last value
		Adjustment a = adjList[0];
		assertEquals(AdjustmentType.GROSS_SRC_ADJUST, a.getType());
		assertEquals(1, a.getSrcId());
		assertEquals(.5d, a.getValue(), .00000000000001);

		a = adjList[1];
		assertEquals(4, a.getSrcId());
		assertEquals(.1, a.getValue(), .00000000000001);

		a = adjList[4];
		assertEquals(9, a.getSrcId());
		assertEquals(0d, a.getValue(), .00000000000001);

		//Test the whole buisiness
		DataTable adjData = sas.adjust(data, null, null);

		//These two should be different instances (one is a view of hte other)
		assertNotSame(data, adjData);

		//Test a few columns
		//Column 1 (index 0)
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals((data.getDouble(r, 0) * .5d), adjData.getDouble(r, 0), .00000000000001);
		}

		//Column 2 (index 1) - not modified
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals(data.getDouble(r, 1), adjData.getDouble(r, 1), .00000000000001);
		}

		//Column 3 (index 2) - not modified
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals(data.getDouble(r, 2), adjData.getDouble(r, 2), .00000000000001);
		}

		//Column 4 (index 3)
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals((data.getDouble(r, 3) * .1d), adjData.getDouble(r, 3), .00000000000001);
		}

		//Column 8 (index 7) - should be zero
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals(0, adjData.getDouble(r, 7), .00000000000001);
		}

		//Column 10 (index 9)
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals((data.getDouble(r, 9) * .5d), adjData.getDouble(r, 9), .00000000000001);
		}

	}

	public void testSetGrossAndSpecificAdjustment() throws Exception {

		DataTable reachIndex = new SimpleDataTableWritable(rowIndexData, (String[]) null, 0);
		String adjustString = "1,.5, 4,.1, 7,1, 8,0, 9,0 10,.5";
		Map<String, String> adjMap = new HashMap<String, String>(11);
		adjMap.put(AdjustmentType.GROSS_SRC_ADJUST.toString(), adjustString);

		AdjustmentSetBuilder sas = new AdjustmentSetBuilder();

		//Add the adjustments - specific adjs should always sort to last.
		sas.addAdjustment(new Adjustment(Adjustment.AdjustmentType.SPECIFIC_ADJUST, 1, 100, .5));	//(0,0)
		sas.addGrossSrcAdjustments(adjMap);
		sas.addAdjustment(new Adjustment(Adjustment.AdjustmentType.SPECIFIC_ADJUST, 2, 200, 2.5d));	//(1,1)

		Adjustment[] adjList = sas.getAdjustments();

		//Test a few of the adjustment values.  In particular, the specific adjustments
		//must go last.
		Adjustment a = adjList[0];
		assertEquals(AdjustmentType.GROSS_SRC_ADJUST, a.getType());
		assertEquals(1, a.getSrcId());
		assertEquals(.5d, a.getValue(), .00000000000001);

		a = adjList[6];
		assertEquals(AdjustmentType.SPECIFIC_ADJUST, a.getType());
		assertEquals(1, a.getSrcId());
		assertEquals(100, a.getReachId());
		assertEquals(.5d, a.getValue(), .00000000000001);

		a = adjList[7];
		assertEquals(AdjustmentType.SPECIFIC_ADJUST, a.getType());
		assertEquals(2, a.getSrcId());
		assertEquals(200, a.getReachId());
		assertEquals(2.5d, a.getValue(), .00000000000001);

		//Test the whole business
		DataTable adjData = sas.adjust(data, null, reachIndex);


		//Test the specific adj value and a few values on either side
		assertEquals(.5d, adjData.getDouble(0, 0), .00000000000001);	//specific
		assertEquals((data.getDouble(1, 0) * .5d), adjData.getDouble(1, 0), .00000000000001);


		assertEquals(data.getDouble(0, 1), adjData.getDouble(0, 1), .00000000000001);
		assertEquals(2.5d, adjData.getDouble(1, 1), .00000000000001);	//specific
		assertEquals(data.getDouble(2, 1), adjData.getDouble(2, 1), .00000000000001);


		//
		//These column tests are the same as the coef adjust
		//

		//Column 3 (index 2) - not modified
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals(data.getDouble(r, 2), adjData.getDouble(r, 2), .00000000000001);
		}

		//Column 4 (index 3)
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals((data.getDouble(r, 3) * .1d), adjData.getDouble(r, 3), .00000000000001);
		}

		//Column 8 (index 7) - should be zero
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals(0, adjData.getDouble(r, 7), .00000000000001);
		}

		//Column 10 (index 9)
		for (int r = 0; r < data.getColumnCount(); r++)  {
			assertEquals((data.getDouble(r, 9) * .5d), adjData.getDouble(r, 9), .00000000000001);
		}

	}


}
