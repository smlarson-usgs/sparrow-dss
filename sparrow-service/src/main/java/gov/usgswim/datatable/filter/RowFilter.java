package gov.usgswim.datatable.filter;

import gov.usgswim.datatable.DataTable;

public interface RowFilter {
    public boolean accept(DataTable table, int rowNum);
		
		/**
		 * Returns an estimated number of rows that will be accepted.
		 * If an estimate is impossible, null may be returned.
		 * @return 
		 */
		public Integer getEstimatedAcceptCount();
}
