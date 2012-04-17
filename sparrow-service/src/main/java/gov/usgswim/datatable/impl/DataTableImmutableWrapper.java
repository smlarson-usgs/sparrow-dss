package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;

import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper class to hide the extraneous and state-modifying methods of the source.
 * @author ilinkuo
 *
 */
public class DataTableImmutableWrapper implements DataTable.Immutable {

	private static final long serialVersionUID = 1L;
	
	private final DataTable source;
	public DataTableImmutableWrapper(DataTable source) {
		this.source = source;
	}
	
	public DataTable.Immutable toImmutable() {
		return this;
	}
	
	// =================
	// Delegated Methods
	// =================
	public int[] findAll(int col, Object value) {
		return source.findAll(col, value);
	}
	public int findFirst(int col, Object value) {
		return source.findFirst(col, value);
	}
	public int findLast(int col, Object value) {
		return source.findLast(col, value);
	}
	@Override
	public ColumnData getColumn(int colIndex) {
		return source.getColumn(colIndex);
	}
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
	public Double getDouble(int row, int col) {
		return source.getDouble(row, col);
	}
	public Float getFloat(int row, int col) {
		return source.getFloat(row, col);
	}
	public Long getIdForRow(int row) {
		return source.getIdForRow(row);
	}
	public Integer getInt(int row, int col) {
		return source.getInt(row, col);
	}
	public Long getLong(int row, int col) {
		return source.getLong(row, col);
	}
	public Double getMaxDouble(int col) {
		return source.getMaxDouble();
	}
	public Double getMaxDouble() {
		return source.getMaxDouble();
	}
	public Integer getMaxInt(int col) {
		return source.getMaxInt(col);
	}
	public Integer getMaxInt() {
		return source.getMaxInt();
	}
	public Double getMinDouble(int col) {
		return source.getMinDouble(col);
	}
	public Double getMinDouble() {
		return source.getMinDouble();
	}
	public Integer getMinInt(int col) {
		return source.getMaxInt(col);
	}
	public Integer getMinInt() {
		return source.getMinInt();
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
	public String getString(int row, int col) {
		return source.getString(row, col);
	}
	public String getUnits(int col) {
		return source.getUnits(col);
	}
	public Object getValue(int row, int col) {
		return source.getValue(row, col);
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
	
	public boolean isValid(int columnIndex) {
		return source.isValid(columnIndex);
	}

}
