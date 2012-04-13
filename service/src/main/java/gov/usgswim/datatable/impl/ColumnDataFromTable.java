package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper for a table that allows a column of the table to be used as a
 * ColumnData instance.
 * 
 * @author eeverman
 *
 */
public class ColumnDataFromTable implements ColumnData {

	/** The datatable to pull data from */
	private DataTable baseTable;
	
	/** The index (zero based) of the column in baseTable to use. */
	private int colIndex;
	
	public ColumnDataFromTable(DataTable baseTable, int columnIndex) {

		this.baseTable = baseTable;
		this.colIndex = columnIndex;
	}
	
	// =========================
	// Standard Instance Methods
	// =========================	
	public String getName() {
		return baseTable.getName(colIndex);
	}
	public String getUnits() {
		return baseTable.getUnits(colIndex);
	}
	public Class<?> getDataType() {
		return baseTable.getDataType(colIndex);
	}
	public String getDescription() {
		return baseTable.getDescription(colIndex);
	}
	
	// =================
	// Property Handlers
	// =================
	public String getProperty(String key) {
		return baseTable.getProperty(colIndex, key);
	}
	public Set<String> getPropertyNames() {
		return baseTable.getPropertyNames(colIndex);
	}
	
	@Override
	public Map<String, String> getProperties() {
		return baseTable.getProperties(colIndex);
	}
	
	// ======================
	// Find and Index Methods
	// ======================

	public boolean isIndexed() {
		return baseTable.isIndexed(colIndex);
	}
	
	public int[] findAll(Object value) {
		return baseTable.findAll(colIndex, value);
	}

	public int findFirst(Object value) {
		return baseTable.findFirst(colIndex, value);
	}

	public int findLast(Object value) {
		return baseTable.findLast(colIndex, value);
	}
	
	// ===============
	// MIN-MAX METHODS
	// ===============
	public Double getMaxDouble() {
		return baseTable.getMaxDouble(colIndex);
	}

	public Double getMinDouble() {
		return baseTable.getMinDouble(colIndex);
	}

	public Integer getMaxInt() {
		return baseTable.getMaxInt(colIndex);
	}

	public Integer getMinInt() {
		return baseTable.getMinInt(colIndex);
	}

	@Override
	public Double getDouble(int row) {
		return baseTable.getDouble(row, colIndex);
	}

	@Override
	public Float getFloat(int row) {
		return baseTable.getFloat(row, colIndex);
	}

	@Override
	public Integer getInt(int row) {
		return baseTable.getInt(row, colIndex);
	}

	@Override
	public Long getLong(int row) {
		return baseTable.getLong(row, colIndex);
	}

	@Override
	public Integer getRowCount() {
		return baseTable.getRowCount();
	}

	@Override
	public String getString(int row) {
		return baseTable.getString(row, colIndex);
	}

	@Override
	public Object getValue(int row) {
		return baseTable.getValue(row, colIndex);
	}

	@Override
	public boolean isValid() {
		return baseTable.isValid(colIndex);
	}

	@Override
	public ColumnData toImmutable() {
		return this;
	}

}
