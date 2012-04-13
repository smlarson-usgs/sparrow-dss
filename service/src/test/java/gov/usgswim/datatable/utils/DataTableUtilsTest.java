package gov.usgswim.datatable.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class DataTableUtilsTest {

	public static final String TABLE_DATA_WITH_HEADERS = "gov/usgswim/datatable/utils/tableDataWithHeaders.txt";
	public static final String STD_TEST_TABLE = "gov/usgswim/datatable/utils/standardTestFile.txt";
	
	private static boolean DataTableUtilsPreviousFailSlentlyState;

	@BeforeClass
	public static void setupTest() {
		// Don't output a whole bunch of shit to the console while running this test
		DataTableUtilsPreviousFailSlentlyState = DataTableUtils.FAIL_SILENTLY;
		DataTableUtils.FAIL_SILENTLY = true;
	}

	@AfterClass
	public static void cleanupTest() {
		DataTableUtils.FAIL_SILENTLY = DataTableUtilsPreviousFailSlentlyState;
	}

	@Test public void testExtractSortedValues() {
		float[][] data = new float[5][1];
		data[0][0] = 3.1f;
		data[1][0] = 2.1f;
		data[2][0] = 1.1f;
		data[3][0] = 4.1f;
		data[4][0] = 3.5f;

		DataTableWritable table = DataTableConverter.toDataTable(data);
		Float[] sorted = DataTableSorter.extractSortedFilteredValues(table, 0, false, null);

		assertEquals(1.1f, sorted[0], .0001);
		assertEquals(2.1f, sorted[1], .0001);
		assertEquals(3.1f, sorted[2], .0001);
		assertEquals(3.5f, sorted[3], .0001);
		assertEquals(4.1f, sorted[4], .0001);
	}

	@Test public void testFill_UsingBufferStream() throws IOException {
		SimpleDataTableWritable table = configureTableDataWithHeaders();

		InputStream stream = null;
		DataTableWritable result = null;
		try {
			stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TABLE_DATA_WITH_HEADERS);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			// configure columns
			result = DataTableUtils.fill(table, in, false, "\t", true);

			//DataTableUtils.printDataTable(result, null);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		assertNotNull(result);

		// Check the metadata
		assertNotNull("first", result.getColumnByName("first"));
		assertNotNull("first", result.getName(0));
		assertEquals(3, result.getColumnCount());
		assertEquals(31, result.getRowCount());

		// Check the values in the first row
		assertEquals("A", result.getString(0, 0));
		assertEquals("A", result.getValue(0, 0));
		assertEquals(Integer.valueOf(2), result.getInt(0, 1));
		assertEquals(Integer.valueOf(2), result.getValue(0, 1));
		assertEquals(Float.valueOf(2.3f), result.getFloat(0, 2));
		assertEquals(Float.valueOf(2.3f), result.getValue(0, 2));
		
		//In this row, col 1 is empty
		assertEquals("N", result.getString(13, 0));
		assertEquals(Integer.valueOf(0), result.getInt(13, 1));
		assertEquals(Float.valueOf(2.3f), result.getFloat(13, 2));
		
		//In this row, col 2 is empty
		assertEquals("O", result.getString(14, 0));
		assertEquals(Integer.valueOf(1), result.getInt(14, 1));
		assertEquals(Float.valueOf(0f), result.getFloat(14, 2));
	}
	
	@Test public void testFill_UsingFirstColumnAsRowId() throws IOException {

		InputStream stream = null;
		DataTableWritable result = new SimpleDataTableWritable();
		
		//Create columns for the data
		for (int i=0; i<8; i++) {
			ColumnDataWritable column = new StandardStringColumnDataWritable(Integer.toString(i), null);
			result.addColumn(column);
		}
		
		try {
			stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(STD_TEST_TABLE);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			DataTableUtils.fill(result, in, true, "\t", true);

			//DataTableUtils.printDataTable(result, null);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		assertNotNull(result);

		// Check the metadata
		assertEquals(new Integer(0), result.getColumnByName("0"));
		assertEquals(8, result.getColumnCount());
		assertEquals(15, result.getRowCount());
	}

	@Test public void testFill_UsingResource() {
		SimpleDataTableWritable table = configureTableDataWithHeaders();

		DataTableWritable result = null;
		result = DataTableUtils.fill(table, TABLE_DATA_WITH_HEADERS, false, "\t", true);

		assertNotNull(result);

		// Check the metadata
		assertNotNull("first", result.getColumnByName("first"));
		assertNotNull("first", result.getName(0));
		assertEquals(3, result.getColumnCount());
		assertEquals(31, result.getRowCount());

		// Check the values in the first row
		assertEquals("A", result.getString(0, 0));
		assertEquals("A", result.getValue(0, 0));
		assertEquals(Integer.valueOf(2), result.getInt(0, 1));
		assertEquals(Integer.valueOf(2), result.getValue(0, 1));
		assertEquals(Float.valueOf(2.3f), result.getFloat(0, 2));
		assertEquals(Float.valueOf(2.3f), result.getValue(0, 2));
	}

	@Test public void testCloneBaseStructure() {
		SimpleDataTableWritable original = createTestDataTable();
		DataTableWritable clone = DataTableUtils.cloneBaseStructure(original);

		// Now the real test begins
		// Go through each column and check equality of column name, unit, description, and type.
		// This section of code works the same as DataTableUtils.CompareColumnStructure()
		// but we're testing that later.
		assertEquals(original.getColumnCount(), clone.getColumnCount());
		for (int i=0; i<original.getColumnCount(); i++) {
			assertEquals(original.getName(i), clone.getName(i));
			assertEquals(original.getUnits(i), clone.getUnits(i));
			assertEquals(original.getDescription(i), clone.getDescription(i));

			Class<?> origType = null, cloneType = null;
			try {
				origType = original.getDataType(i);
			} catch (Exception e) { /* ignore type not yet defined exception*/ }

			try {
				cloneType = clone.getDataType(i);
			} catch (Exception e) { /* ignore type not yet defined exception*/ }

			assertEquals(origType, cloneType);
		}
	}

	@Test public void testCompareColumnStructure() {
		SimpleDataTableWritable original = createTestDataTable();
		DataTableWritable clone = DataTableUtils.cloneBaseStructure(original);

		List<String> comparisonResult = DataTableUtils.compareColumnStructure(original, clone);
		assertTrue("Errors are not expected", comparisonResult.size() == 0);
	}

	@Test public void testCompareColumnStructure_ShouldFailWithExtraOrigColumn() {
		SimpleDataTableWritable original = createTestDataTable();
		DataTableWritable clone = DataTableUtils.cloneBaseStructure(original);
		// Add an extra column to the original
		original.addColumn(new StandardStringColumnDataWritable("bogus", null));

		List<String> comparisonResult = DataTableUtils.compareColumnStructure(original, clone);

		assertTrue("1 error expected", comparisonResult.size() == 1);
	}

	@Test public void testCompareColumnStructure_ShouldFailWithExtraCloneColumn() {
		SimpleDataTableWritable original = createTestDataTable();
		DataTableWritable clone = DataTableUtils.cloneBaseStructure(original);
		// Add an extra column to the clone
		clone.addColumn(new StandardStringColumnDataWritable("bogus", null));

		List<String> comparisonResult = DataTableUtils.compareColumnStructure(original, clone);

		assertTrue("1 error expected", comparisonResult.size() == 1);
	}

	@Test public void testCompareColumnStructure_ShouldFailWithNamesChanged() {
		SimpleDataTableWritable original = createTestDataTable();
		DataTableWritable clone = DataTableUtils.cloneBaseStructure(original);
		// Change the names of two of the columns
		original.setName("fu", 0);
		original.setName("bar", 1);

		List<String> comparisonResult = DataTableUtils.compareColumnStructure(original, clone);

		assertTrue("2 name errors expected", comparisonResult.size() == 2);
	}

	@Test public void testCompareColumnStructure_ShouldFailWithDescriptionsAndUnitsChanged() {
		SimpleDataTableWritable original = createTestDataTable();
		DataTableWritable clone = DataTableUtils.cloneBaseStructure(original);

		StandardStringColumnDataWritable col = new StandardStringColumnDataWritable("borg", "cube");
		// Alter units of two columns
		original.setUnits("fu", 0);
		original.setUnits("bar", 2);
		// Alter description of one column. Due to DataTable interface, this can
		// only be done with a ref to the original column
		col.setDescription("resistance is futile");
		original.addColumn(col);
		col = new StandardStringColumnDataWritable("borg", "cube");
		col.setDescription("resistance is possible");
		clone.addColumn(col);

		List<String> comparisonResult = DataTableUtils.compareColumnStructure(original, clone);

		assertTrue("2 units errors and 1 description error expected", comparisonResult.size() == 3);
	}

	@Test public void testGetHeadings() {
		DataTable dt = createTestDataTable();
		String[] headings = DataTableUtils.getHeadings(dt);
		String[] expectedHeadings = {"aString", "number", "Integer", "Double"};
		assertTrue(Arrays.equals(expectedHeadings,headings));
	}
	// =========================
	// Test helper methods
	// =========================
	private SimpleDataTableWritable createTestDataTable() {
		SimpleDataTableWritable testTable = new SimpleDataTableWritable();
		{
			StandardStringColumnDataWritable col = new StandardStringColumnDataWritable("aString", "varchar");
			col.setDescription("string description");
			testTable.addColumn(col);
		}
		{
			StandardNumberColumnDataWritable<Number> col = new StandardNumberColumnDataWritable<Number>("number", "numeric");
			col.setDescription("a nonspecific number");
			testTable.addColumn(col);
		}
		{
			StandardNumberColumnDataWritable<Integer> col = new StandardNumberColumnDataWritable<Integer>("Integer", "numeric");
			col.setDescription("an integer column");
			col.setType(Integer.class);
			testTable.addColumn(col);
		}
		{
			StandardNumberColumnDataWritable<Double> col = new StandardNumberColumnDataWritable<Double>("Double", "numeric");
			// col.setDescription("an integer column"); Try this without a description
			//col.setType(Integer.class); don't set the type for this column to test null handling
			testTable.addColumn(col);
		}
		return testTable;
	}



	public static SimpleDataTableWritable configureTableDataWithHeaders() {
		SimpleDataTableWritable table = new SimpleDataTableWritable();
		// Configure a DataTable with 4 columns, one is a String and the others numeric types
		ColumnDataWritable column = new StandardStringColumnDataWritable("first", null);
		table.addColumn(column);
		StandardNumberColumnDataWritable<Integer> intColumn = new StandardNumberColumnDataWritable<Integer>("second", null);
		intColumn.setType(Integer.class);
		table.addColumn(intColumn);
		StandardNumberColumnDataWritable<Float> floatColumn = new StandardNumberColumnDataWritable<Float>("third", null);
		floatColumn.setType(Float.class);
		table.addColumn(floatColumn);
		return table;
	}
}
