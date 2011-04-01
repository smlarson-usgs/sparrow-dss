package gov.usgswim.sparrow.datatable;

import java.util.Map;
import java.util.Set;

import gov.usgswim.datatable.ColumnAttribs;
import gov.usgswim.datatable.ColumnAttribsImm;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.FindHelper;

/**
 * An abstract implementation of ColumnData that works as a base for a column
 * view.
 * 
 * This implementation will work for most most numeric views - any data
 * manipulation can be done in the getDouble(row) method.
 * 
 * Column properties are pulled from a properties column which can be
 * (and often will be) a reference to the baseCol.
 * These properties can be overridden by passing a ColumnAttribs object.  The
 * ColumnAttrib may be null, but if non-null, any properties in it will
 * override those of the propertiesCol.
 * 
 * @author eeverman
 *
 */
public abstract class AbstractColumnDataView implements ColumnData {

	private static final long serialVersionUID = 1L;

	/** The base column */
	protected ColumnData baseCol;
	
	/** The properties column - may duplicate the base column */
	protected ColumnData propertiesCol;
	
	/** Specifies the attributes for the specified column,
	 * allowing overwrites and additions to the attributes in the primary column.
	 */
	protected ColumnAttribs colAttribs;

	public AbstractColumnDataView(ColumnData baseCol, ColumnData propertiesCol,
			ColumnAttribs colAttribs) {
		
		this.baseCol = baseCol;
		this.propertiesCol = propertiesCol;
		this.colAttribs = colAttribs;
		
		if (baseCol == null || propertiesCol == null) {
			throw new IllegalArgumentException("The baseCol and/or propertiesCol" +
					" cannot be null.");
		}
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}
	
	//
	// Implement these methods
	
	/**
	 * Part of the ColumnData interface.
	 * For numerical columns in most cases you can implement this method and
	 * all the other value access methods default implementation will be fine.
	 */
	@Override
	public abstract Double getDouble(int row);
	
	/**
	 * Part of the ColumnData interface.
	 * Is the column view valid or have errors?
	 */
	@Override
	public abstract boolean isValid();

	/**
	 * Part of the ColumnData interface.
	 * Implement a way to convert this table to immutable.  The ColumnData
	 * interface does not have mutate methods, but the underlying data may
	 * have hooks to other code that could modify it...
	 */
	@Override
	public abstract ColumnData toImmutable();
	
	
	// ===============
	// GET METADATA METHODS
	// ===============
	
	/**
	 * Its a reasonable assumption that the view's return data type is the same
	 * as the base column... but not always.
	 */
	@Override
	public Class<?> getDataType() {
		return propertiesCol.getDataType();
	}
	
	@Override
	public String getName() {
		return colAttribs.getName(propertiesCol.getName());
	}

	@Override
	public String getUnits() {
		return colAttribs.getUnits(propertiesCol.getUnits());
	}

	@Override
	public String getDescription() {
		return colAttribs.getDescription(propertiesCol.getDescription());
	}

	@Override
	public String getProperty(String key) {
		return colAttribs.getProperty(key, propertiesCol.getProperty(key));
	}

	@Override
	public Set<String> getPropertyNames() {
		return colAttribs.getPropertyNames(propertiesCol.getPropertyNames());
	}

	@Override
	public Map<String, String> getProperties() {
		return colAttribs.getProperties(propertiesCol.getProperties());
	}
	
	@Override
	public Integer getRowCount() {
		return baseCol.getRowCount();
	}
	
	// ===============
	// GET VALUE METHODS
	// ===============
	
	@Override
	public Float getFloat(int row) {
		Double val = getDouble(row);
		if (val != null) {
			return val.floatValue();
		} else {
			return null;
		}
	}

	@Override
	public Integer getInt(int row) {
		Double val = getDouble(row);
		if (val != null) {
			return val.intValue();
		} else {
			return null;
		}
	}

	@Override
	public Long getLong(int row) {
		Double val = getDouble(row);
		if (val != null) {
			return val.longValue();
		} else {
			return null;
		}
	}

	@Override
	public String getString(int row) {
		Double val = getDouble(row);
		if (val != null) {
			return val.toString();
		} else {
			return null;
		}
	}
	
	@Override
	public Object getValue(int row) {
		return getDouble(row);
	}

	// ======================
	// Find and Index Methods
	// ======================
	
	@Override
	public boolean isIndexed() {
		return false;	//base table index would be invalid for most view usages
	}

	@Override
	public int[] findAll(Object value) {
		return FindHelper.findAllWithoutIndex(value, this);
	}

	@Override
	public int findFirst(Object value) {
		return FindHelper.bruteForceFindFirst(this, value);
	}

	@Override
	public int findLast(Object value) {
		return FindHelper.bruteForceFindLast(this, value);
	}

	// ===============
	// MIN-MAX METHODS
	// ===============
	
	@Override
	public Double getMaxDouble() {
		return FindHelper.findMaxDouble(this);
	}

	@Override
	public Double getMinDouble() {
		return FindHelper.findMinDouble(this);
	}
	
	@Override
	public Integer getMaxInt() {
		Double d = getMaxDouble();
		if (d != null) {
			return d.intValue();
		} else {
			return null;
		}
	}

	@Override
	public Integer getMinInt() {
		Double d = getMinDouble();
		if (d != null) {
			return d.intValue();
		} else {
			return null;
		}
	}

}