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
	public String getString(int row);
	public Double getDouble(int row);
	public Float getFloat(int row);
	public Integer getInt(int row);
	public Object getValue(int row);	//TODO:  This should use generics
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
