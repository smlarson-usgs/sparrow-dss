package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.FindHelper;

import java.util.Map;
import java.util.Set;

/**
 * Calculate the percentage of one column with respect to another.
 * 
 * Note that this is different that percentage change.
 * 
 * The top part of the percentage fraction is called the 'compareCol' in the
 * code, the 'baseCol' is the bottom of the fraction.  The actual calculation is:
 * (compareCol / baseCol) * 100
 * 
 * There are some odd cases (zeros) that are dealt with in the getDouble()
 * method.
 * 
 * Column properties are pulled from a third column which can be passed in the
 * constructor.  This column may (and often will) be a reference to one of the others.
 * These properties can be overridden by passing a ColumnAttribs object.
 * If null, properties will be pulled from baseCol.
 * 
 * Units are always 'Percentage' unless explicitely set in the colAttribs.
 * 
 * @author eeverman
 *
 */
public class PercentageColumnData implements ColumnData {

	private static final long serialVersionUID = 1L;

	/** The numerator column of the percentage.  */
	private ColumnData compareCol;
	
	/** The denominator of the percentage */
	private ColumnData baseCol;
	
	/** The properties column - may duplicate one of the others */
	private ColumnData propertiesCol;
	
	/** Specifies the attributes for the specified column,
	 * allowing overwrites and additions to the attributes in the primary column.
	 */
	private ColumnAttribs colAttribs;
	
	public PercentageColumnData(ColumnData baseCol, ColumnData compareCol, ColumnData propertiesCol, ColumnAttribs colAttribs) {

		this.compareCol = compareCol;
		this.baseCol = baseCol;
		this.propertiesCol = propertiesCol;
		
		if (this.compareCol == null || this.baseCol == null) {
			throw new IllegalArgumentException("The compareCol and baseCol's" +
					" cannot be null.");
		}
		
		//Use the baseCol for properties if not spec'ed
		if (propertiesCol == null) {
			propertiesCol = baseCol;
		}
		
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
		return colAttribs.getName(propertiesCol.getName());
	}

	@Override
	public String getUnits() {
		if (colAttribs != null && colAttribs.getName() != null) {
			return colAttribs.getName();
		} else {
			return "Percentage";
		}
	}
	@Override
	public Class<?> getDataType() {
		return propertiesCol.getDataType();
	}
	@Override
	public String getDescription() {
		return colAttribs.getDescription(propertiesCol.getDescription());
	}
	
	// =================
	// Property Handlers
	// =================
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
	
	/**
	 * The basic calculation is: (compareCol / baseCol) * 100
	 * However, the following zero and null assumptions are made:
	 * 
	 * <ul>
	 * <li>A null value is never returned from this method.
	 * <li>If both values are null, zero is returned.
	 * <li>Any single null value will be considered a zero.
	 * <li>Any positive comp value over a zero base is considered a 100% increase.
	 * <li>Any negative comp value over a zero base is considered a 100% decrease.
	 * </ul>
	 */
	@Override
	public Double getDouble(int row) {
		
		//TODO:  Increase or decreases from zero are considered +/-100%.
		//Is that correct?  JIRA issue filed: http://privusgs4.er.usgs.gov//browse/SPDSS-313

		Double b = baseCol.getDouble(row);
		Double c = compareCol.getDouble(row);
		
		if (b == null && c == null) {
			return 0d;
		}
		
		if (b == null) b = 0d;
		if (c == null) c = 0d;
		
		if (b != 0d) {
			return 100d * c / b;
		} else if (c > 0) {
			return 100d;
		} else if (c < 0) {
			return -100d;
		} else {
			return 0d;	// 0 over 0
		}
		
	}


	@Override
	public Float getFloat(int row) {
		return getDouble(row).floatValue();
	}

	@Override
	public Integer getInt(int row) {
		return getDouble(row).intValue();
	}

	@Override
	public Long getLong(int row) {
		return getDouble(row).longValue();
	}

	@Override
	public String getString(int row) {
		return getDouble(row).toString();
	}

	@Override
	public Object getValue(int row) {
		return getDouble(row);
	}
	
	@Override
	public Integer getRowCount() {
		return compareCol.getRowCount();
	}

	@Override
	public boolean isValid() {
		return compareCol.isValid() && baseCol.isValid();
	}

	@Override
	public ColumnData toImmutable() {
		return this;
	}

}
