package gov.usgs.cida.datatable;

import java.io.Serializable;

/**
 * Defines a set of IDs that are uniquely associated with rows.
 * 
 * Row IDs are one-to-one:  No duplicate IDs are allowed.
 * 
 * @author eeverman
 */
public interface ColumnIndex extends Serializable {

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
	 * Returns the unique row for the given ID.
	 * 
	 * If the ID is not found, -1 is returned.
	 * 
	 * @param id
	 * @return
	 */
	int getRowForId(long id);
	
	/**
	 * Returns the ID for a row number.  Rows numbers are zero based.
	 * 
	 * A 'no ID implementation' should return the row number back.  i.e., the
	 * row ID is the row number.
	 * 
	 * 
	 * @param row
	 * @return The row number or null if the ID was not found or is beyond the
	 * 		rows in the table.
	 */
	Long getIdForRow(int row);
	
	/**
	 * Return an Immutable version of this instance, or return this if already
	 * immutable.
	 * 
	 * @return
	 */
	ColumnIndex toImmutable();
	
	/**
	 * Returns a mutable copy of this ColumnIndex.
	 * 
	 * If this instance is immutable, a mutable detached copy will be returned.
	 * If this instance is already mutable, this method will return a new
	 * detached copy.
	 * This method must ensure that the copy is detached such that edits made to
	 * the copy will affect each the original (and visa versa if the original
	 * is mutable).
	 * 
	 * @return
	 */
	MutableColumnIndex toMutable();
	
	
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
	 * Returns the max row number for which this index is valid.
	 * 
	 * Note that for mutable implementations, this number may not have much
	 * meaning while the index is being built.  For instance, if the first
	 * ID value is assigned to row 1000, this method would return 1000, but
	 * no ID numbers are assigned for values smaller than 1000.
	 * 
	 * Immutable implementations are generally assumed to be fully populated.
	 * @return
	 */
	int getMaxRowNumber();
	
	/**
	 * Returns true if the index has an entry for every row.
	 * 
	 * This is generally a requirement for immutable instances, since all
	 * current immutable implementations use primatives and cannot indicate a
	 * null, however, other implementations would be possible.
	 * 
	 * Operations that create an immutable may throw an exception if the source
	 * data does not meet this requirement.
	 * @return
	 */
	boolean isFullyPopulated();
	
	
	
}
