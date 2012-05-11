package gov.usgswim.datatable.view;

import gov.usgswim.datatable.*;
import gov.usgswim.datatable.impl.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;
import static org.junit.Assert.*;

public class TotalingColumnDataViewTest {
		
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
		
		ColumnData intCol = builder.getColumn(INT_COL_INDEX);
		ColumnAttribsBuilder colAttribs = new ColumnAttribsBuilder();
		colAttribs.setName("totCol");
		
		
		//Do we have the correct column?
		assertEquals(4, intCol.getRowCount().intValue());
		assertEquals("ints", intCol.getName());
		assertEquals(10, intCol.getInt(0).intValue());
		assertEquals(10, intCol.getInt(3).intValue());
		
		TotalingColumnDataView view = new TotalingColumnDataView(intCol, colAttribs);
		assertEquals(5, view.getRowCount().intValue());
		assertEquals("totCol", view.getName());
		assertEquals(10, view.getInt(0).intValue());
		assertEquals(10, view.getInt(3).intValue());
		assertEquals(44, view.getInt(4).intValue());	//Total row
		
		//The value 14 can be found
		assertEquals(1, view.findAll(new Integer(14)).length);
		assertEquals(1, view.findAll(new Integer(14))[0]);
		assertEquals(1, view.findFirst(new Integer(14)));
		assertEquals(1, view.findLast(new Integer(14)));
		
		//The value 44 cannot be found in the table
		assertEquals(0, view.findAll(new Integer(44)).length);
		assertEquals(-1, view.findFirst(new Integer(44)));
		assertEquals(-1, view.findLast(new Integer(44)));
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
