package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTable.Immutable;
import gov.usgswim.datatable.impl.FindHelper;

import java.util.Set;

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
public class DataTableCompare implements Immutable {
	
	protected final DataTable base;
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
		this.base = base.toImmutable();
		this.compare = compare.toImmutable();
		this.absolute = isAbsolute;
	}

	public int[] findAll(int col, Object value) {
		return base.findAll(col, value);
	}

	public int findFirst(int col, Object value) {
		return base.findFirst(col, value);
	}

	public int findLast(int col, Object value) {
		return base.findLast(col, value);
	}

	public Integer getColumnByName(String name) {
		return base.getColumnByName(name);
	}

	public int getColumnCount() {
		return base.getColumnCount();
	}

	public Class<?> getDataType(int col) {
		return Double.class;
	}

	public String getDescription() {
		return base.getDescription();
	}

	public String getDescription(int col) {
		return base.getDescription(col);
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
	
	public Long getIdForRow(int row) {
		return base.getIdForRow(row);
	}

	public Double getMaxDouble(int col) {
		return FindHelper.bruteForceFindMaxDouble(this, col);
	}

	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	public Integer getMaxInt(int col) {
		return getMaxDouble(col).intValue();
	}

	public Integer getMaxInt() {
		return getMaxDouble().intValue();
	}

	public Double getMinDouble(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col);
	}

	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	public Integer getMinInt(int col) {
		return getMinDouble(col).intValue();
	}

	public Integer getMinInt() {
		return getMinDouble().intValue();
	}

	public String getName() {
		return base.getName();
	}

	public String getName(int col) {
		return base.getName(col);
	}

	public String getProperty(String name) {
		return base.getProperty(name);
	}

	public String getProperty(int col, String name) {
		return base.getProperty(col, name);
	}

	public Set<String> getPropertyNames() {
		return base.getPropertyNames();
	}

	public Set<String> getPropertyNames(int col) {
		return base.getPropertyNames(col);
	}

	public int getRowCount() {
		return base.getRowCount();
	}

	public int getRowForId(Long id) {
		return base.getRowForId(id);
	}

	public String getString(int row, int col) {
		return Double.toString(getDouble(row, col));
	}

	public String getUnits(int col) {
		if (absolute) {
			return base.getUnits(col);
		}
		return "percentage";
	}

	public Object getValue(int row, int col) {
		return getDouble(row, col);
	}

	public boolean hasRowIds() {
		return base.hasRowIds();
	}

	public boolean isIndexed(int col) {
		return base.isIndexed(col);
	}

	public boolean isValid() {
		return base.isValid() && compare.isValid();
	}

	public Immutable toImmutable() {
		return this;
	}

}
