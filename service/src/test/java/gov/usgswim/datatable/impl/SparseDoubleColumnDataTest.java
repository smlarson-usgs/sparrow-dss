package gov.usgswim.datatable.impl;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class SparseDoubleColumnDataTest {

	@Test
	public void testAsReplacementForStandardColumn() {
		//double[] data = {1.1d,2.1d,3.2d,4.1d};
		HashMap<Integer, Double> data2 = new HashMap<Integer, Double>();
		data2.put(0, 1.1d);
		data2.put(1, 2.1d);
		data2.put(2, 3.2d);
		data2.put(3, 4.1d);
		
		SparseDoubleColumnData column = new
			SparseDoubleColumnData(data2, "myName", "myUnits", "myDesc",
				null, null, 4, -1d); 
		
		assertTrue(column.getDataType().equals(Double.class));
		assertTrue(column.isValid());
		assertEquals("myName", column.getName());
		assertEquals("myUnits", column.getUnits());
		assertEquals("myDesc", column.getDescription());
		assertEquals(false, column.isIndexed());
		assertEquals(4, column.getRowCount().intValue());
		
		assertEquals(4.1d, column.getMaxDouble(), .0000000001d);
		assertEquals(1.1d, column.getMinDouble(), .0000000001d);
		assertEquals(new Integer(4), column.getMaxInt());
		assertEquals(new Integer(1), column.getMinInt());
		
		assertEquals(1.1d, column.getDouble(0), .0000000001d);
		assertEquals(2.1d, column.getDouble(1), .0000000001d);
		assertEquals(3.2d, column.getDouble(2), .0000000001d);
		assertEquals(4.1d, column.getDouble(3), .0000000001d);
		
		assertEquals(0, column.findFirst(1.1d));
		assertEquals(1, column.findFirst(2.1d));
		assertEquals(2, column.findFirst(3.2d));
		assertEquals(3, column.findFirst(4.1d));
	}
	
	@Test
	public void testMissingValue() {
		//double[] data = {1.1d,2.1d,3.2d,4.1d};
		HashMap<Integer, Double> data2 = new HashMap<Integer, Double>();
		data2.put(0, 1.1d);
		//data2.put(1, 2.1d); --removed
		data2.put(2, 3.2d);
		data2.put(3, 4.1d);
		//Also there is an extra row (row 4) b/c we spec 5 rows in constructor.
		
		SparseDoubleColumnData column = new
			SparseDoubleColumnData(data2, "myName", "myUnits", "myDesc",
				null, null, 5, -1d); 
		
		assertTrue(column.getDataType().equals(Double.class));
		assertTrue(column.isValid());
		assertEquals("myName", column.getName());
		assertEquals("myUnits", column.getUnits());
		assertEquals("myDesc", column.getDescription());
		assertEquals(false, column.isIndexed());
		assertEquals(5, column.getRowCount().intValue());
		
		assertEquals(4.1d, column.getMaxDouble(), .0000000001d);
		assertEquals(-1d, column.getMinDouble(), .0000000001d);	//default val is -1
		assertEquals(new Integer(4), column.getMaxInt());
		assertEquals(new Integer(-1), column.getMinInt());
		
		assertEquals(1.1d, column.getDouble(0), .0000000001d);
		assertEquals(-1d, column.getDouble(1), .0000000001d);
		assertEquals(3.2d, column.getDouble(2), .0000000001d);
		assertEquals(4.1d, column.getDouble(3), .0000000001d);
		
		assertEquals(0, column.findFirst(1.1d));
		assertEquals(-1, column.findFirst(2.1d));	//no longer present
		assertEquals(1, column.findFirst(-1d));		//-1 is at the 1 spot (default val)
		assertEquals(2, column.findFirst(3.2d));
		assertEquals(3, column.findFirst(4.1d));
		
		assertEquals(0, column.findLast(1.1d));
		assertEquals(-1, column.findLast(2.1d));	//no longer present
		assertEquals(4, column.findLast(-1d));		//extra row at end w/ default -1
		assertEquals(2, column.findLast(3.2d));
		assertEquals(3, column.findLast(4.1d));
		
		assertArrayEquals(new int[] {1, 4}, column.findAll(-1));
		assertArrayEquals(new int[] {0}, column.findAll(1.1d));
	}
	
}
