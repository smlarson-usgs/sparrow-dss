package gov.usgswim.datatable.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StandardLongColumnDataTest {

	@Test
	public void testConstructionFromStandardNumCol() {
		double[] data = {1.1d,2.1d,3.2d,4.1d};
		
		
		//Create a standard number
		StandardNumberColumnDataWritable<Double> numCol = new 
			StandardNumberColumnDataWritable<Double>("My Col", "feet");
		
		for (int r = 0; r < data.length; r++) {
			numCol.setValue(data[r], r);
		}
		
		
		StandardLongColumnData longCol = new StandardLongColumnData(numCol, true, 0);
		
		assertTrue(longCol.isIndexed());
		
		//Check Long values
		assertEquals(1L, longCol.getLong(0).longValue());
		assertEquals(2L, longCol.getLong(1).longValue());
		assertEquals(3L, longCol.getLong(2).longValue());
		assertEquals(4L, longCol.getLong(3).longValue());
		
		//Check value type
		assertTrue(longCol.getValue(0).getClass().equals(Long.class));
		
		
		//Find Long values
		assertEquals(0, longCol.findFirst(1L));
		assertEquals(1, longCol.findLast(2L));
		assertEquals(2, longCol.findFirst(3L));
		assertEquals(3, longCol.findLast(4L));
		
	}
	
}
