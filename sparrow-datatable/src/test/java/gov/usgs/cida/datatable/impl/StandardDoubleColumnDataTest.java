package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.impl.StandardDoubleColumnData;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StandardDoubleColumnDataTest {

	@Test
	public void testArrayConstructor() {
		double[] data = {1.1d,2.1d,3.2d,4.1d};
		
		StandardDoubleColumnData column = new
			StandardDoubleColumnData(data, "myName", "myUnits", "myDesc",
				null, true); 
		
		assertTrue(column.getDataType().equals(Double.class));
		assertTrue(column.isValid());
		assertEquals("myName", column.getName());
		assertEquals("myUnits", column.getUnits());
		assertEquals("myDesc", column.getDescription());
		assertEquals(true, column.isIndexed());
		assertEquals(4, column.getRowCount().intValue());
		
		assertEquals(data[0], column.getDouble(0), .0000000001d);
		assertEquals(data[1], column.getDouble(1), .0000000001d);
		assertEquals(data[2], column.getDouble(2), .0000000001d);
		assertEquals(data[3], column.getDouble(3), .0000000001d);
		
		assertEquals(0, column.findFirst(1.1d));
		assertEquals(1, column.findFirst(2.1d));
		assertEquals(2, column.findFirst(3.2d));
		assertEquals(3, column.findFirst(4.1d));
		
	}
	
	@Test
	public void testConstructionFromStandardNumCol() {
		double[] data = {1.1d,2.1d,3.2d,4.1d};
		
		
		//Create a standard number
		StandardNumberColumnDataWritable<Double> numCol = new 
			StandardNumberColumnDataWritable<Double>("My Col", "feet");
		
		for (int r = 0; r < data.length; r++) {
			numCol.setValue(data[r], r);
		}
		
		
		StandardDoubleColumnData dblCol = new StandardDoubleColumnData(numCol, true, 0);
		
		assertTrue(dblCol.isIndexed());
		
		//Check values
		assertEquals(data[0], dblCol.getDouble(0), .0000000001d);
		assertEquals(data[1], dblCol.getDouble(1), .0000000001d);
		assertEquals(data[2], dblCol.getDouble(2), .0000000001d);
		assertEquals(data[3], dblCol.getDouble(3), .0000000001d);
		
		//Check value type
		assertTrue(dblCol.getValue(0).getClass().equals(Double.class));
		
		
		//Find  values
		assertEquals(0, dblCol.findFirst(1.1d));
		assertEquals(1, dblCol.findFirst(2.1d));
		assertEquals(2, dblCol.findFirst(3.2d));
		assertEquals(3, dblCol.findFirst(4.1d));
		
	}
	
}
