package gov.usgswim.datatable;

/**
 * Defines a set of IDs that are uniquely associated with rows.
 * 
 * Row IDs are one-to-one:  No duplicate IDs are allowed.
 * 
 * @author eeverman
 */
public interface ColumnIndex {

	/**
	 * Returns the unique row for the given ID.
	 * 
	 * If the ID is not found, -1 is returned.
	 * 
	 * @param id
	 * @return
	 */
	int getRowForId(Long id);
	
	/**
	 * Returns the ID for a row number.  Rows numbers are zero based.
	 * 
	 * A 'no ID implementation' should return the row number back.  i.e., the
	 * row ID is the row number.
	 * 
	 * @param row
	 * @return
	 */
	Long getIdForRow(int row);
	
	/**
	 * Return a new instance that is either immutable or contains a copy of the
	 * data.  
	 * 
	 * The intent is that in no way can the returned instance write back to
	 * the original instance.
	 * @return
	 */
	ColumnIndex getDetachedClone();
	
	/**
	 * Return an Immutable version of this instance, or return this if already
	 * immutable.
	 * 
	 * @return
	 */
	ColumnIndex toImmutable();
	
	
	/**
	 * Return true to indicate that there are unique IDs assigned to the row.
	 * 
	 * Return false to indicate that rows have no special IDs.
	 * 
	 * Implementations should treat the row number as the row ID if there are no
	 * row IDs.  i.e., getIdForRow(1) should return 1 and visa versa.
	 * @return
	 */
	boolean hasIds();
	
	/**
	 * Returns true if the index is valid for the specified row number.
	 * Rather than require that the index have a specific configured size (ie,
	 * get row count), this allows the index to not know its max size (useful
	 * for the NoIdsColumnIndex implementation).
	 * 
	 * @return
	 */
	boolean isValidForRowNumber(int rowNumber);
	
	
	
}
