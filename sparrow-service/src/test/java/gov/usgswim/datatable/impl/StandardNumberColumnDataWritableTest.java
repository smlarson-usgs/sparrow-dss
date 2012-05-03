package gov.usgswim.datatable.impl;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

public class StandardNumberColumnDataWritableTest {

	@Test
	public void testBasic() {
		StandardNumberColumnDataWritable col = new StandardNumberColumnDataWritable();
		
		//Check values on completely empty column
		assertEquals(0, col.getRowCount().intValue());
		assertNull(col.getDouble(0));
		assertNull(col.getInt(0));
		assertNull(col.getFloat(0));
		assertNull(col.getLong(0));
		assertNull(col.getString(0));
		assertNull(col.getValue(0));
		
		//Set one value, check some values leading up to it
		col.setValue(10D, 4);
		assertEquals(5, col.getRowCount().intValue());
		//Row 0
		assertNull(col.getDouble(0));
		assertNull(col.getInt(0));
		assertNull(col.getFloat(0));
		assertNull(col.getLong(0));
		assertNull(col.getString(0));
		assertNull(col.getValue(0));
		//Row 3
		assertNull(col.getDouble(3));
		assertNull(col.getInt(3));
		assertNull(col.getFloat(3));
		assertNull(col.getLong(3));
		assertNull(col.getString(3));
		assertNull(col.getValue(3));
		//Row 4
		assertEquals(10D, col.getDouble(4), .000000001D);
		assertEquals(10, col.getInt(4).intValue());
		assertEquals(10F, col.getFloat(4).floatValue(), .00001D);
		assertEquals(10L, col.getLong(4).longValue());
		assertEquals("10.0", col.getString(4));
		assertEquals(new Double(10D), col.getValue(4));
	}
	
	
}
