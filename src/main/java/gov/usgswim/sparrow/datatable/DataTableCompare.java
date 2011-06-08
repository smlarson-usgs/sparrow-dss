package gov.usgswim.sparrow.datatable;

import org.apache.commons.lang.StringUtils;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTable.Immutable;
import gov.usgswim.datatable.impl.ColumnDataFromTable;
import gov.usgswim.datatable.impl.FindHelper;
import gov.usgswim.sparrow.domain.ComparisonType;


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
	
	private static final long serialVersionUID = 1L;
	
	protected final DataTable compare;
	protected final ComparisonType type;
	
	/**
	 * Constructs a new comparison instance
	 * @param base	The data to compare to
	 * @param compare	The data to be compared
	 * @param type	Specifies the type of comparison
	 */
	public DataTableCompare(DataTable base, DataTable compare, ComparisonType type) {
		super(base);
		this.compare = compare;
		this.type = type;
	}

	/**
	 * Actual type is a mixture, but when the comparison is done, resolution is Double.
	 */
	@Override
	public Class<?> getDataType(int col) {
		return Double.class;
	}

	/**
	 * 
	 * For percent, the cal is: (compareCol / baseCol) * 100.
	 * For absolute, the calc is: (compareCol - baseCol).
	 * For percent change, the calc is: ((compareCol - baseCol) / baseCol) * 100.
	 * 
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
	public Double getDouble(int row, int col) {
		
		if (isStringCol(col)) {
			return new Double(getString(row, col));
		} else {
			Double b = base.getDouble(row, col);
			Double c = compare.getDouble(row, col);
			
			//True for all types of comparison
			if (b == null && c == null) {
				return 0d;
			}
			
			if (b == null) b = 0d;
			if (c == null) c = 0d;
			
			switch (type) {
			case percent:
				if (b != 0d) {
					return 100d * c / b;
				} else if (c > 0) {
					return 100d;
				} else if (c < 0) {
					return -100d;
				} else {
					return 0d;	// 0 over 0
				}
				//break; //unreachable
			case absolute:
				return c - b;
				//break; //unreachable
			case percent_change:
				if (b != 0d) {
					return 100d * (c - b) / b;
				} else if (c > 0) {
					return 100d;
				} else if (c < 0) {
					return -100d;
				} else {
					return 0d;	// 0 over 0
				}
				//break; //unreachable
			default:
				throw new RuntimeException("Unexpected ComparisonType " + type);
			}
		}

	}

	@Override
	public Float getFloat(int row, int col) {
		return getDouble(row, col).floatValue();
	}

	@Override
	public Integer getInt(int row, int col) {
		return getDouble(row, col).intValue();
	}

	@Override
	public Long getLong(int row, int col) {
		return getDouble(row, col).longValue();
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
		if (ComparisonType.absolute.equals(type)) {
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
	
	@Override
	public ColumnData getColumn(int colIndex) {
		
		if (colIndex < 0 || colIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Requested column index does not exist.");
		}
		
		ColumnDataFromTable col = new ColumnDataFromTable(this, colIndex);
		return col;
	}
	
	protected Double bruteForceFindMaxDouble(DataTable dt, int col) {
		Double max = Double.MIN_VALUE * -1d;
		int rows = dt.getRowCount();
		for (int row=0; row < rows; row++) {
			Double value = dt.getDouble(row, col);
			if (value != null) {
				if (value > max) {
					max = value;
				}
			}
		}
		
		return max;
	}
	
	protected Double bruteForceFindMaxDouble(DataTable dt) {
		Double max = Double.MIN_VALUE * -1d;
		int cols = dt.getColumnCount();
		for (int col=0; col < cols; col++) {
			Double value = dt.getMaxDouble(col);
			if (value != null) {
				if (value > max) {
					max = value;
				}
			}
		}
		return max;
	}

}
