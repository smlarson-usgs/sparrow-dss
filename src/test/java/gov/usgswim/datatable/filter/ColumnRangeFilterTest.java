package gov.usgswim.datatable.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

import org.junit.Before;
import org.junit.Test;

public class ColumnRangeFilterTest {

    public static int[][] DATA = new int[][] {{}};

    private DataTable table;

    @Before
    public void before() {
        table = new SimpleDataTableWritable(DATA, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void colOutOfBoundsLow() {
        new ColumnRangeFilter(-1, 1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void colCountInvalidLow() {
        new ColumnRangeFilter(0, -1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void colCountInvalidZero() {
        new ColumnRangeFilter(0, 0);
    }

    @Test public void matchesCol() {
        ColumnRangeFilter filter = new ColumnRangeFilter(1, 3);
        assertTrue("Column falls within range", filter.accept(table, 2));
    }

    @Test public void matchesStartCol() {
        ColumnRangeFilter filter = new ColumnRangeFilter(1, 3);
        assertTrue("Column matches start column", filter.accept(table, 1));
    }

    @Test public void matchesColCount() {
        // Matches index calculated by startCol + colCount - 1
        ColumnRangeFilter filter = new ColumnRangeFilter(1, 3);
        assertTrue("Column matches col count index", filter.accept(table, 3));
    }

    @Test public void notMatchesBeforeStartCol() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 3);
        assertFalse("Column index does not match before range", filter.accept(table, 1));
    }

    @Test public void notMatchesAfterColCount() {
        ColumnRangeFilter filter = new ColumnRangeFilter(0, 2);
        assertFalse("Column index does not match after range", filter.accept(table, 2));
        assertFalse("Column index does not match after range", filter.accept(table, 3));
    }
}
