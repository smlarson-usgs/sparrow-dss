package gov.usgs.cida.datatable.adjustment;

import java.util.Map;
import java.util.Set;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.DataTableImmutableWrapper;

public class ColumnMappedTable implements DataTable {

	private static final long serialVersionUID = 1L;
	
	private final DataTable base;
	private final int[] map;

	// ===========
	// CONSTRUCTOR
	// ===========
	public ColumnMappedTable(DataTable base, int[] columnMapping) {
		this.base = base;
		if (columnMapping != null && columnMapping.length > 0 && isMapppedColumnsWithinBase()) {
			this.map = columnMapping;
		} else {
			this.map = null;
		}
	}

	// =================
	// LIFECYCLE METHODS
	// =================
	public boolean isValid() {
		return base != null && isMapppedColumnsWithinBase();
	}
	
	@Override
	public boolean isValid(int columnIndex) {
		int c = map(columnIndex);
		return base.isValid(c);
	}
	
	public Immutable toImmutable() {
		if (base instanceof DataTable.Immutable) return  new DataTableImmutableWrapper(this);
		int[] newMap = (map == null)? null: map.clone();
		ColumnMappedTable immutableCore = new ColumnMappedTable(base.toImmutable(), newMap);
		return new DataTableImmutableWrapper(immutableCore);
	}
	
	@Override
	public ColumnIndex getIndex() {
		return base.getIndex();
	}

	// ===============
	// UTILITY METHODS
	// ===============
	private int map(int col) {
		return (map == null)? col: map[col];
	}
	
	/**
	 * Checks whether all the columns in the mapping are within the range of the base
	 * @return
	 */
	private boolean isMapppedColumnsWithinBase() {
		if (map == null) return true;
		for (int mappedCol: map) {
			if (mappedCol >= base.getColumnCount()) return false;
		}
		return true;
	}
	
	// ===============
	// FIND METHODS
	// ===============
	public int[] findAll(int col, Object value) {
		return base.findAll(map(col), value);
	}

	public int findFirst(int col, Object value) {
		return base.findFirst(map(col), value);
	}
	
	public int findLast(int col, Object value) {
		return base.findLast(map(col), value);
	}

	@Override
	public ColumnData getColumn(int colIndex) {
		return base.getColumn(map(colIndex));
	}
	
	// ================
	// METAINFO METHODS
	// ================
	public Integer getColumnByName(String name) {
		return map(base.getColumnByName(name));
	}

	public int getColumnCount() {
		return (map == null)? base.getColumnCount(): map.length;
	}

	public Class<?> getDataType(int col) {
		return base.getDataType(map(col));
	}

	public String getDescription() {
		return base.getDescription();
	}

	public String getDescription(int col) {
		return base.getDescription(map(col));
	}

	public Double getDouble(int row, int col) {
		return base.getDouble(row, map(col));
	}

	public Float getFloat(int row, int col) {
		return base.getFloat(row, map(col));
	}

	public Long getIdForRow(int row) {
		return base.getIdForRow(row);
	}

	public Integer getInt(int row, int col) {
		return base.getInt(row, map(col));
	}

	public Long getLong(int row, int col) {
		return base.getLong(row, map(col));
	}

	public Double getMaxDouble(int col) {
		return base.getMaxDouble(map(col));
	}

	public Double getMaxDouble() {
		return base.getMaxDouble();
	}

	public Integer getMaxInt(int col) {
		return base.getMaxInt(map(col));
	}

	public Integer getMaxInt() {
		return base.getMaxInt();
	}

	public Double getMinDouble(int col) {
		return base.getMinDouble(map(col));
	}

	public Double getMinDouble() {
		return base.getMinDouble();
	}

	public Integer getMinInt(int col) {
		return base.getMinInt(map(col));
	}

	public Integer getMinInt() {
		return base.getMinInt();
	}

	public String getName() {
		return base.getName();
	}

	public String getName(int col) {
		return base.getName(map(col));
	}

	public String getProperty(String name) {
		return base.getProperty(name);
	}

	public String getProperty(int col, String name) {
		return base.getProperty(map(col), name);
	}

	public Set<String> getPropertyNames() {
		return base.getPropertyNames();
	}

	public Set<String> getPropertyNames(int col) {
		return base.getPropertyNames(map(col));
	}
	@Override
	public Map<String, String> getProperties() {
		return base.getProperties();
	}
	@Override
	public Map<String, String> getProperties(int col) {
		return base.getProperties(map(col));
	}

	public int getRowCount() {
		return base.getRowCount();
	}

	public int getRowForId(Long id) {
		return base.getRowForId(id);
	}

	public String getString(int row, int col) {
		return base.getString(row, map(col));
	}

	public String getUnits(int col) {
		return base.getUnits(map(col));
	}

	public Object getValue(int row, int col) {
		return base.getValue(row, map(col));
	}

	public boolean hasRowIds() {
		return base.hasRowIds();
	}

	public boolean isIndexed(int col) {
		return base.isIndexed(map(col));
	}


}
