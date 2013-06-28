package gov.usgs.cida.datatable.filter;

import gov.usgs.cida.datatable.DataTable;

public interface ColumnFilter {
    public boolean accept(DataTable table, int colNum);
}
