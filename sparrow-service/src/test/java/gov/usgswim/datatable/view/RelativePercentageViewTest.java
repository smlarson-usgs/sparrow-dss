package gov.usgswim.datatable.view;

import gov.usgswim.datatable.impl.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.RelationType;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;
import static org.junit.Assert.*;

public class RelativePercentageViewTest {
		
	public static final double COMP_ERR = .0000000001D;
	
	private static int INT_COL_INDEX = 0;
	private static int FLOAT_COL_INDEX = 1;
	private static int STRING_COL_INDEX = 2;
	private static int LONG_COL_INDEX = 3;
	private static int DOUBLE_COL_INDEX = 4;

	ColumnDataWritable intCol;
	ColumnDataWritable floatCol;
	ColumnDataWritable stringCol;
	ColumnDataWritable longCol;
	ColumnDataWritable doubleCol;
	DataTableWritable builder;
	DataTable data;

	@Before public void setUp() throws Exception {
		// configure the DataTable by adding columns to it
		intCol = new StandardNumberColumnDataWritable<Integer>("ints", "attosec");
		floatCol = new StandardNumberColumnDataWritable<Float>("floats", "eV");
		stringCol = new StandardStringColumnDataWritable("name", null);
		longCol = new StandardNumberColumnDataWritable<Long>("longs", "mass");
		doubleCol = new StandardNumberColumnDataWritable<Double>("doubles", "momentum");

		// set column properties
		intCol.setProperty("units description", "pulse time");
		floatCol.setProperty("constants", "mathematical constants");
		stringCol.setProperty("particle", "fundamental particles");
		longCol.setProperty("a", "1");
		longCol.setProperty("b", "2");
		// no properties for double

		// set the column description
		intCol.setDescription("I'm an int");
		floatCol.setDescription("I'm a float");
		stringCol.setDescription("I'm a String");


		// Configure the columns in the builder
		builder = new SimpleDataTableWritable();
		builder.addColumn(intCol)
			.addColumn(floatCol)
			.addColumn(stringCol)
			.addColumn(longCol)
			.addColumn(doubleCol);

		// Set metadata on the original
		builder.setDescription("This is a Data2D");
		builder.setName("TEST_DATA");
		builder.setProperty("size", "6");
	};



	@Test public void testBasic() {
		populateBuilder();

		RelativePercentageView view = new RelativePercentageView(
					builder,
					3, null, null,
					4, null, null,
					true);
		
		Double BASE_COL_TOTAL = 118D;
		Double BASE_ROW_TOTAL = 196D;
		
		assertEquals(6, view.getColumnCount());
		assertEquals(5, view.getRowCount());
		assertEquals(BASE_COL_TOTAL, view.getBaseColTotal(), COMP_ERR);
		assertEquals(BASE_ROW_TOTAL, view.getBaseRowTotal(), COMP_ERR);
		assertEquals(RelationType.rel_fraction.name(), view.getProperty(5, RelationType.XML_ATTRIB_NAME));
		assertEquals(RelationType.rel_fraction.getFullName(), view.getName(5));
		assertEquals(1, view.getPropertyNames(5).size());
		
		//
		//Check first, last and rel percent columns
		
		//Check all values in column 0
		assertEquals(10, view.getInt(0, 0).intValue());
		assertEquals(14, view.getInt(1, 0).intValue());
		assertEquals(10, view.getInt(2, 0).intValue());
		assertEquals(10, view.getInt(3, 0).intValue());
		assertEquals(10D / BASE_ROW_TOTAL, view.getDouble(4, 0), COMP_ERR);
		
		//Check all values in column 4
		assertEquals(70, view.getInt(0, 4).intValue());
		assertEquals(16, view.getInt(1, 4).intValue());
		assertEquals(16, view.getInt(2, 4).intValue());
		assertEquals(16, view.getInt(3, 4).intValue());
		assertEquals(16D / BASE_ROW_TOTAL, view.getDouble(4, 4), COMP_ERR);
		
		//Check all values in column 5 (all vals are rel percent fraction)
		assertEquals(70D / BASE_COL_TOTAL, view.getDouble(0, 5), COMP_ERR);
		assertEquals(16d / BASE_COL_TOTAL, view.getDouble(1, 5), COMP_ERR);
		assertEquals(16d / BASE_COL_TOTAL, view.getDouble(2, 5), COMP_ERR);
		assertEquals(16d / BASE_COL_TOTAL, view.getDouble(3, 5), COMP_ERR);
		assertNull(view.getDouble(4, 5));	//no valid value here
		assertNull(view.getInt(4, 5));
		assertNull(view.getFloat(4, 5));
		assertNull(view.getLong(4, 5));
		assertNull(view.getString(4, 5));
		//assertNull(view.getValue(4, 5));
		
		
		//
		//Check first, last and rel percent rows
		
		//Check all values in row 0 (The last column was already checked)
		assertEquals(10, view.getInt(0, 0).intValue());
		assertEquals(2, view.getInt(0, 1).intValue());
		assertNull(view.getInt(0, 2));
		assertEquals(18, view.getInt(0, 3).intValue());
		assertEquals(70, view.getInt(0, 4).intValue());
		
		//Check all values in row 3 (The last column was already checked)
		assertEquals(10, view.getInt(3, 0).intValue());
		assertEquals(100, view.getInt(3, 1).intValue());
		assertNull(view.getDouble(3, 2));
		assertNull(view.getInt(3, 2));
		assertNull(view.getFloat(3, 2));
		assertNull(view.getLong(3, 2));
		assertEquals("baryons", view.getString(3, 2));
		assertEquals("baryons", view.getValue(3, 2));
		assertEquals(70, view.getInt(3, 3).intValue());
		assertEquals(16, view.getInt(3, 4).intValue());
		
		//Check all values in row 4 (the rel percent row)
		assertEquals(10D / BASE_ROW_TOTAL, view.getDouble(4, 0), COMP_ERR);
		assertEquals(100D / BASE_ROW_TOTAL, view.getDouble(4, 1), COMP_ERR);
		assertNull(view.getDouble(4, 2));
		assertNull(view.getInt(4, 2));
		assertNull(view.getFloat(4, 2));
		assertNull(view.getLong(4, 2));
		assertNull(view.getString(4, 2));
		assertNull(view.getValue(4, 2));
		assertEquals(70D / BASE_ROW_TOTAL, view.getDouble(4, 3), COMP_ERR);
		assertEquals(16D / BASE_ROW_TOTAL, view.getDouble(4, 4), COMP_ERR);
		//assertNull(view.getDouble(4, 5));	//no valid value here

	}

	// =======================
	// private utility methods
	// =======================
	private void populateBuilder() {
		// set the first row
		builder.setValue(10, 0, INT_COL_INDEX);
		builder.setValue(2f, 0, FLOAT_COL_INDEX);
		builder.setValue("baryons", 0, STRING_COL_INDEX);
		builder.setValue(18L, 0, LONG_COL_INDEX);
		builder.setValue(70D, 0, DOUBLE_COL_INDEX);

		// set the second row
		builder.setValue(14, 1, INT_COL_INDEX);
		builder.setValue(100, 1, FLOAT_COL_INDEX);
		builder.setValue("leptons", 1, STRING_COL_INDEX);
		builder.setValue(70L, 1, LONG_COL_INDEX);
		builder.setValue(16D, 1, DOUBLE_COL_INDEX);

		// set the third row
		builder.setValue(10, 2, INT_COL_INDEX);
		builder.setValue(100f, 2, FLOAT_COL_INDEX);
		builder.setValue("baryons", 2, STRING_COL_INDEX);
		builder.setValue(70L, 2, LONG_COL_INDEX);
		builder.setValue(16D, 2, DOUBLE_COL_INDEX);

		// set the fourth row
		builder.setValue(10, 3, INT_COL_INDEX);
		builder.setValue(100f, 3, FLOAT_COL_INDEX);
		builder.setValue("baryons", 3, STRING_COL_INDEX);
		builder.setValue(70L, 3, LONG_COL_INDEX);
		builder.setValue(16D, 3, DOUBLE_COL_INDEX);
	}


}
