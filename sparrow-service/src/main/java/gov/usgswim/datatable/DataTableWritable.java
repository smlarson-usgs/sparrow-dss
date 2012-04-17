package gov.usgswim.datatable;



public interface DataTableWritable extends DataTable {

	// ------------
	// COPY METHODS
	// ------------
//	/**
//	 * Returns a DataTable with just the specified list of columns and the
//	 * specified metaInfo. The original is rendered invalid if it is not an
//	 * immutable DataTable. The MetaInfo object will be copied and modified as
//	 * necessary.
//	 * 
//	 * @param columns
//	 * @return
//	 */
//	public DataTable.Immutable toImmutable(List<Integer> columns);
//
//	public DataTable.Immutable toImmutable(int[] columns);
//	
	// ---------------------
	// BUILD & INDEX METHODS
	// ---------------------
	/**
	 * @param column
	 * @return this if successful, null if column could not be added
	 */
	public DataTableWritable addColumn(ColumnDataWritable column);
	
	
	/**
	 * Removes the specified column.
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public ColumnDataWritable removeColumn(int index) throws IndexOutOfBoundsException;
	
	
	/**
	 * Sets the column at the specified index.
	 * Replaces an existing column or tacks a column to the end.
	 * Missing columns are not allowed, thus the specified zero based index
	 * must be within the existing set of columns 1+ the last index, resulting
	 * in the same behavior as addColumn().
	 * @param The column to add or replace
	 * @return the replaced column if the index is an existing column, or null if adding.
	 */
	public ColumnDataWritable setColumn(ColumnDataWritable column, int columnIndex);
	
	/**
	 * Returns a detatched array of the current columns.
	 * This method is not intended to be used to edit the columns (it is a newly
	 * copied array), but is intended to allow callers to reassemble new
	 * DataTables.
	 * @return A detatched array of the columns.
	 */
	public ColumnDataWritable[] getColumns();
	
	/**
	 * @param row (zero-based)
	 * @param col (zero-based)
	 * @param value (no null values allowed, must be either String, Integer, or Double)
	 */
	public void setValue(String value, int row, int col) throws IndexOutOfBoundsException;
	public void setValue(Number value, int row, int col) throws IndexOutOfBoundsException;
	public void setValue(Object value, int row, int col) throws IndexOutOfBoundsException;
	
	public void setRowId(long id, int row);
	public DataTableWritable buildIndex(int col);
	
	// -----------------------------
	// METAINFO MODIFICATION METHODS
	// -----------------------------
	public String setProperty(String key, String value);
	public void setDescription(String desc);
	public void setName(String name);
	
}

