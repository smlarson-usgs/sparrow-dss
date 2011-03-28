package gov.usgswim.datatable.adjustment;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.ColumnDataFromTable;
import gov.usgswim.datatable.impl.FindHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * SparseOverrideAdjustment accepts either sourceData and/or a default values as
 * source, and then allows values to be overridden on a cell by cell basis. For
 * getXXX() methods, in the case where the underlying source returns null, the
 * default value is returned if appropriate.
 * 
 * @author ilinkuo
 * 
 */
public class SparseOverrideAdjustment implements DataTableWritable {

	private static final long serialVersionUID = 1L;
	

	//used for hash generation
	private final static long TWO_TO_THE_32ND = (long) Math.pow(2d, 32d);
	
	private DataTable source;
	protected Map<Long, Object> adjustments = new HashMap<Long, Object>();

	// ===========
	// CONSTRUCTOR
	// ===========
	public SparseOverrideAdjustment(DataTable sourceData) {
		this.source = sourceData;
	}
	
	// =================
	// Lifecycle Methods
	// =================
	public void setValue(String value, int row, int col) throws IndexOutOfBoundsException {
		adjust(row, col, Double.valueOf(value));
	}

	public void setValue(Number value, int row, int col) throws IndexOutOfBoundsException {
		adjust(row, col, value);
	}

	public void setValue(Object value, int row, int col) throws IndexOutOfBoundsException {
		adjust(row, col, value);
	}
	public void adjust(int row, int column, Object value) {
		adjustments.put(key(row, column), value);
	}

	public static Long key(int row, int column) {
		//Create a long hash
		long rowKey = (long)row * TWO_TO_THE_32ND;
		
		long hashKey = rowKey + (long) column;
		return hashKey;
	}
	
	public static int decodeRow(Long longKey) {
		long foundRow = longKey / TWO_TO_THE_32ND;
		return (int) foundRow;
	}
	
	public static int decodeCol(Long longKey) {
		long foundRow = longKey / TWO_TO_THE_32ND;
		long foundCol = longKey - (foundRow * TWO_TO_THE_32ND);
		return (int) foundCol;
	}

	public DataTable.Immutable toImmutable() {
		DataTable.Immutable result = new SparseOverrideAdjustmentImmutable(source, adjustments);
		
		// invalidate after constructing
		source = null;
		adjustments = null;
		
		return result;
	}
	
	
	// =================
	// Adjusted findXXX()
	// =================
	/**
	 * @see gov.usgswim.datatable.DataTable#findAll(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate as it searches on pre-adjusted values
	 */
	@Deprecated
	public int[] findAll(int col, Object value) {
//		return source.findAll(col, value);
		return FindHelper.bruteForceFindAll(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findFirst(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate as it searches on pre-adjusted values
	 */
	@Deprecated
	public int findFirst(int col, Object value) {
//		return source.findFirst(col, value);
		return FindHelper.bruteForceFindFirst(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findLast(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate as it searches on pre-adjusted values
	 */
	@Deprecated
	public int findLast(int col, Object value) {
//		return source.findLast(col, value);
		return FindHelper.bruteForceFindLast(this, col, value);
	}
	
	// =================
	// Adjusted getXXX()
	// =================
	public Double getDouble(int row, int col) {
		Object value = adjustments.get(key(row, col));
		if (value == null) {
			return source.getDouble(row, col);
		}
		return ((Number)value).doubleValue();
	}

	public Float getFloat(int row, int col) {
		Object value = adjustments.get(key(row, col));
		if (value == null) {
			return source.getFloat(row, col);
		}
		return ((Number)value).floatValue();
	}

	public Integer getInt(int row, int col) {
		Object value = adjustments.get(key(row, col));
		if (value == null) {
			return source.getInt(row, col);
		}
		return ((Number)value).intValue();
	}
	public Object getValue(int row, int col) {
		Object value = adjustments.get(key(row, col));
		if (value == null) {
			return source.getValue(row, col);
		}
		return value;
	}
	public String getString(int row, int col) {
		Object val = getValue(row, col);
		if (val != null) {
			return val.toString();
		} else {
			return "[null]";
		}
	}
	public Long getIdForRow(int row) {
		return source.getIdForRow(row);
	}
	public Long getLong(int row, int col) {
		Object value = adjustments.get(key(row, col));
		if (value == null) {
			return source.getLong(row, col);
		}
		return ((Number)value).longValue();
	}
	
	// ========================
	// Adjusted Max-Min Methods
	// ========================
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
	public Map<String, String> getProperties() { return source.getProperties(); }

	@Override
	public Map<String, String> getProperties(int col) { return source.getProperties(col); }
	
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
	public boolean isValid() {
		return source.isValid();
	}
	
	@Override
	public boolean isValid(int columnIndex) {
		return source.isValid(columnIndex);
	}

	public DataTableWritable addColumn(ColumnDataWritable column) {
		throw new UnsupportedOperationException("Cannot add columns to a View");
	}
	
	@Override
	public ColumnDataWritable removeColumn(int index)
			throws IndexOutOfBoundsException {
		throw new UnsupportedOperationException("Cannot add or remove columns from a view");
	}
	
	@Override
	public ColumnDataWritable setColumn(ColumnDataWritable column, int columnIndex) {
		
		throw new UnsupportedOperationException(
				"Not really useful to assign columns in a overlayed view." +
				"  Or perhaps it is, but its not implemented.");
	}
	
	@Override
	public ColumnDataWritable[] getColumns() {
		throw new UnsupportedOperationException(
				"Not really useful to work w/ columns in a overlayed view." +
				"  Or perhaps it is, but its not implemented.");
	}

	public DataTableWritable buildIndex(int col) {
		throw new UnsupportedOperationException("Implement this if needed");
	}

	public void setDescription(String desc) {
		throw new UnsupportedOperationException("Cannot set description on a view");
	}

	public void setName(String name) {
		throw new UnsupportedOperationException("Cannot set name on a view");		
	}

	public String setProperty(String key, String value) {
		throw new UnsupportedOperationException("Cannot set properties on a view (but should be able to?)");
	}

	public void setRowId(long id, int row) {
		throw new UnsupportedOperationException("Cannot set ids on a view");		
	}


}
