package gov.usgswim.datatable.filter;

import gov.usgswim.datatable.DataTable;

public interface RowFilter {
    public boolean accept(DataTable table, int rowNum);
}
