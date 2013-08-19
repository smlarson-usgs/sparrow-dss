package gov.usgs.cida.datatable.view;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;

public interface DataView extends DataTable {
	public DataTable retrieveBaseTable();

	public DataView retrieveBaseView();

	public DataTableWritable retrieveBaseWritable();

	public boolean isBasedOnView();

	public boolean isBasedOnDataTable();

	public boolean isBasedOnDataTableWritable();
}
