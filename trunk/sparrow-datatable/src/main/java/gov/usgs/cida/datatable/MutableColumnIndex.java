package gov.usgs.cida.datatable;

public interface MutableColumnIndex extends ColumnIndex {
	
	/**
	 * Assigns a row id to a specific row
	 * 
	 * @param row
	 * @param id
	 */
	void setRowId(int row, Long id);
	
	/**
	 * Assigns a row id to a specific row
	 * 
	 * @param row
	 * @param id
	 */
	void setRowId(int row, long id);
}
