package gov.usgswim.datatable;

import gov.usgswim.ImmutableBuilder;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


/**
 * DataTable is an interface for two-dimensional data, conceptualized as an ordered
 * list of columns. For thread safety, all methods in the interface are
 * read-only and do not alter state.
 *
 * @author ilinkuo
 *
 */
public interface DataTable extends ImmutableBuilder<DataTable.Immutable>, Serializable {

	public static interface Immutable extends DataTable{/* Just a marker interface */};

	// =================
	// "TABLE"-WIDE INFO (used to be MetaInfo)
	// =================

	/**
	 * Returns a name for the DataTable, or null if none is provided.
	 *
	 * @return The name of the DataTable
	 */
	public String getName();

	/**
	 * Returns a description of the DataTable, or null if none is provided.
	 *
	 * @return A description of the DataTable
	 */
	public String getDescription();

	/**
	 * Returns the number of columns in the DataTable.
	 * @return
	 */
	public int getColumnCount();

	/**
	 * Returns the number of rows in the DataTable.
	 * @return
	 */
	public int getRowCount();

	/**
	 * Returns true if the DataTable has business logic supplied row IDs.
	 * These IDs must be unique and provide a fast way to look up data.
	 *
	 * @return
	 */
	public boolean hasRowIds();

	/**
	 * Returns the table property for the specified name.
	 *
	 * The named properties for the table are distinct from the named
	 * properties for each column.
	 *
	 * @param name The case-sensitive name of the property
	 * @return The property value
	 */
	public String getProperty(String name);

	/**
	 * Returns all the property names for this table.
	 *
	 * The named properties for the table are distinct from the named
	 * properties for each column.
	 *
	 * @return A detached (changes are not written back) or immutable set of property names
	 */
	public Set<String> getPropertyNames();
	
	/**
	 * Returns a copy of the current table properties.
	 * Changes to these properties will not affect this instance.
	 * @return A detached copy of the properties.
	 */
	public Map<String, String> getProperties();
	
	/**
	 * Returns a copy of the properties for the specified column.
	 * Changes to these properties will not affect the base column instance.
	 * @param col The column to fetch properties for.
	 * @return A detached copy of the properties.
	 */
	public Map<String, String> getProperties(int col);

	// -----------
	// COLUMN INFO
	// -----------

	/**
	 * Returns a name for the specified column, or null if none is provided.
	 *
	 * @param col
	 * @return column name
	 */
	public String getName(int col);

	/**
	 * Returns a description for the specified column, or null if none is provided.
	 *
	 * @param col
	 * @return Column description
	 */
	public String getDescription(int col);


	/**
	 * Returns the units for the column or null if none is specified.
	 *
	 * @param col
	 * @return column units
	 */
	public String getUnits(int col);

	/**
	 * Returns a Class indicating the storage datatype of the column.
	 *
	 * @param col
	 * @return column data type
	 */
	public Class<?> getDataType(int col);

	/**
	 * Returns the property for the specified column and property name.
	 *
	 * The named properties for each column are distinct from the named
	 * properties for the DataTable instance.

	 * @param col The zero based column index
	 * @param name The case-sensitive name of the property
	 * @return column-level property value
	 */
	public String getProperty(int col, String name);

	/**
	 * Returns all the property names for the specified column
	 *
	 * The named properties for each column are distinct from the named
	 * properties for the DataTable instance.
	 *
	 * @param col The zero based column index
	 * @return A detached (changes are not written back) or immutable set of property names
	 * TODO [IK] verify propertyNames are/should be detached.
	 */
	public Set<String> getPropertyNames(int col);

	/**
	 * Returns a reference to an individual column.
	 * 
	 * For immutable tables, this will always be an immutable column.  For
	 * mutable tables, this may be a mutable column, depending on the implementation.
	 * 
	 * @param colIndex
	 * @return
	 * @throws UnsupportedOperationException If the table does not have columns
	 * 	to store the data.  This may be the case for most views.
	 */
	public ColumnData getColumn(int colIndex) throws UnsupportedOperationException;
	
	/**
	 * Returns the zero based column index based on the column name.
	 *
	 * Returns null if the column name is not found.
	 * The column name is the same value returned by <code>getName(int)</code>.
	 *
	 * @param name The case sensitive column name
	 * @return the zero based index of the column
	 */
	public Integer getColumnByName(String name);


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
	public DataTable.Immutable toImmutable();

	/**
	 * An instance may be created in an invalid manner (such as mismatched
	 * column sizes). An instance may be rendered invalid when toImmutable() is
	 * invoked, possibly destroying the original. An instance may be invalidated
	 * for other reasons, depending on the implementation class.
	 *
	 * @return
	 */
	public boolean isValid();
	
	/**
	 * An instance may be rendered invalid when toImmutable() is
	 * invoked, possibly destroying the original. An instance may be invalidated
	 * for other reasons, depending on the implementation class.
	 *
	 * @param columnIndex The column to determine validity on
	 * @return
	 */
	public boolean isValid(int columnIndex);

	// --------------------
	// INDEX & FIND METHODS
	// --------------------
	/**
	 * Returns true if indexing is done for the find methods on the specified column.
	 *
	 * @param column
	 * @return true if the column is indexed
	 */
	public boolean isIndexed(int col);

	/**
	 * Returns a business ID for the row, which must be unique.
	 *
	 * Row IDs are not required, so this method may return null.
	 *
	 * @param row The zero based row number
	 * @return Business ID for the row.
	 */
	public Long getIdForRow(int row);

	/**
	 * Returns a zero based row number for the specified business id.
	 *
	 * Not all DataTable's have row IDs, so this may return -1 to indicate
	 * the row ID was not found.
	 *
	 * @param id as supplied by business logic
	 * @return Zero based row number or -1 if the row ID was not found.
	 */
	int getRowForId(Long id);

	/**
	 * Returns the first row containing the specified value  or -1 if the value is not found.
	 *
	 * @param col The column to search.
	 * @param value The value to search for.
	 * @return A zero based row number or -1 if not found
	 */
	public int findFirst(int col, Object value);

	/**
	 * Returns the last row containing the specified value  or -1 if the value is not found.
	 *
	 * @param col The column to search.
	 * @param value The value to search for.
	 * @return A zero based row number or -1 if not found
	 */
	public int findLast(int col, Object value);

	/**
	 * Returns all the rows containing the specified value  or an empty list if the value is not found.
	 *
	 * @param col The column to search.
	 * @param value The value to search for.
	 * @return A list of zero based row numbers or an empty list if the value is not found
	 */
	public int[] findAll(int col, Object value);

	// ----------------------------
	// Cell Value Retrieval Methods
	// ----------------------------
	/**
	 * @param row
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Double getDouble(int row, int col);
	/**
	 * @param row
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Integer getInt(int row, int col);
	/**
	 * @param row
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Float getFloat(int row, int col);
	/**
	 * @param row
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Long getLong(int row, int col);

	public String getString(int row, int col);
	public Object getValue(int row, int col);

	// -----------------------
	// Cell Statistics Methods
	// -----------------------
	/**
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Double getMaxDouble(int col);
	public Double getMaxDouble();

	/**
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Double getMinDouble(int col);
	public Double getMinDouble();

	/**
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Integer getMaxInt(int col);
	public Integer getMaxInt();

	/**
	 * @param col
	 * @return null if underlying data is not Number type
	 */
	public Integer getMinInt(int col);
	public Integer getMinInt();

}
