package gov.usgs.cida.datatable.adjustment;

import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SparseOverrideAdjustmentTest {
	
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

    @Test
	public void testSOAConstructorWithIntegerData() {
		DataTable intFt = new SimpleDataTableWritable(INT_DATA, null);
		
		SparseOverrideAdjustment dt = new SparseOverrideAdjustment(intFt);
		dt.adjust(2,3, 5d);
		dt.adjust(4,3, 2d);
		dt.adjust(1,2, -2d);
		dt.adjust(3,0, -3d);
		
		doSOAConstructorWithIntegerData(dt);
		doSOAConstructorWithIntegerData(dt.toImmutable());
	}
    
    private void doSOAConstructorWithIntegerData(DataTable dt) {
		// normal unadjusted cells
		assertEquals(Integer.valueOf(1), dt.getInt(0,0));
		assertEquals(Integer.valueOf(12), dt.getInt(1,1));
		assertEquals(Integer.valueOf(23), dt.getInt(2,2));
		assertEquals(Integer.valueOf(34), dt.getInt(3,3));
		//assertEquals(Integer.valueOf(44), dt.getInt(4,3));
		
		// adjusted cells
		assertEquals(Integer.valueOf(5), dt.getInt(2,3));
		assertEquals(Integer.valueOf(2), dt.getInt(4,3));
		assertEquals(Integer.valueOf(-2), dt.getInt(1,2));
		assertEquals(Integer.valueOf(-3), dt.getInt(3,0));
		
		//Try fetching values in different types
		assertEquals("5.0", dt.getString(2,3));
		assertEquals(5, dt.getDouble(2, 3).intValue());
		assertEquals(5F, dt.getFloat(2, 3).floatValue(), .0000f);
		assertEquals(5L, dt.getLong(2, 3).longValue());
		assertEquals(5d, dt.getValue(2, 3));
    }
	
    @Test
	public void testSOAConstructorWithDoubleData() {
		DataTable intFt = new SimpleDataTableWritable(DOUBLE_DATA, null);
		
		SparseOverrideAdjustment dt = new SparseOverrideAdjustment(intFt);
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
		assertEquals(Double.valueOf(5), dt.getDouble(2,3));
		assertEquals(Double.valueOf(2), dt.getDouble(4,3));
		assertEquals(Double.valueOf(-2), dt.getDouble(1,2));
		assertEquals(Double.valueOf(-3), dt.getDouble(3,0));

	}
	
    
    /**
     * The point of this test is to validate the sparse override hashing
     * algorythem.  Previously SO had used a very slow string hashing implementation.
     * I decided to speed that up by swithcing to a long key w/ the row
     * 'slid' over to the high bit positions and the column in the lower
     * bit positions.
     * 
     * The assumptions here is that if I can recover the row and column value
     * back out of the has value for the extreme row & column values, I must be
     * creating a unique hash value.
     */
    @Test
	public void validateDigitSlidingForHash() {
		int row = Integer.MAX_VALUE;
		int col = Integer.MAX_VALUE;
		
		long hashKey = SparseOverrideAdjustment.key(row, col);
		long foundRow = SparseOverrideAdjustment.decodeRow(hashKey);
		long foundCol = SparseOverrideAdjustment.decodeCol(hashKey);
		assertEquals(row, foundRow);
		assertEquals(col, foundCol);
		
		//
		//Now try for zeros
		//
		row = 0;
		col = 0;
		
		hashKey = SparseOverrideAdjustment.key(row, col);
		foundRow = SparseOverrideAdjustment.decodeRow(hashKey);
		foundCol = SparseOverrideAdjustment.decodeCol(hashKey);
		assertEquals(row, foundRow);
		assertEquals(col, foundCol);
		
		//
		//And some other combos...
		//
		row = Integer.MAX_VALUE;
		col = 0;
		
		hashKey = SparseOverrideAdjustment.key(row, col);
		foundRow = SparseOverrideAdjustment.decodeRow(hashKey);
		foundCol = SparseOverrideAdjustment.decodeCol(hashKey);
		assertEquals(row, foundRow);
		assertEquals(col, foundCol);
		
		//
		//And some other combos...
		//
		row = 0;
		col = Integer.MAX_VALUE;
		
		hashKey = SparseOverrideAdjustment.key(row, col);
		foundRow = SparseOverrideAdjustment.decodeRow(hashKey);
		foundCol = SparseOverrideAdjustment.decodeCol(hashKey);
		assertEquals(row, foundRow);
		assertEquals(col, foundCol);
		
		//
		//And some other combos...
		//
		row = 1;
		col = Integer.MAX_VALUE;
		
		hashKey = SparseOverrideAdjustment.key(row, col);
		foundRow = SparseOverrideAdjustment.decodeRow(hashKey);
		foundCol = SparseOverrideAdjustment.decodeCol(hashKey);
		assertEquals(row, foundRow);
		assertEquals(col, foundCol);
		
		//
		//And some other combos...
		//
		row = Integer.MAX_VALUE;
		col = 1;
		
		hashKey = SparseOverrideAdjustment.key(row, col);
		foundRow = SparseOverrideAdjustment.decodeRow(hashKey);
		foundCol = SparseOverrideAdjustment.decodeCol(hashKey);
		assertEquals(row, foundRow);
		assertEquals(col, foundCol);
		
	}
	
}
