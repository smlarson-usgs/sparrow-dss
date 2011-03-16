package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.FindHelper;

import java.util.Map;
import java.util.Set;

/**
 * Multiplies two columns together to create a new virtual column using no
 * additional storage.
 * 
 * Column properties are pulled from the primaryColumn (not the coef table).
 * These properties can be overridden by passing a ColumnAttribs object.
 * 
 * @author eeverman
 *
 */
public class MultiplicativeColumnData implements ColumnData {

	private static final long serialVersionUID = 1L;

	/** The primary column is used for column attributes and one of the factors */
	private ColumnData primaryCol;
	
	/** The coefficient column to use */
	private ColumnData coefCol;
	
	/** Specifies the attributes for the specified column,
	 * allowing overwrites and additions to the attributes in the primary column.
	 */
	private ColumnAttribs colAttribs;
	
	public MultiplicativeColumnData(ColumnData primaryColumn, ColumnData coefColumn, ColumnAttribs colAttribs) {

		this.primaryCol = primaryColumn;
		this.coefCol = coefColumn;
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}
	
	// =========================
	// Standard Instance Methods
	// =========================
	@Override
	public String getName() {
		return colAttribs.getName(primaryCol.getName());
	}
	@Override
	public String getUnits() {
		return colAttribs.getUnits(primaryCol.getUnits());
	}
	@Override
	public Class<?> getDataType() {
		return primaryCol.getDataType();
	}
	@Override
	public String getDescription() {
		return colAttribs.getDescription(primaryCol.getDescription());
	}
	
	// =================
	// Property Handlers
	// =================
	@Override
	public String getProperty(String key) {
		return colAttribs.getProperty(key, primaryCol.getProperty(key));
	}
	
	@Override
	public Set<String> getPropertyNames() {
		return colAttribs.getPropertyNames(primaryCol.getPropertyNames());
	}
	
	@Override
	public Map<String, String> getProperties() {
		return colAttribs.getProperties(primaryCol.getProperties());
	}
	
	// ======================
	// Find and Index Methods
	// ======================

	public boolean isIndexed() {
		return false;	//base table index would be invalid due to coef
	}
	
	public int[] findAll(Object value) {
		return FindHelper.findAllWithoutIndex(value, this);
	}

	public int findFirst(Object value) {
		return FindHelper.bruteForceFindFirst(this, value);
	}

	public int findLast(Object value) {
		return FindHelper.bruteForceFindLast(this, value);
	}
	
	// ===============
	// MIN-MAX METHODS
	// ===============
	public Double getMaxDouble() {
		return FindHelper.findMaxDouble(this);
	}

	public Double getMinDouble() {
		return FindHelper.findMinDouble(this);
	}

	public Integer getMaxInt() {
		Double d = getMaxDouble();
		if (d != null) {
			return d.intValue();
		} else {
			return null;
		}
	}

	public Integer getMinInt() {
		Double d = getMinDouble();
		if (d != null) {
			return d.intValue();
		} else {
			return null;
		}
	}

	@Override
	public Double getDouble(int row) {
		Double base = primaryCol.getDouble(row);
		Double coef = coefCol.getDouble(row);
		
		if (base != null && coef != null) {
			return base * coef;
		} else {
			return null;
		}
	}

	@Override
	public Float getFloat(int row) {
		Double d = getDouble(row);
		if (d != null) {
			return d.floatValue();
		} else {
			return null;
		}
	}

	@Override
	public Integer getInt(int row) {
		Double d = getDouble(row);
		if (d != null) {
			return d.intValue();
		} else {
			return null;
		}
	}

	@Override
	public Long getLong(int row) {
		Double d = getDouble(row);
		if (d != null) {
			return d.longValue();
		} else {
			return null;
		}
	}

	@Override
	public Integer getRowCount() {
		return primaryCol.getRowCount();
	}

	@Override
	public String getString(int row) {
		Double d = getDouble(row);
		if (d != null) {
			return d.toString();
		} else {
			return null;
		}
	}

	@Override
	public Object getValue(int row) {
		return getDouble(row);
	}

	@Override
	public boolean isValid() {
		return primaryCol.isValid() && coefCol.isValid();
	}

	@Override
	public ColumnData toImmutable() {
		return this;
	}

}
