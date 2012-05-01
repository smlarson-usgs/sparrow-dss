package gov.usgswim.datatable;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface ColumnData extends Serializable {

	// ------------
	// Copy Methods
	// ------------
	/**
	 * Returns itself if already immutable, otherwise returns an immutable copy,
	 * destroying/invalidating itself (the original).
	 * 
	 * @return
	 */
	public ColumnData toImmutable();
	
	public boolean isValid();
	
	// ----------------------
	// Metadata Methods
	// ----------------------
	public String getName();
	public String getUnits();
	public Class<?> getDataType();
	public Integer getRowCount();
	public String getProperty(String key);
	public String getDescription();
	public Set<String> getPropertyNames();
	
	/**
	 * Returns a copy of the current properties.
	 * Changes to these properties will not affect this instance.
	 * @return A detached copy of the properties.
	 */
	public Map<String, String> getProperties();
	
	// ----------------------
	// Cell Retrieval Methods
	// ----------------------
	/**
	 * Returns the value at the row location, converted to a string if the
	 * underlying data is not String based.
	 * 
	 * For immutable implementations, this method will throw an OutOfBounds
	 * (or similar) runtime error if the index is beyond the number of rows.
	 * 
	 * For mutable instances, the number of rows is not really defined, so null
	 * may be returned.
	 * 
	 * @param row
	 * @return 
	 */
	public String getString(int row);
	
		/**
	 * Returns the value at the row location, converted to a Double if the
	 * underlying data is not Double based.
	 * 
	 * For immutable implementations, this method will throw an OutOfBounds
	 * (or similar) runtime error if the index is beyond the number of rows.
	 * 
	 * For mutable instances, the number of rows is not really defined, so null
	 * may be returned.
	 * 
	 * @param row
	 * @return 
	 */
	public Double getDouble(int row);
	
		/**
	 * Returns the value at the row location, converted to a Float if the
	 * underlying data is not Float based.
	 * 
	 * For immutable implementations, this method will throw an OutOfBounds
	 * (or similar) runtime error if the index is beyond the number of rows.
	 * 
	 * For mutable instances, the number of rows is not really defined, so null
	 * may be returned.
	 * 
	 * @param row
	 * @return 
	 */
	public Float getFloat(int row);
	
		/**
	 * Returns the value at the row location, converted to a int if the
	 * underlying data is not Int based.
	 * 
	 * For immutable implementations, this method will throw an OutOfBounds
	 * (or similar) runtime error if the index is beyond the number of rows.
	 * 
	 * For mutable instances, the number of rows is not really defined, so null
	 * may be returned.
	 * 
	 * @param row
	 * @return 
	 */
	public Integer getInt(int row);
	
		/**
	 * Returns the value at the row location as the Object type of the underlying
	 * storage.
	 * 
	 * For immutable implementations, this method will throw an OutOfBounds
	 * (or similar) runtime error if the index is beyond the number of rows.
	 * 
	 * For mutable instances, the number of rows is not really defined, so null
	 * may be returned.
	 * 
	 * @param row
	 * @return 
	 */
	public Object getValue(int row);	//TODO:  This should use generics
	
		/**
	 * Returns the value at the row location, converted to a Long if the
	 * underlying data is not Long based.
	 * 
	 * For immutable implementations, this method will throw an OutOfBounds
	 * (or similar) runtime error if the index is beyond the number of rows.
	 * 
	 * For mutable instances, the number of rows is not really defined, so null
	 * may be returned.
	 * 
	 * @param row
	 * @return 
	 */
	public Long getLong(int row);

	
	// --------------------
	// INDEX & FIND METHODS
	// --------------------
	public boolean isIndexed();
	public int findFirst(Object value);
	public int findLast(Object value);
	public int[] findAll(Object value);
	
	// ---------------
	// MIN-MAX METHODS
	// ---------------
	public Double getMaxDouble();
	public Double getMinDouble();
	public Integer getMaxInt();
	public Integer getMinInt();

}
