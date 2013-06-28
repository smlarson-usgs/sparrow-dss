package gov.usgs.cida.datatable.filter;

import gov.usgs.cida.datatable.DataTable;

/**
 * 
 *
 */
public class RowRangeFilter implements RowFilter {
	protected int startRow;
	protected int rowCount;

	public RowRangeFilter(int startRow, int rowCount) {
		if (startRow < 0 || rowCount <= 0) {
			throw new IllegalArgumentException(
					"startRow must be zero or greater.  " +
					"rowCount must be greater than zero.");
		}

			this.startRow = startRow;
			this.rowCount = rowCount;
	}

	public boolean accept(DataTable source, int rowNum) {
			return (rowNum >= startRow) && (rowNum < startRow + rowCount);
	}

	@Override
	public Integer getEstimatedAcceptCount() {
		return rowCount;
	}
}
