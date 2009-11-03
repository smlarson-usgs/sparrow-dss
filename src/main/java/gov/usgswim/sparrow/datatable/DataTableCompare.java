package gov.usgswim.sparrow.datatable;

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
		this.compare = compare.toImmutable();
		this.absolute = isAbsolute;
	}

	/**
	 * Actual type is a mixture, but when the comparison is done, resolution is Double.
	 */
	public Class<?> getDataType(int col) {
		return Double.class;
	}

	public Double getDouble(int row, int col) {
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

	public Float getFloat(int row, int col) {
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

	public Integer getInt(int row, int col) {
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

	public Long getLong(int row, int col) {
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
	
	public Double getMaxDouble(int col) {
		return FindHelper.bruteForceFindMaxDouble(this, col);
	}

	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	public Double getMinDouble(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col);
	}

	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	public String getString(int row, int col) {
		return Double.toString(getDouble(row, col));
	}

	/**
	 * For absolute comparisons, the unit of the column in the base table is returned.
	 * For percentage comparisons, 'Percentage' is returned.
	 */
	public String getUnits(int col) {
		if (absolute) {
			return base.getUnits(col);
		}
		return "Percentage";
	}

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

}
