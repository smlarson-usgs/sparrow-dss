package gov.usgswim.datatable.filter;

import gov.usgswim.datatable.DataTable;

public interface ColumnFilter {
    public boolean accept(DataTable table, int colNum);
}
