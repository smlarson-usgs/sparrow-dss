package gov.usgswim.datatable;

import gov.usgswim.ImmutableBuilder;
import gov.usgswim.datatable.impl.DataTableSetCoord;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


/**
 * DataTableSet is an interface for a set of related DataTables.
 * 
 * All methods in the DataTable interface whose parameters refer to columns, 
 * are interpreted as columns going across the entire set of columns.  Thus, it
 * is possible to treat the entire set of tables as one large DataTable.
 * 
 * Name and description are unique to the instance, properties may be supplied
 * by an instance or pulled from the first table in the set.
 *
 * @author eeverman
 *
 */
public interface DataTableSet<D extends DataTable> extends Serializable, DataTable {

	public static interface Immutable extends DataTable.Immutable, DataTableSet<DataTable.Immutable>{/* Just a marker interface */};
	
	//////////
	// getTableXXX properties, related to a specified table in the set, or set properties.
	//////////
	
	/**
	 * Returns the number of tables in the set.
	 * @return 
	 */
	public int getTableCount();
	
	/**
	 * Returns the table at the specified index.
	 * 
	 * @param tableIndex The index of the table
	 * @return The table, or may through a beyond index exception if beyond the available tables.
	 */
	public D getTable(int tableIndex);
	
	/**
	 * Returns a detached array of the current tables.
	 * This method is not intended to be used to edit the columns (it is a newly
	 * copied array), but is intended to allow callers to reassemble new
	 * DataTableSets.
	 * @return A detached array of the tables, ot an empty array if there are no tables.  Never null.
	 */
	public D[] getTables();
	
	
		/**
	 * Returns a name for the specified table, or null if none is provided.
	 *
	 * @param tableIndex
	 * @return The table name
	 */
	public String getTableName(int tableIndex);

	/**
	 * Returns a description for the specified table, or null if none is provided.
	 *
	 * @param tableIndex
	 * @return Table description
	 */
	public String getTableDescription(int tableIndex);


	/**
	 * Returns all the property names for the specified table.
	 *
	 * @param tableIndex The zero based column index
	 * @return A detached (changes are not written back) or immutable set of property names
	 */
	public Set<String> getTablePropertyNames(int tableIndex);
	
	/**
	 * Returns a copy of the properties for the specified table.
	 * Changes to these properties will not affect the base table instance.
	 * @param tableIndex The table to fetch properties for.
	 * @return A detached copy of the properties.
	 */
	public Map<String, String> getTableProperties(int tableIndex);
	
	/**
	 * An instance may be rendered invalid when toImmutable() is
	 * invoked, possibly destroying the original. An instance may be invalidated
	 * for other reasons, depending on the implementation class.
	 *
	 * @param tableIndex The table to determine validity on
	 * @return
	 */
	public boolean isTableValid(int tableIndex);
	
	
	/**
	 * Returns the number of columns in the specified table.
	 * @param tableIndex
	 * @return 
	 */
	public int getTableColumnCount(int tableIndex);
	
	/**
	 * Returns the number of rows in the specified table.
	 * @param tableIndex
	 * @return 
	 */
	public int getTableRowCount(int tableIndex);
	
	
	/**
	 * Returns the coordinates of a requested value, crossing multiple tables
	 * to find the correct column.
	 * 
	 * For instance, if the set if made up to 3 tables of 2 columns each, a
	 * request for a value at row 3, col 5 would return a coordinate of:
	 * Table 2, row 3, col 1 (all values are zero based).
	 * 
	 * @param row
	 * @param col
	 * @return 
	 */
	public DataTableSetCoord getCoord(int row, int col);


	// ------------
	// COPY METHODS
	// ------------
	/**
	 * Returns itself if already immutable, otherwise returns an immutable copy.
	 *
	 * When creating an immutable copy of itself, the instance may destroy
	 * itself and its internal references, causing isValid to return false.
	 *
	 * @return
	 */
	public DataTableSet.Immutable toImmutable();


}
