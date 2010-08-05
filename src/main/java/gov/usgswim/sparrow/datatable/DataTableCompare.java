package gov.usgswim.sparrow.datatable;

import org.apache.commons.lang.StringUtils;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTable.Immutable;
import gov.usgswim.datatable.impl.FindHelper;


/**
 * Compares two PredictResults and returns values as either percentage increase
 * or as absolute values.
 * 
 * find, ID, metadata, getMax/Min, return values from the base data.
 * 
 * getDataType returns Double for all columns b/c the returned data is always a comparison.
 * getUnits returns the units of the base table if absolute, 'percentage' otherwise.
 * 
 * Sparrow specific methods (the PredictResult interface) return column numbers
 * from the base data, but return comparison values for all data access methods.
 * 
 * @author eeverman
 *
 */
public class DataTableCompare extends AbstractDataTableBase implements Immutable {
	
	protected final DataTable compare;
	protected final boolean absolute;
	
	/**
	 * Constructs a new comparison instance
	 * @param base	The data to compare to
	 * @param compare	The data to be compared
	 * @param isAbsolute	If true, values are (compare - base).  If false (percentage increase),
	 * the values are (compare - base) / base.
	 */
	public DataTableCompare(DataTable base, DataTable compare, boolean isAbsolute) {
		super(base);
		this.compare = compare;
		this.absolute = isAbsolute;
	}

	/**
	 * Actual type is a mixture, but when the comparison is done, resolution is Double.
	 */
	@Override
	public Class<?> getDataType(int col) {
		return Double.class;
	}

	@Override
	public Double getDouble(int row, int col) {
		
		if (isStringCol(col)) {
			return new Double(getString(row, col));
		} else {
			double b = base.getDouble(row, col);
			double c = compare.getDouble(row, col);
			
			if (absolute) {
				return c - b;
			}
			if (b != 0d) {
				return 100d * (c - b) / b;
			}
			
			//TODO:  Increase or decreases from zero are considered +/-100%.
			//Is that correct?  JIRA isse filed: http://privusgs4.er.usgs.gov//browse/SPDSS-313
			if (c > b) {
				return 100d;
			} else if (b > c) {
				return -100d;
			} else {
				return 0d;
			}
		}

	}

	@Override
	public Float getFloat(int row, int col) {
		
		if (isStringCol(col)) {
			return new Float(getString(row, col));
		} else {
			double b = base.getDouble(row, col);
			double c = compare.getDouble(row, col);
			
			if (absolute) {
				return (float)(c - b);
			}
			if (b != 0d) {
				return (float)(100d * (c - b) / b);
			}
			if (c > b) {
				return 100f;
			} else if (b > c) {
				return -100f;
			} else {
				return 0f;
			}
		}

	}

	@Override
	public Integer getInt(int row, int col) {
		
		if (isStringCol(col)) {
			return new Integer(getString(row, col));
		} else {
			double b = base.getDouble(row, col);
			double c = compare.getDouble(row, col);
			
			if (absolute) {
				return (int)(c - b);
			}
			if (b != 0d) {
				return (int) (100d * (c - b) / b);
			}
			if (c > b) {
				return 100;
			} else if (b > c) {
				return -100;
			} else {
				return 0;
			}
		}

	}

	@Override
	public Long getLong(int row, int col) {
		
		if (isStringCol(col)) {
			return new Long(getString(row, col));
		} else {
			double b = base.getDouble(row, col);
			double c = compare.getDouble(row, col);
			
			if (absolute) {
				return (long)(c - b);
			}
			if (b != 0d) {
				return (long)(100d * (c - b) / b);
			}
			if (c > b) {
				return 100L;
			} else if (b > c) {
				return -100L;
			} else {
				return 0L;
			}
		}
	}
	
	@Override
	public Double getMaxDouble(int col) {
		return bruteForceFindMaxDouble(this, col);
	}

	@Override
	public Double getMaxDouble() {
		return bruteForceFindMaxDouble(this);
	}

	@Override
	public Double getMinDouble(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col);
	}

	@Override
	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	/**
	 * For columns that are strings in both tables, this returns the
	 * Levenshtein distance between the two strings.  When calculating distance,
	 * null strings are considered empty, though comparing an empty to a null
	 * String will result in a distance of 1.
	 * 
	 * If both columns are not Strings, a double comparison is done.
	 */
	@Override
	public String getString(int row, int col) {
		if (isStringCol(col)) {
			String org = base.getString(row, col);
			String comp = compare.getString(row, col);
			
			if (org != null && comp != null) {
				return Integer.toString(StringUtils.getLevenshteinDistance(org, comp));
			} else if (org == null && comp == null) {
				return "0";
			} else if (org == null) {
				return (comp.length()==0)?"1":Integer.toString(comp.length());
			} else {
				return (org.length()==0)?"1":Integer.toString(org.length());
			}
			
		} else {
			return Double.toString(getDouble(row, col));
		}
		
	}

	/**
	 * For absolute comparisons, the unit of the column in the base table is returned.
	 * For percentage comparisons, 'Percentage' is returned.
	 */
	@Override
	public String getUnits(int col) {
		if (absolute) {
			return base.getUnits(col);
		}
		return "Percentage";
	}

	@Override
	public Object getValue(int row, int col) {
		return getDouble(row, col);
	}
	
	@Override
	public boolean isValid() {
		return super.isValid() && compare.isValid();
	}

	public Immutable toImmutable() {
		return this;
	}
	
	private boolean isStringCol(int index) {
		return (compare.getDataType(index) == String.class && super.getDataType(index) == String.class);
	}
	
	protected Double bruteForceFindMaxDouble(DataTable dt, int col) {
		Double max = 0d;
		int rows = dt.getRowCount();
		for (int row=0; row < rows; row++) {
			Double value = dt.getDouble(row, col);
			if (value != null) {
				if (Math.abs(value) > Math.abs(max)) {
					max = value;
				}
			}
		}
		return max;
	}
	
	protected Double bruteForceFindMaxDouble(DataTable dt) {
		Double max = 0d;
		int cols = dt.getColumnCount();
		for (int col=0; col < cols; col++) {
			Double value = dt.getMaxDouble(col);
			if (value != null) {
				if (Math.abs(value) > Math.abs(max)) {
					max = value;
				}
			}
		}
		return max;
	}

}
