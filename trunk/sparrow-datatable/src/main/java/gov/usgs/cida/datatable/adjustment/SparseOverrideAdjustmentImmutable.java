package gov.usgs.cida.datatable.adjustment;

import gov.usgswim.Immutable;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.HashMapColumnIndex;
import gov.usgs.cida.datatable.impl.ColumnDataFromTable;
import gov.usgs.cida.datatable.impl.FindHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The immutable version of SparseOverrideAdjustment. This isn't really needed
 * because DataTableImmutable instances can be easily created via
 * SimpleDataTableImmutable, but we're doing this as a premature optimization to
 * avoid an extra layer of indirection, at a cost of some duplicate code.
 * 
 * @author ilinkuo
 *
 */
@Immutable
public class SparseOverrideAdjustmentImmutable implements DataTable.Immutable {

	private static final long serialVersionUID = 1L;
	
	private final Map<Long, Object> adjustments;
	private final DataTable.Immutable source;

	/**
	 * @param original
	 * @param source
	 * @param adjustments
	 * Note: For now set constructor access to package level
	 */
	SparseOverrideAdjustmentImmutable(DataTable source, Map<Long, Object> adjustments) {
		this.source = source.toImmutable();
		// detach and copy adjustments
		if (adjustments != null && adjustments.size() != 0) {
			this.adjustments = new HashMap<Long, Object>(adjustments.size());
			this.adjustments.putAll(adjustments);
		} else {
			this.adjustments = new HashMap<Long, Object>(0);
		}	
	}
	
	// =================
	// Lifecycle Methods
	// =================
	public DataTable.Immutable toImmutable() {
		return this;
	}
	
	@Override
	public ColumnIndex getIndex() {
		return source.getIndex();
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
		Object value = adjustments.get(SparseOverrideAdjustment.key(row, col));
		if (value == null) {
			return source.getDouble(row, col);
		}
		return ((Number)value).doubleValue();
	}

	public Float getFloat(int row, int col) {
		Object value = adjustments.get(SparseOverrideAdjustment.key(row, col));
		if (value == null) {
			return source.getFloat(row, col);
		}
		return ((Number)value).floatValue();
	}

	public Integer getInt(int row, int col) {
		Object value = adjustments.get(SparseOverrideAdjustment.key(row, col));
		if (value == null) {
			return source.getInt(row, col);
		}
		return ((Number)value).intValue();
	}
	public Object getValue(int row, int col) {
		Object value = adjustments.get(SparseOverrideAdjustment.key(row, col));
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
		Object value = adjustments.get(SparseOverrideAdjustment.key(row, col));
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


}
