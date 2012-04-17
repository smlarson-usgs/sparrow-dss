package gov.usgswim.datatable.adjustment;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

public class ColumnCoefAdjustmentTest{
    public static int[][] INT_DATA = new int[][] {
        {1,2,3,4},
        {11,12,13,14},
        {21,22,23,24},
        {31,32,33,34},
        {41,42,43,44},
    };
    
    public static double[][] DOUBLE_DATA = new double[][] {
        {.1,.2,.3,.4},
        {1.1,1.2,1.3,1.4},
        {2.1,2.2,2.3,2.4},
        {3.1,3.2,3.3,3.4},
        {4.1,4.2,4.3,4.4},
    };
	
	@Test public void testRCCAConstructorWithIntegerData() {
		DataTable intFt = new SimpleDataTableWritable(INT_DATA, null);
	
		ColumnCoefAdjustment dt = new ColumnCoefAdjustment(intFt);
		dt.setColumnMultiplier(0, 5d);
		
		doRCCAConstructorWithIntegerData(dt);
		doRCCAConstructorWithIntegerData(dt.toImmutable());
	}
	
	private void doRCCAConstructorWithIntegerData(DataTable dt) {
		// normal unadjusted cells
		//assertEquals(arg0, dt.getInt(0,0));
		assertEquals(Integer.valueOf(12), dt.getInt(1,1));
		assertEquals(Integer.valueOf(23), dt.getInt(2,2));
		assertEquals(Integer.valueOf(32), dt.getInt(3,1));
		assertEquals(Integer.valueOf(33), dt.getInt(3,2));
		assertEquals(Integer.valueOf(34), dt.getInt(3,3));
		assertEquals(Integer.valueOf(44), dt.getInt(4,3));
		
		// column multiplier cells
		assertEquals(Integer.valueOf(5), dt.getInt(0,0));
		assertEquals(Integer.valueOf(55), dt.getInt(1,0));
		assertEquals(Integer.valueOf(105), dt.getInt(2,0));
		assertEquals(Integer.valueOf(155), dt.getInt(3,0));
		
		// adjusted cells w/ alt datatypes
		assertEquals("5.0", dt.getString(0, 0));
		assertEquals(5F, dt.getFloat(0,0), .00000F);
		assertEquals(5L, dt.getLong(0,0).longValue());
		assertEquals(5D, dt.getValue(0,0));
	}

	@Test public void testRCCAConstructorWithDoubleData() {
		DataTable intFt = new SimpleDataTableWritable(DOUBLE_DATA, null);
	
		ColumnCoefAdjustment dt = new ColumnCoefAdjustment(intFt);
		dt.setColumnMultiplier(0, 5d);
		
		// normal unadjusted cells
		//assertEquals(arg0, dt.getInt(0,0));
		assertEquals(Double.valueOf(1.2), dt.getDouble(1,1));
		assertEquals(Double.valueOf(2.3), dt.getDouble(2,2));
		assertEquals(Double.valueOf(3.2), dt.getDouble(3,1));
		assertEquals(Double.valueOf(3.3), dt.getDouble(3,2));
		assertEquals(Double.valueOf(3.4), dt.getDouble(3,3));
		assertEquals(Double.valueOf(4.4), dt.getDouble(4,3));
		
		// column multiplier cells
		assertEquals(Double.valueOf(.5), dt.getDouble(0,0));
		assertEquals(Double.valueOf(5.5), dt.getDouble(1,0));
		assertEquals(Double.valueOf(10.5), dt.getDouble(2,0));
		assertEquals(Double.valueOf(15.5), dt.getDouble(3,0));


	}
}
