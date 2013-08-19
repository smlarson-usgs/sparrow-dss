package gov.usgs.cida.datatable.filter;

import gov.usgs.cida.datatable.DataTable;

public interface RowFilter {
    public boolean accept(DataTable table, int rowNum);
		
		/**
		 * Returns an estimated number of rows that will be accepted.
		 * If an estimate is impossible, null may be returned.
		 * @return 
		 */
		public Integer getEstimatedAcceptCount();
}
