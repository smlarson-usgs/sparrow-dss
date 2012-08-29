package gov.usgswim.datatable.adjustment;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnIndex;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.HashMapColumnIndex;
import gov.usgswim.datatable.impl.ColumnDataFromTable;
import gov.usgswim.datatable.impl.FindHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The immutable result of ColumnCoefAdjustment. This isn't really needed
 * because DataTableImmutable instances can be easily created via
 * SimpleDataTableImmutable, but we're doing this as a premature optimization to
 * avoid an extra layer of indirection, at a cost of some duplicate code.
 * 
 * @author ilinkuo
 * 
 */
public class ColumnCoefAdjustmentImmutable implements DataTable.Immutable {
	public static Double ZERO = Double.valueOf(0);
	public static Double ONE = Double.valueOf(1);
	
	private final DataTable source;
	private final Map<Integer, Double> columnMultipliers;
	
	public ColumnCoefAdjustmentImmutable(DataTable source, Map<Integer, Double> columnMultipliers) {
		this.source = source;
		
		// detach and copy columnMultipliers
		if (columnMultipliers != null && columnMultipliers.size() != 0) {
			this.columnMultipliers = new HashMap<Integer, Double>(columnMultipliers.size());
			this.columnMultipliers.putAll(columnMultipliers);
		} else {
			this.columnMultipliers = new HashMap<Integer, Double>(0);
		}
	}

	protected Double getMultiplier(int col) {
		Double lookup = columnMultipliers.get(col);
		return (lookup == null)? 1: lookup;
	}

	public boolean isValid() {
		return source != null;
	}
	
	@Override
	public boolean isValid(int columnIndex) {
		return source.isValid(columnIndex);
	}

	public DataTable.Immutable toImmutable() {
		return this;
	}
	
	@Override
	public ColumnIndex getIndex() {
		if (source instanceof DataTable.Immutable) {
			return ((DataTable.Immutable) source).getIndex();
		} else {
			return new HashMapColumnIndex(source);
		}
	}
	
	// ==========================
	// Adjusted findXXX() Methods
	// ==========================
	/**
	 * @see gov.usgswim.datatable.DataTable#findAll(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if rows have been adjusted
	 *  because of rounding
	 */
	@Deprecated
	public int[] findAll(int col, Object value) {
//		Double multiplier = columnMultipliers.get(col);
//		if (multiplier != null) {
//			if (!multiplier.equals(ZERO) && !multiplier.equals(ONE)) {
//				Double searchValue = ((Number) value).doubleValue()/multiplier;
//				return source.findAll(col, searchValue);
//			}
//			// note this is wrong when multiplier = 1 & value = 1
//		}
//		return source.findAll(col, value);
		return FindHelper.bruteForceFindAll(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findFirst(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if columns have been adjusted
	 *  because of rounding.
	 */
	@Deprecated
	public int findFirst(int col, Object value) {
//		Double multiplier = columnMultipliers.get(col);
//		if (multiplier != null) {
//			if (!multiplier.equals(ZERO) && !multiplier.equals(ONE)) {
//				Double searchValue = ((Number) value).doubleValue()/multiplier;
//				return source.findFirst(col, searchValue);
//			} else if (multiplier.equals(ZERO) && ZERO.equals(value)) {
//				return 0;
//			}
//		}
//		return source.findFirst(col, value);
		return FindHelper.bruteForceFindFirst(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findLast(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if columns have been adjusted
	 *  because of rounding
	 */
	@Deprecated
	public int findLast(int col, Object value) {
//		Double multiplier = columnMultipliers.get(col);
//		if (multiplier != null) {
//			if (!multiplier.equals(ZERO) && !multiplier.equals(ONE)) {
//				Double searchValue = ((Number) value).doubleValue()/multiplier;
//				return source.findLast(col, searchValue);
//			} else if (multiplier.equals(ZERO) && ZERO.equals(value)) {
//				return getRowCount();
//			}
//		}
//		return source.findLast(col, value);
		return FindHelper.bruteForceFindLast(this, col, value);
	}
	
	// ========================
	// Adjusted Max-Min Methods
	// ========================
	// TODO [IK] change the implementation of these later
	public Double getMaxDouble(int col) {
		if (Number.class.isAssignableFrom(source.getDataType(col))){
			if (columnMultipliers.containsKey(col)) {
				return source.getMaxDouble(col) * getMultiplier(col);
			}
			return source.getMaxDouble(col);
		}
		return null;
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
		if (Number.class.isAssignableFrom(source.getDataType(col))){
			if (columnMultipliers.containsKey(col)) {
				return source.getMinDouble(col) * getMultiplier(col);
			}
			return source.getMinDouble(col);
		}
		return null;
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
	
	// =================
	// Adjusted getXXX()
	// =================
	public Object getValue(int row, int col) {
		Object value = source.getValue(row, col);
		if (value instanceof Number) {
			return ((Number) value).doubleValue() * getMultiplier(col);
		}
		return value;
	}
	
	public Integer getInt(int row, int col) {
		return Double.valueOf((source.getDouble(row, col) * getMultiplier(col))).intValue();
	}
	public Double getDouble(int row, int col) {
		return source.getDouble(row, col) * getMultiplier(col);
	}
	public Float getFloat(int row, int col) {
		return Double.valueOf(source.getDouble(row, col) * getMultiplier(col)).floatValue();
	}
	public Long getLong(int row, int col) {
		return Double.valueOf(source.getDouble(row, col) * getMultiplier(col)).longValue();
	}
	public String getString(int row, int col) {
		Object val = getDouble(row, col);
		if (val != null) {
			return val.toString();
		} else {
			return "[null]";
		}
	}
	
	@Override
	public ColumnData getColumn(int colIndex) {
		
		if (colIndex < 0 || colIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Requested column index does not exist.");
		}
		
		ColumnDataFromTable col = new ColumnDataFromTable(this, colIndex);
		return col;
	}
	
	// =================
	// Delegated methods
	// =================
	public Integer getColumnByName(String name) {
		return source.getColumnByName(name);
	}
	public int getColumnCount() {
		return source.getColumnCount();
	}
	public Class<?> getDataType(int col) {
		return source.getDataType(col);
	}
	public String getDescription() {
		return source.getDescription();
	}
	public String getDescription(int col) {
		return source.getDescription(col);
	}
	public Long getIdForRow(int row) {
		return source.getIdForRow(row);
	}
	public String getName() {
		return source.getName();
	}
	public String getName(int col) {
		return source.getName(col);
	}
	public String getProperty(String name) {
		return source.getProperty(name);
	}
	public String getProperty(int col, String name) {
		return source.getProperty(col, name);
	}
	public Set<String> getPropertyNames() {
		return source.getPropertyNames();
	}
	public Set<String> getPropertyNames(int col) {
		return source.getPropertyNames(col);
	}
	@Override
	public Map<String, String> getProperties() {
		return source.getProperties();
	}
	@Override
	public Map<String, String> getProperties(int col) {
		return source.getProperties(col);
	}
	public int getRowCount() {
		return source.getRowCount();
	}
	public int getRowForId(Long id) {
		return source.getRowForId(id);
	}
	public String getUnits(int col) {
		return source.getUnits(col);
	}
	public boolean hasRowIds() {
		return source.hasRowIds();
	}
	public boolean isIndexed(int col) {
		return source.isIndexed(col);
	}

}
