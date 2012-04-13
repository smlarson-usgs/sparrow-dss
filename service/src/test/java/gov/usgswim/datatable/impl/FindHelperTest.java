package gov.usgswim.datatable.impl;

import static org.junit.Assert.*;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;

import org.junit.Before;
import org.junit.Test;

public class FindHelperTest {
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

	@Before
	public void setUp() throws Exception {
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
	
	@Test
	public void findAllInTableViews() {
		populateBuilder();
		
		SparseOverrideAdjustment adj = new SparseOverrideAdjustment(builder);
		
		//Find a missing value should return an empty array
		assertArrayEquals(new int[] {}, adj.findAll(INT_COL_INDEX, 9992342));
		
		//Find values of the same type
		assertArrayEquals(new int[] {0, 2}, adj.findAll(INT_COL_INDEX, 10));
		assertArrayEquals(new int[] {0}, adj.findAll(LONG_COL_INDEX, 1976L));
		assertArrayEquals(new int[] {0}, adj.findAll(FLOAT_COL_INDEX, 2.718f));
		assertArrayEquals(new int[] {0, 3}, adj.findAll(DOUBLE_COL_INDEX, 137.036D));
		
		//Find values of different types on int column
		assertArrayEquals(new int[] {0, 2}, adj.findAll(INT_COL_INDEX, 10));
		assertArrayEquals(new int[] {0, 2}, adj.findAll(INT_COL_INDEX, 10L));
		assertArrayEquals(new int[] {0, 2}, adj.findAll(INT_COL_INDEX, 10F));
		assertArrayEquals(new int[] {0, 2}, adj.findAll(INT_COL_INDEX, 10D));
		
		//Find values of different types on float column
		assertArrayEquals(new int[] {}, adj.findAll(FLOAT_COL_INDEX, (int) 2.718f));
		assertArrayEquals(new int[] {}, adj.findAll(FLOAT_COL_INDEX, (long) 2.718f));
		assertArrayEquals(new int[] {0}, adj.findAll(FLOAT_COL_INDEX, 2.718f));
		//This next one fails b/c the stored 2.718f value is slightly off, while the
		//2.718d value is more precise.
		assertArrayEquals(new int[] {}, adj.findAll(FLOAT_COL_INDEX, 2.718d));
		
		//Find values of different types on long column
		assertArrayEquals(new int[] {0}, adj.findAll(LONG_COL_INDEX, (int) 1976L));
		assertArrayEquals(new int[] {0}, adj.findAll(LONG_COL_INDEX, 1976L));
		assertArrayEquals(new int[] {0}, adj.findAll(LONG_COL_INDEX, 1976F));
		assertArrayEquals(new int[] {0}, adj.findAll(LONG_COL_INDEX, 1976d));
		
		//Find values of different types on double column
		assertArrayEquals(new int[] {}, adj.findAll(DOUBLE_COL_INDEX, (int) 137.036D));
		assertArrayEquals(new int[] {}, adj.findAll(DOUBLE_COL_INDEX, (long) 137.036D));
		//This next one fails b/c of precision again.
		assertArrayEquals(new int[] {}, adj.findAll(DOUBLE_COL_INDEX, (float) 137.036D));
		assertArrayEquals(new int[] {0, 3}, adj.findAll(DOUBLE_COL_INDEX, 137.036D));	
	}
	
	@Test
	public void findFirstInTableViews() {
		populateBuilder();
		
		SparseOverrideAdjustment adj = new SparseOverrideAdjustment(builder);
		
		//Find a missing value should return an empty array
		assertEquals(-1, adj.findFirst(INT_COL_INDEX, 9992342));
		
		//Find values of the same type
		assertEquals(0, adj.findFirst(INT_COL_INDEX, 10));
		assertEquals(0, adj.findFirst(LONG_COL_INDEX, 1976L));
		assertEquals(0, adj.findFirst(FLOAT_COL_INDEX, 2.718f));
		assertEquals(0, adj.findFirst(DOUBLE_COL_INDEX, 137.036D));
		
		//Find values of different types on int column
		assertEquals(0, adj.findFirst(INT_COL_INDEX, 10));
		assertEquals(0, adj.findFirst(INT_COL_INDEX, 10L));
		assertEquals(0, adj.findFirst(INT_COL_INDEX, 10F));
		assertEquals(0, adj.findFirst(INT_COL_INDEX, 10D));
		
		//Find values of different types on float column
		assertEquals(-1, adj.findFirst(FLOAT_COL_INDEX, (int) 2.718f));
		assertEquals(-1, adj.findFirst(FLOAT_COL_INDEX, (long) 2.718f));
		assertEquals(0, adj.findFirst(FLOAT_COL_INDEX, 2.718f));
		//This next one fails b/c the stored 2.718f value is slightly off, while the
		//2.718d value is more precise.
		assertEquals(-1, adj.findFirst(FLOAT_COL_INDEX, 2.718d));
		
		//Find values of different types on long column
		assertEquals(0, adj.findFirst(LONG_COL_INDEX, (int) 1976L));
		assertEquals(0, adj.findFirst(LONG_COL_INDEX, 1976L));
		assertEquals(0, adj.findFirst(LONG_COL_INDEX, 1976F));
		assertEquals(0, adj.findFirst(LONG_COL_INDEX, 1976d));
		
		//Find values of different types on double column
		assertEquals(-1, adj.findFirst(DOUBLE_COL_INDEX, (int) 137.036D));
		assertEquals(-1, adj.findFirst(DOUBLE_COL_INDEX, (long) 137.036D));
		//This next one fails b/c of precision again.
		assertEquals(-1, adj.findFirst(DOUBLE_COL_INDEX, (float) 137.036D));
		assertEquals(0, adj.findFirst(DOUBLE_COL_INDEX, 137.036D));	
	}
	
	@Test
	public void findLastInTableViews() {
		populateBuilder();
		
		SparseOverrideAdjustment adj = new SparseOverrideAdjustment(builder);
		
		//Find a missing value should return an empty array
		assertEquals(-1, adj.findLast(INT_COL_INDEX, 9992342));
		
		//Find values of the same type
		assertEquals(2, adj.findLast(INT_COL_INDEX, 10));
		assertEquals(0, adj.findLast(LONG_COL_INDEX, 1976L));
		assertEquals(0, adj.findLast(FLOAT_COL_INDEX, 2.718f));
		assertEquals(3, adj.findLast(DOUBLE_COL_INDEX, 137.036D));
		
		//Find values of different types on int column
		assertEquals(2, adj.findLast(INT_COL_INDEX, 10));
		assertEquals(2, adj.findLast(INT_COL_INDEX, 10L));
		assertEquals(2, adj.findLast(INT_COL_INDEX, 10F));
		assertEquals(2, adj.findLast(INT_COL_INDEX, 10D));
		
		//Find values of different types on float column
		assertEquals(-1, adj.findLast(FLOAT_COL_INDEX, (int) 2.718f));
		assertEquals(-1, adj.findLast(FLOAT_COL_INDEX, (long) 2.718f));
		assertEquals(0, adj.findLast(FLOAT_COL_INDEX, 2.718f));
		//This next one fails b/c the stored 2.718f value is slightly off, while the
		//2.718d value is more precise.
		assertEquals(-1, adj.findLast(FLOAT_COL_INDEX, 2.718d));
		
		//Find values of different types on long column
		assertEquals(0, adj.findLast(LONG_COL_INDEX, (int) 1976L));
		assertEquals(0, adj.findLast(LONG_COL_INDEX, 1976L));
		assertEquals(0, adj.findLast(LONG_COL_INDEX, 1976F));
		assertEquals(0, adj.findLast(LONG_COL_INDEX, 1976d));
		
		//Find values of different types on double column
		assertEquals(-1, adj.findLast(DOUBLE_COL_INDEX, (int) 137.036D));
		assertEquals(-1, adj.findLast(DOUBLE_COL_INDEX, (long) 137.036D));
		//This next one fails b/c of precision again.
		assertEquals(-1, adj.findLast(DOUBLE_COL_INDEX, (float) 137.036D));
		assertEquals(3, adj.findLast(DOUBLE_COL_INDEX, 137.036D));	
	}
	

	/**
	 * Test no index find - Doubles
	 */
	@Test
	public void testFindAllDoubles() {
		double[] array1 = new double[] {23d, 56d, 93d, 8344.79d, 56d, 23d};
		
		//Find null & number not in list
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(null, array1));
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(99.99d, array1));
		
		//Find 93's
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93L, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93f, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93d, array1));
		
		//Find 23's
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23L, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23f, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23d, array1));
		
		//Find 8344.79.  Note:  A float value is NOT close enough to match a double.
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(8344, array1));
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(8344L, array1));
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(8344.79f, array1));
		assertArrayEquals(new int[] {3}, FindHelper.findAllWithoutIndex(8344.79d, array1));
	}
	
	/**
	 * Test no index find - Doubles
	 */
	@Test
	public void testFindAllLongs() {
		long[] array1 = new long[] {23L, 56L, 93L, 8344L, 56L, 23L};
		
		//Find null & number not in list
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(null, array1));
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(99.99d, array1));
		
		//Find 93's
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93L, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93f, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93d, array1));
		
		//Find 23's
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23L, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23f, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23d, array1));
	}
	
	/**
	 * Test no index find - Integer
	 */
	@Test
	public void testFindAllInts() {
		int[] array1 = new int[] {23, 56, 93, 8344, 56, 23};
		
		//Find null & number not in list
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(null, array1));
		assertArrayEquals(new int[] {}, FindHelper.findAllWithoutIndex(99.99d, array1));
		
		//Find 93's
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93L, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93f, array1));
		assertArrayEquals(new int[] {2}, FindHelper.findAllWithoutIndex(93d, array1));
		
		//Find 23's
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23L, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23f, array1));
		assertArrayEquals(new int[] {0, 5}, FindHelper.findAllWithoutIndex(23d, array1));
	}
	
	// =======================
	// private utility methods
	// =======================
	private void populateBuilder() {
		// set the first row
		builder.setValue(10, 0, INT_COL_INDEX);
		builder.setValue(2.718f, 0, FLOAT_COL_INDEX);
		builder.setValue("baryons", 0, STRING_COL_INDEX);
		builder.setValue(1976L, 0, LONG_COL_INDEX);
		builder.setValue(137.036D, 0, DOUBLE_COL_INDEX);

		// set the second row
		builder.setValue(11, 1, INT_COL_INDEX);
		builder.setValue(3.14f, 1, FLOAT_COL_INDEX);
		builder.setValue("leptons", 1, STRING_COL_INDEX);
		builder.setValue(1776L, 1, LONG_COL_INDEX);
		builder.setValue(1.752D, 1, DOUBLE_COL_INDEX);

		// set the third row
		builder.setValue(10, 2, INT_COL_INDEX);
		builder.setValue(1.618f, 2, FLOAT_COL_INDEX);
		builder.setValue("baryons", 2, STRING_COL_INDEX);
		builder.setValue(2008L, 2, LONG_COL_INDEX);
		builder.setValue(6.022D, 2, DOUBLE_COL_INDEX);

		// set the fourth row
		builder.setValue(11, 3, INT_COL_INDEX);
		builder.setValue(3.14f, 3, FLOAT_COL_INDEX);
		builder.setValue("baryons", 3, STRING_COL_INDEX);
		builder.setValue(1812L, 3, LONG_COL_INDEX);
		builder.setValue(137.036D, 3, DOUBLE_COL_INDEX);
	}
	

}
