package gov.usgs.cida.datatable.filter;

import gov.usgs.cida.datatable.filter.RowRangeFilter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;

import org.junit.Before;
import org.junit.Test;

public class RowRangeFilterTest {
    
    public static int[][] DATA = new int[][] {{}};
    
    private DataTable table;
    
    @Before
    public void before() {
        table = new SimpleDataTableWritable(DATA, null);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void rowOutOfBoundsLow() {
        new RowRangeFilter(-1, 1);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void rowCountInvalidLow() {
        new RowRangeFilter(0, -1);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void rowCountInvalidZero() {
        new RowRangeFilter(0, 0);
    }

    @Test
    public void matchesRow() {
        RowRangeFilter filter = new RowRangeFilter(1, 3);
        assertTrue("Row falls within range", filter.accept(table, 2));
    }
    
    @Test
    public void matchesStartRow() {
        RowRangeFilter filter = new RowRangeFilter(1, 3);
        assertTrue("Row matches start row", filter.accept(table, 1));
    }
    
    @Test
    public void matchesRowCount() {
        // Matches index calculated by startRow + rowCount - 1
        RowRangeFilter filter = new RowRangeFilter(1, 3);
        assertTrue("Row matches row count index", filter.accept(table, 3));
    }
    
    @Test
    public void notMatchesBeforeStartRow() {
        RowRangeFilter filter = new RowRangeFilter(2, 3);
        assertFalse("Row index does not match before range", filter.accept(table, 1));
    }
    
    @Test
    public void notMatchesAfterRowCount() {
        RowRangeFilter filter = new RowRangeFilter(0, 2);
        assertFalse("Row index does not match after range", filter.accept(table, 2));
        assertFalse("Row index does not match after range", filter.accept(table, 3));
    }
}
