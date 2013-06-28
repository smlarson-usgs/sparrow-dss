package gov.usgs.cida.datatable.adjustment;

import gov.usgs.cida.datatable.adjustment.SparseCoefficientAdjustment;
import org.junit.Test;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import static org.junit.Assert.*;

public class SparseCoefficientAdjustmentTest{
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

    @Test public void testSCAConstructorWithIntegerData() {
		DataTable intFt = new SimpleDataTableWritable(INT_DATA, null);
		
		SparseCoefficientAdjustment dt = new SparseCoefficientAdjustment(intFt);
		dt.adjust(2,3, 5d);
		dt.adjust(4,3, 2d);
		dt.adjust(1,2, -2d);
		dt.adjust(3,0, -3d);
		
		doSCAConstructorWithIntegerData(dt);
		doSCAConstructorWithIntegerData(dt.toImmutable());
	}
    
    private void doSCAConstructorWithIntegerData(DataTable dt) {
		// normal unadjusted cells
		assertEquals(Integer.valueOf(1), dt.getInt(0,0));
		assertEquals(Integer.valueOf(12), dt.getInt(1,1));
		assertEquals(Integer.valueOf(23), dt.getInt(2,2));
		assertEquals(Integer.valueOf(34), dt.getInt(3,3));
		//assertEquals(Integer.valueOf(44), dt.getInt(4,3));
		
		// adjusted cells
		assertEquals(Integer.valueOf(120), dt.getInt(2,3));
		assertEquals(Integer.valueOf(88), dt.getInt(4,3));
		assertEquals(Integer.valueOf(-26), dt.getInt(1,2));
		assertEquals(Integer.valueOf(-93), dt.getInt(3,0));
		
		// adjusted cells w/ alt datatypes
		assertEquals("120.0", dt.getString(2,3));
		assertEquals(120F, dt.getFloat(2,3), .00000F);
		assertEquals(120L, dt.getLong(2,3).longValue());
		assertEquals(120D, dt.getValue(2,3));
    }
	
    @Test public void testSCAConstructorWithDoubleData() {
		DataTable intFt = new SimpleDataTableWritable(DOUBLE_DATA, null);
		
		SparseCoefficientAdjustment dt = new SparseCoefficientAdjustment(intFt);
		dt.adjust(2,3, 5d);
		dt.adjust(4,3, 2d);
		dt.adjust(1,2, -2d);
		dt.adjust(3,0, -3d);
		
		// normal unadjusted cells
		assertEquals(Double.valueOf(.1), dt.getDouble(0,0));
		assertEquals(Double.valueOf(1.2), dt.getDouble(1,1));
		assertEquals(Double.valueOf(2.3), dt.getDouble(2,2));
		assertEquals(Double.valueOf(3.4), dt.getDouble(3,3));
		//assertEquals(Double.valueOf(4.4), dt.getInt(4,3));
		
		// adjusted cells
		assertEquals(Double.valueOf(12.0), dt.getDouble(2,3));
		assertEquals(Double.valueOf(8.8), dt.getDouble(4,3));
		assertEquals(Double.valueOf(-2.6), dt.getDouble(1,2));
		assertEquals(Double.valueOf(-9.3), dt.getDouble(3,0));

	}
}
