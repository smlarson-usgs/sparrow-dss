package gov.usgswim.sparrow.revised.transformers;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.impl.BuilderHelper;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.revised.CalculationResult;
import gov.usgswim.sparrow.revised.transformers.NSDatasetTransformer;

import org.junit.Ignore;
import org.junit.Test;

public class NSDatasetTransformerTest {

	@Test @Ignore
	public void testTransform() {
		fail("Not yet implemented");
	}

	@Test
	public void testTransformDataTable_DataTableInt() {
		SimpleDataTableWritable table = setupSimpleDataTable();
		CalculationResult result = new NSDatasetTransformer().transformDataTable(table, 2);

		assertResultContainsOnlyNSDataSet(result);

		assertEquals(table.getRowCount(), result.nsDataSet.size());
	}

	@Test
	public void testTransformDataTable_CalculationResultAndColumnName() {
		CalculationResult input = new CalculationResult();
		SimpleDataTableWritable table = setupSimpleDataTable();
		input.table = table;
		String columnName = "atm_dep";
		CalculationResult result = new NSDatasetTransformer().transformDataTable(input, "atm_dep");

		assertResultContainsOnlyNSDataSet(result);

		assertEquals(table.getRowCount(), result.nsDataSet.size());
	}

	@Test
	public void testTransformDataTable_CalculationResultAndSource() {
		CalculationResult input = new CalculationResult();
		SimpleDataTableWritable table = setupDataTableWithProperties();


		input.table = table;
		CalculationResult result = new NSDatasetTransformer().transformDataTable(input, "atm_dep");

		assertResultContainsOnlyNSDataSet(result);

		assertEquals(table.getRowCount(), result.nsDataSet.size());
	}

	@Test
	public void testFindColumnByAnyPropertyValue() {
		SimpleDataTableWritable table = setupDataTableWithProperties();
		assertEquals(Integer.valueOf(1), NSDatasetTransformer.findColumnByAnyPropertyValue(table, "atm_dep"));
	}

	@Test @Ignore
	public void testTransformDataColumn() {
		fail("Not yet implemented");
	}


	// ==============
	// HELPER METHODS
	// ==============
	public static SimpleDataTableWritable setupSimpleDataTable() {
		int[][] data= {{1335,1,2,3},{1366,4,5,6},{2110,7,8,9}};
		String[] headings = {"index", "atm_dep", "source", "all"};
		SimpleDataTableWritable table = new SimpleDataTableWritable(data, headings, 0);
		return table;
	}


	public static SimpleDataTableWritable setupDataTableWithProperties() {
		SimpleDataTableWritable table = new SimpleDataTableWritable();
		{	// manipulate the table
			ColumnDataWritable col1 = BuilderHelper.createColWriteable("foo", Integer.class, "kg/yr");
			col1.setValue(1, 0);
			col1.setProperty("source", "point");
			table.addColumn(col1);

			ColumnDataWritable col2 = BuilderHelper.createColWriteable("bar", Integer.class, "kg/yr");
			col2.setValue(1, 0);
			col2.setProperty("source", "atm_dep");
			table.addColumn(col2);

			ColumnDataWritable col3 = BuilderHelper.createColWriteable("camp", Integer.class, "kg/yr");
			col3.setValue(1, 0);
			col3.setProperty("source", "fert");
			table.addColumn(col3);

			table.setRowId(123, 0); // currently, assumes table has rowids set
		}
		return table;
	}

	public static void assertResultContainsOnlyNSDataSet(CalculationResult result) {
		assertNotNull(result);
		assertNull(result.predictData);
		assertNull(result.column);
		assertNull(result.table);
		assertNull(result.weights);
		assertNotNull(result.nsDataSet);
		assertNull(result.prevResult);
	}


}
