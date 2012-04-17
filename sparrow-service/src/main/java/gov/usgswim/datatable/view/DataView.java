package gov.usgswim.datatable.view;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;

public interface DataView extends DataTable {
	public DataTable retrieveBaseTable();

	public DataView retrieveBaseView();

	public DataTableWritable retrieveBaseWritable();

	public boolean isBasedOnView();

	public boolean isBasedOnDataTable();

	public boolean isBasedOnDataTableWritable();
}
