package gov.usgs.cida.datatable;

import gov.usgswim.ImmutableBuilder;



public interface DataTableSetWritable<D extends DataTable> extends DataTableSet<D> {

	/**
	 * @param table
	 * @return this if successful, null if not be added
	 */
	public DataTableSetWritable addTable(D table);
	
	
	/**
	 * Removes the specified table.
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public D removeTable(int index) throws IndexOutOfBoundsException;
	
	
	/**
	 * Sets the table at the specified index.
	 * Replaces an existing table or tacks a column to the end.
	 * Missing tables are not allowed, thus the specified zero based index
	 * must be within the existing set of tables 1+ the last index, resulting
	 * in the same behavior as addTable().
	 * @param The table to add or replace
	 * @return the replaced table if the index is an existing table, or null if adding.
	 */
	public D setTable(D column, int columnIndex);
	
	
	// -----------------------------
	// METAINFO MODIFICATION METHODS
	// -----------------------------
	
	public void setDescription(String desc);
	public void setName(String name);
	
}

