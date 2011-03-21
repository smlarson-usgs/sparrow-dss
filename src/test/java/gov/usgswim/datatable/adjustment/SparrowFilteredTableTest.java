package gov.usgswim.datatable.adjustment;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.filter.ColumnRangeFilter;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.datatable.filter.RowRangeFilter;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.utils.DataTableUtils;

import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

public class SparrowFilteredTableTest {

	private static final String UNEQUAL_PRIMITIVE_WRAPPERS = "java does not allow different primitives to be equal";

	static final double DELTA = .00000000000000001d;

	String[] headings = new String[] { "c0", "c1", "c2" };
	int[][] intData =
		new int[][] {
			{ 0, 2, 1 },
			{ 1, 2, 1 },
			{ 2, 4, 1 },
			{ 3, 4, 1 },
			{ 5, 6, 1 },
			{ 4, 6, 1 },
			{ 6, 7, 1 }
	};
	double[][] doubleData =
		new double[][] {
			{ .2d, .3d, .4d },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 }
	};


	@Test public void testBasic1() throws Exception {
		DataTable int2D = new SimpleDataTableWritable(intData, headings);
		DataTable double2D = new SimpleDataTableWritable(doubleData, headings);
		runBasicTestA(int2D, double2D);
		runBasicTestB(int2D);
	}

	@Test public void testBasic2() throws Exception {
		DataTable int2D = new SimpleDataTableWritable(intData, headings);
		DataTable double2D = new SimpleDataTableWritable(doubleData, headings);
		runBasicTestA(int2D, double2D);
		runBasicTestB(int2D);
	}


	public void runBasicTestA(DataTable int2D, DataTable double2D) throws Exception {
		runBasicTestAWithViews(new FilteredDataTable(int2D, new ColumnRangeFilter(0, 2)),new FilteredDataTable(double2D, new ColumnRangeFilter(1, 2)));

		if (int2D instanceof DataTableWritable && double2D instanceof DataTableWritable) {
			runBasicTestAWithViews(
					new FilteredDataTable(int2D, new ColumnRangeFilter(0, 2)),
					new FilteredDataTable(double2D, new ColumnRangeFilter(1, 2)));
		}
	}

	/**
	 * Test assumes these two datasets are passed:
	 * new DataTableView(int2D, 0, 2)
	 * new DataTableView(double2D, 1, 2)
	 * 
	 * @throws Exception
	 */
	public void runBasicTestAWithViews(DataTable int2DView, DataTable double2DView) throws Exception {

		//int2DView
		assertEquals(2, int2DView.getColumnCount());
		assertEquals(7, int2DView.getRowCount());
		assertEquals("c0", int2DView.getName(0));
		assertEquals("c0", DataTableUtils.getHeadings(int2DView)[0]);
		assertEquals("c1", DataTableUtils.getHeadings(int2DView)[1]);

		assertEquals(Integer.valueOf(0), int2DView.getInt(0, 0));
		assertFalse(UNEQUAL_PRIMITIVE_WRAPPERS, Double.valueOf(0).equals(int2DView.getValue(0, 0)));
		assertEquals(Double.valueOf(0), int2DView.getDouble(0, 0));

		assertEquals("c1", int2DView.getName(1));
		assertEquals(Integer.valueOf(2), int2DView.getInt(0, 1));
		assertEquals(Integer.valueOf(2), int2DView.getValue(0, 1));
		assertFalse(UNEQUAL_PRIMITIVE_WRAPPERS, Double.valueOf(2d).equals(int2DView.getValue(0, 1)));
		assertEquals(2d, int2DView.getDouble(0, 1), 0d);

		//--int rows and columns
		int[] row0 = DataTableUtils.getIntRow(int2DView, 0);
		int[] row6 = DataTableUtils.getIntRow(int2DView, 6);
		int[] col0 = DataTableUtils.getIntColumn(int2DView, 0);
		int[] col1 = DataTableUtils.getIntColumn(int2DView, 1);
		assertTrue(Arrays.equals(new int[] {0, 2}, row0));
		assertTrue(Arrays.equals(new int[] {6, 7}, row6));
		assertTrue(Arrays.equals(new int[] {0, 1, 2, 3, 5, 4, 6}, col0));
		assertTrue(Arrays.equals(new int[] {2, 2, 4, 4, 6, 6, 7}, col1));
		//--double rows and columns
		double[] rowd0 = DataTableUtils.getDoubleRow(int2DView,0);
		double[] rowd6 = DataTableUtils.getDoubleRow(int2DView,6);
		double[] cold0 = DataTableUtils.getDoubleColumn(int2DView,0);
		double[] cold1 = DataTableUtils.getDoubleColumn(int2DView,1);
		assertTrue(Arrays.equals(new double[] {0d, 2d}, rowd0));
		assertTrue(Arrays.equals(new double[] {6d, 7d}, rowd6));
		assertTrue(Arrays.equals(new double[] {0d, 1d, 2d, 3d, 5d, 4d, 6d}, cold0));
		assertTrue(Arrays.equals(new double[] {2d, 2d, 4d, 4d, 6d, 6d, 7d}, cold1));

		//
		//double2DView
		assertEquals(2, double2DView.getColumnCount());
		assertEquals(7, double2DView.getRowCount());
		assertEquals("c1", double2DView.getName(0));
		assertEquals("c1", DataTableUtils.getHeadings(double2DView)[0]);
		assertEquals("c2", double2DView.getName(1));

		assertEquals(Integer.valueOf(0), double2DView.getInt(0, 0));
		assertEquals(Double.valueOf(.3d), double2DView.getValue(0, 0));
		assertEquals(.3d, double2DView.getDouble(0, 0), 0d);

		assertFalse(UNEQUAL_PRIMITIVE_WRAPPERS, Double.valueOf(0).equals(double2DView.getInt(0, 1)));
		assertEquals(.4d, double2DView.getValue(0, 1));
		assertEquals(.4d, double2DView.getDouble(0, 1), 0d);
		assertEquals("c2", double2DView.getName(1));

		//--int rows and columns
		row0 = DataTableUtils.getIntRow(double2DView,0);
		row6 = DataTableUtils.getIntRow(double2DView,6);
		col0 = DataTableUtils.getIntColumn(double2DView,0);
		col1 = DataTableUtils.getIntColumn(double2DView,1);
		assertTrue(Arrays.equals(new int[] {0, 0}, row0));
		assertTrue(Arrays.equals(new int[] {0, 0}, row6));
		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0, 0, 0, 0}, col0));
		assertTrue(Arrays.equals(new int[] {0, 0, 0, 0, 0, 0, 0}, col1));
		//--double rows and columns
		rowd0 = DataTableUtils.getDoubleRow(double2DView, 0);
		rowd6 = DataTableUtils.getDoubleRow(double2DView, 6);
		cold0 = DataTableUtils.getDoubleColumn(double2DView, 0);
		cold1 = DataTableUtils.getDoubleColumn(double2DView, 1);
		assertTrue(Arrays.equals(new double[] {.3d, .4d}, rowd0));
		assertTrue(Arrays.equals(new double[] {.3d, .4d}, rowd6));
		assertTrue(Arrays.equals(new double[] {.3d, .3d, .3d, .3d, .3d, .3d, .3d}, cold0));
		assertTrue(Arrays.equals(new double[] {.4d, .4d, .4d, .4d, .4d, .4d, .4d}, cold1));



		//These tests are outside the column bound and should throw errors
		try {
			int2DView.getInt(0, 2);
			fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			double2DView.getInt(0, 2);
			fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
	}

	@Test public void testSet1() throws Exception {

		SimpleDataTableWritable int2D = new SimpleDataTableWritable(intData, headings);
		SimpleDataTableWritable double2D = new SimpleDataTableWritable(doubleData, headings);

		FilteredDataTable int2DView = new FilteredDataTable(int2D, new ColumnRangeFilter(0, 2));
		FilteredDataTable double2DView = new FilteredDataTable(double2D, new ColumnRangeFilter(1, 2));

		//Test Setting values on Int
		int2DView.setValue(new Double(9.9), 0, 0);
		assertFalse("doubles stored as ints are rounded", Double.valueOf(9.9d).equals(int2DView.getDouble(0, 0)));
		assertEquals(9.9d, (int2DView.getDouble(0, 0)), .9001d);
		assertFalse("doubles stored as ints are rounded", Double.valueOf(9.9d).equals(int2D.getDouble(0, 0)));
		assertEquals(9.9d, (int2D.getDouble(0, 0)), .9001d);
		assertEquals(Integer.valueOf(9), int2DView.getInt(0, 0));
		assertEquals(Integer.valueOf(9), int2D.getInt(0, 0));	//test base data

		int2DView.setValue(new Integer(5), 6, 1);
		assertEquals(Integer.valueOf(5), int2DView.getInt(6, 1));
		assertEquals(Double.valueOf(5), int2D.getDouble(6, 1));	//test base data

		try {
			int2DView.setValue(new Integer(5), 0, 2);
			fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			int2DView.setValue(new Integer(5), 7, 1);
			fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		//
		//Test Setting values on Double
		double2DView.setValue(new Double(9.9), 0, 0);
		assertEquals(Double.valueOf(9.9), double2DView.getDouble(0, 0));
		assertEquals(Double.valueOf(9.9), double2D.getDouble(0, 1));	//test base data

	}

	/**
	 * Test row and column trimming
	 * @throws Exception
	 */
	@Test public void testSet2() throws Exception {

		DataTableWritable int2D = new SimpleDataTableWritable(intData, headings);
		DataTableWritable int2DView = new FilteredDataTable(int2D, new RowRangeFilter(1, 5), new ColumnRangeFilter(0, 2));

		//Test Setting values
		int2DView.setValue(new Integer(9), 0, 0);
		assertEquals(Integer.valueOf(9), int2DView.getInt(0, 0));
		assertEquals(Integer.valueOf(9), int2D.getInt(1, 0));


		//These tests are outside the column/row bound and should throw errors
		try {
			int2DView.getInt(5, 0);
			fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

	}


	public void runBasicTestB(DataTable int2D) throws Exception {
		runBasicTestBWithViews(new FilteredDataTable(int2D, new RowRangeFilter(1, 5), new ColumnRangeFilter(0, 2)));

		if (int2D instanceof DataTableWritable) {
			runBasicTestBWithViews(
					new FilteredDataTable((DataTableWritable)int2D, new RowRangeFilter(1, 5), new ColumnRangeFilter(0, 2))
			);
		}
	}


	/**
	 * This test assumes we are given a view as: DataTableView(int2D, 1, 5, 0, 2).
	 * 
	 * @throws Exception
	 */
	public void runBasicTestBWithViews(DataTable int2DView) throws Exception {

		//int2DView
		assertEquals(6d, int2DView.getMaxDouble(), 0d);
		assertEquals(5, int2DView.getRowCount());
		assertEquals("c0", int2DView.getName(0));


		assertEquals(Integer.valueOf(1), int2DView.getInt(0, 0));
		assertFalse(UNEQUAL_PRIMITIVE_WRAPPERS, Double.valueOf(1d).equals(int2DView.getValue(0, 0)));
		assertEquals(1d, int2DView.getDouble(0, 0), 0d);

		assertEquals("c1", int2DView.getName(1));
		assertEquals(Integer.valueOf(4), int2DView.getInt(4, 0));
		assertFalse(UNEQUAL_PRIMITIVE_WRAPPERS, Double.valueOf(4d).equals(int2DView.getValue(4, 0)));
		assertEquals(4d, int2DView.getDouble(4, 0), 0d);

		//These tests are outside the column/row bound and should throw errors
		try {
			int2DView.getInt(5, 0);
			fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

	}

	/**
	 * Test findMax value
	 * @throws Exception
	 */
	@Test public void testBasic3() throws Exception {

		DataTable int2D = new SimpleDataTableWritable(intData, headings);
		DataTable int2DView = new FilteredDataTable(int2D, new RowRangeFilter(1, 5), new ColumnRangeFilter(0, 2));


		assertEquals(6d, int2DView.getMaxDouble(), 0d);
		assertEquals(7d, int2D.getMaxDouble(), 0d);

	}


//	/**
//	 * Expects a view created as:
//	 * file = /gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt
//	 * new DataTableView(DataTablebase, 1, 9, 1, 2, 0);
//	 * ...or similar
//	 * 
//	 * @param DataTable
//	 * @throws Exception
//	 */
//	public void runfindById(DataTable DataTable) throws Exception {
//
//
//		assertEquals(0, DataTable.findRowByIndex(12d));
//		assertEquals(1, DataTable.findRowByIndex(22d));
//		assertEquals(2, DataTable.findRowByIndex(32d));
//		assertEquals(3, DataTable.findRowByIndex(42d));
//		assertEquals(8, DataTable.findRowByIndex(92d));
//
//		//should not be found (-1)
//		assertEquals(-1, DataTable.findRowByIndex(99d));
//
//
//		//
//		// Change some values and make sure we find them.
//		//
//		if (DataTable instanceof DataTableWritable) {
//			DataTableWritable d2dw = (DataTableWritable)DataTable;
//
//			d2dw.setValueAt(new Integer(99), 8, 0);
//			assertEquals(8, d2dw.findRowByIndex(99d));
//
//			d2dw.setValueAt(new Integer(-1), 0, 0);
//			assertEquals(0, d2dw.findRowByIndex(-1d));
//
//			//
//			// Change the index to the 2nd column.
//			//
//			d2dw.setValueAt(new Integer(99), 0, 1);	//update one row b/f changing index
//			d2dw.setIndexColumn(1);
//			assertEquals(0, d2dw.findRowByIndex(99d));
//			assertEquals(1, d2dw.findRowByIndex(23d));
//			assertEquals(2, d2dw.findRowByIndex(33d));
//			assertEquals(3, d2dw.findRowByIndex(43d));
//			assertEquals(8, d2dw.findRowByIndex(93d));
//		}
//	}

}

