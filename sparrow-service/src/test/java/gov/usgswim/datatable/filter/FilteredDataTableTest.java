package gov.usgswim.datatable.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 */
public class FilteredDataTableTest {

    public static int[][] INT_DATA = new int[][] {
        {1,2,3,4},
        {11,12,13,14},
        {21,22,23,24},
        {31,32,33,34},
        {41,42,43,44}
    };

    public static String[] HEADINGS = new String[] { "Zero", "One", "Two", "Three" };
    public static int[] IDS = new int[] { 1000, 1001, 1002, 1003, 1004 };

    private DataTable simpleDataTable;
    private DataTable dataWithHeadingsIds;
    private RowRangeFilter simpleRowFilter;
    private ColumnRangeFilter simpleColumnFilter;

    @Before
    public void before() {
        simpleDataTable = new SimpleDataTableWritable(INT_DATA, null);
        dataWithHeadingsIds = new SimpleDataTableWritable(INT_DATA, HEADINGS, IDS);
        simpleRowFilter = new RowRangeFilter(0, 1);
        simpleColumnFilter = new ColumnRangeFilter(0, 1);
    }

    @Test (expected = NullPointerException.class)
    public void testNullColumnFilter() {
        // Shouldn't be called without col filter, checking for exception
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, simpleRowFilter);
        fdt.buildColumnMap();
    }

    @Test (expected = NullPointerException.class)
    public void testNullRowFilter() {
        // Shouldn't be called without row filter, checking for exception
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, simpleColumnFilter);
        fdt.buildRowMap();
    }

    @Test public void testNoColumnFilter() {
        try {
            new FilteredDataTable(simpleDataTable, simpleRowFilter);
        } catch (Exception e) {
            fail("Did not construct properly without column filter");
        }
    }

    @Test public void testNoRowFilter() {
        try {
            new FilteredDataTable(simpleDataTable, simpleColumnFilter);
        } catch (Exception e) {
            fail("Did not construct properly without row filter");
        }
    }

    @Test public void testNoFilter() {
        try {
            new FilteredDataTable(simpleDataTable, null, null);
        } catch (Exception e) {
            fail("Did not construct properly without filters");
        }
    }

    @Test public void testBuildRowMapOneRow() {
        RowRangeFilter filter = new RowRangeFilter(0, 1);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("One row accepted", 1, fdt.buildRowMap().size());
    }

    @Test public void testBuildRowMapAllRows() {
        RowRangeFilter filter = new RowRangeFilter(0, 5);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("All rows accepted", 5, fdt.buildRowMap().size());
    }

    @Test public void testBuildRowMapFrontLoaded() {
        RowRangeFilter filter = new RowRangeFilter(0, 3);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> rowMap = fdt.buildRowMap();
        assertEquals("First 3 rows accepted", 3, rowMap.size());
        assertEquals("First 3 row indices mapped correctly", 0, (int)rowMap.get(0));
        assertEquals("First 3 row indices mapped correctly", 1, (int)rowMap.get(1));
        assertEquals("First 3 row indices mapped correctly", 2, (int)rowMap.get(2));
    }

    @Test public void testBuildRowMapBackLoaded() {
        RowRangeFilter filter = new RowRangeFilter(2, 3);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> rowMap = fdt.buildRowMap();
        assertEquals("Last 3 rows accepted", 3, rowMap.size());
        assertEquals("Last 3 row indices mapped correctly", 2, (int)rowMap.get(0));
        assertEquals("Last 3 row indices mapped correctly", 3, (int)rowMap.get(1));
        assertEquals("Last 3 row indices mapped correctly", 4, (int)rowMap.get(2));
    }

    @Test public void testBuildRowMapRowCountOutOfBounds() {
        RowRangeFilter filter = new RowRangeFilter(2, 4);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> rowMap = fdt.buildRowMap();
        assertEquals("Row count extending beyond data table size ignored", 3, rowMap.size());
    }

    @Test public void testBuildRowMapStartRowOutOfBounds() {
        RowRangeFilter filter = new RowRangeFilter(5, 1);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> rowMap = fdt.buildRowMap();
        assertEquals("Start row out-of-bounds ignored", 0, rowMap.size());
    }

    @Test public void testBuildColMapOneCol() {
        ColumnRangeFilter filter = new ColumnRangeFilter(0, 1);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("One column accepted", 1, fdt.buildColumnMap().size());
    }

    @Test public void testBuildColMapAllCols() {
        ColumnRangeFilter filter = new ColumnRangeFilter(0, 4);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("All columns accepted", 4, fdt.buildColumnMap().size());
    }

    @Test public void testBuildColMapFrontLoaded() {
        ColumnRangeFilter filter = new ColumnRangeFilter(0, 3);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> colMap = fdt.buildColumnMap();
        assertEquals("First 3 cols accepted", 3, colMap.size());
        assertEquals("First 3 col indices mapped correctly", 0, (int)colMap.get(0));
        assertEquals("First 3 col indices mapped correctly", 1, (int)colMap.get(1));
        assertEquals("First 3 col indices mapped correctly", 2, (int)colMap.get(2));
    }

    @Test public void testBuildColMapBackLoaded() {
        ColumnRangeFilter filter = new ColumnRangeFilter(1, 3);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> colMap = fdt.buildColumnMap();
        assertEquals("Last 3 cols accepted", 3, colMap.size());
        assertEquals("Last 3 col indices mapped correctly", 1, (int)colMap.get(0));
        assertEquals("Last 3 col indices mapped correctly", 2, (int)colMap.get(1));
        assertEquals("Last 3 col indices mapped correctly", 3, (int)colMap.get(2));
    }

    @Test public void testBuildColMapColCountOutOfBounds() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 3);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> colMap = fdt.buildColumnMap();
        assertEquals("Col count extending beyond data table size ignored", 2, colMap.size());
    }

    @Test public void testBuildColMapStartColOutOfBounds() {
        ColumnRangeFilter filter = new ColumnRangeFilter(4, 1);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);

        Map<Integer,Integer> colMap = fdt.buildColumnMap();
        assertEquals("Start col out-of-bounds ignored", 0, colMap.size());
    }

    @Test public void testMapRow() {
        RowRangeFilter filter = new RowRangeFilter(1, 2);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("Row index pulled from row map", 1, (int)fdt.mapR(0));
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testMapRowOob() {
        RowRangeFilter filter = new RowRangeFilter(0, 1);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        fdt.mapR(1);
    }

    @Test public void testMapRowNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, null, null);
        assertEquals("Row index not pulled from map", 0, (int)fdt.mapR(0));
        assertEquals("Row index not pulled from map", 2, (int)fdt.mapR(2));
        assertEquals("Row index not pulled from map", 4, (int)fdt.mapR(4));
    }

    @Test public void testMapRowReverse() {
        RowRangeFilter filter = new RowRangeFilter(1, 3);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("Row index pulled from row map", 2, (int)fdt.mapRowReverse(3));
    }

    @Test public void testMapRowReverseNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, null, null);
        assertEquals("Row index not pulled from map", 0, (int)fdt.mapRowReverse(0));
        assertEquals("Row index not pulled from map", 2, (int)fdt.mapRowReverse(2));
        assertEquals("Row index not pulled from map", 4, (int)fdt.mapRowReverse(4));
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testMapRowReverseOob() {
        RowRangeFilter filter = new RowRangeFilter(0, 5);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        fdt.mapRowReverse(5);
    }

    @Test public void testMapCol() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 2);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("Column index pulled from col map", 2, (int)fdt.mapC(0));
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testMapColOob() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 2);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        fdt.mapC(2);
    }

    @Test public void testMapColNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, null, null);
        assertEquals("Column index not pulled from map", 0, (int)fdt.mapC(0));
        assertEquals("Column index not pulled from map", 2, (int)fdt.mapC(2));
    }

    @Test public void testMapColReverse() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 2);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("Column index pulled from col map", 0, (int)fdt.mapColReverse(2));
    }

    @Test public void testMapColReverseNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, null, null);
        assertEquals("Column index not pulled from map", 0, (int)fdt.mapColReverse(0));
        assertEquals("Column index not pulled from map", 2, (int)fdt.mapColReverse(2));
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testMapColReverseOob() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 2);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        fdt.mapColReverse(4);
    }

    @Test public void testGetColumnByName() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 2);
        FilteredDataTable fdt = new FilteredDataTable(dataWithHeadingsIds, filter);
        assertEquals("Column by name not filtered through map", 0, (int)fdt.getColumnByName("Two"));
    }

    @Test public void testGetColumnByNameNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(dataWithHeadingsIds, null, null);
        assertEquals("Column by name not filtered through map", 2, (int)fdt.getColumnByName("Two"));
    }

    @Test public void testGetColumnCount() {
        ColumnRangeFilter filter = new ColumnRangeFilter(2, 2);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("Column count is 2", 2, fdt.getColumnCount());
    }

    @Test public void testGetColumnCountNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, null, null);
        assertEquals("Column count without filter is 4", 4, fdt.getColumnCount());
    }

    @Test public void testGetRowCount() {
        RowRangeFilter filter = new RowRangeFilter(2, 3);
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, filter);
        assertEquals("Row count is 3", 3, fdt.getRowCount());
    }

    @Test public void testGetRowCountNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(simpleDataTable, null, null);
        assertEquals("Row count without filter is 5", 5, fdt.getRowCount());
    }

    @Test public void testGetRowForId() {
        RowRangeFilter filter = new RowRangeFilter(2, 3);
        FilteredDataTable fdt = new FilteredDataTable(dataWithHeadingsIds, filter);
        assertEquals("", 0, fdt.getRowForId(1002L));
    }

    @Test public void testGetRowForIdNotFound() {
        RowRangeFilter filter = new RowRangeFilter(0, 5);
        FilteredDataTable fdt = new FilteredDataTable(dataWithHeadingsIds, filter);
        assertEquals("", -1, fdt.getRowForId(9999L));
    }

    @Test public void testGetRowForIdNoFilter() {
        FilteredDataTable fdt = new FilteredDataTable(dataWithHeadingsIds, null, null);
        assertEquals("", 1, fdt.getRowForId(1001L));
    }
}
