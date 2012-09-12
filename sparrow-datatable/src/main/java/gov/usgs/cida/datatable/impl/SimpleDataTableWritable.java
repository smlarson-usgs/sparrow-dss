package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleDataTableWritable implements DataTableWritable {

	protected List<ColumnDataWritable> columns = new ArrayList<ColumnDataWritable>();
	protected String description;
	protected String name;
	protected Map<String, String> properties= new HashMap<String, String>();
	protected Map<Long, Integer> idIndex;
	protected List<Long> idColumn;

	// ========================
	// CONVENIENCE CONSTRUCTORS
	// ========================
	public SimpleDataTableWritable() { /*  */};

	public SimpleDataTableWritable(int[][] data, String[] headings) {
		BuilderHelper.fill(this, data, headings);
	}
	public SimpleDataTableWritable(double[][] data, String[] headings) {
		BuilderHelper.fill(this, data, headings);
	}
	public SimpleDataTableWritable(double[][] data, String[] headings, Integer indexCol) {
		BuilderHelper.fill(this, data, headings, indexCol);
	}
	public SimpleDataTableWritable(double[][] data, String[] headings, int[] ids) {
		BuilderHelper.fill(this, data, headings);
		DataTableUtils.setIds(this, ids);
	}
	public SimpleDataTableWritable(int[][] data, String[] headings, Integer indexCol) {
		BuilderHelper.fill(this, data, headings, indexCol);
	}
	public SimpleDataTableWritable(int[][] data, String[] headings, int[] ids) {
		BuilderHelper.fill(this, data, headings);
		DataTableUtils.setIds(this, ids);
	}
	public SimpleDataTableWritable(String[] headings, String[] units, Class<?>... types ) {
		types = (types == null)? new Class<?>[0]: types;
		units = (units == null)? new String[0]: units;
		for (int i=0; i<headings.length; i++) {
			String heading = headings[i];
			String unit = (i < units.length)? units[i]: null;
			Class<?> type = (i < types.length)? types[i]: String.class;
			this.addColumn(BuilderHelper.createColWriteable(heading, type, unit));
		}
	}

	/**
	 * @param headings
	 * @param columnSources
	 *            -- not a two dimensional array of data, but should rather be
	 *            thought of an array of columns, e.g. data[row][col] =
	 *            columnSources[col][row]
	 */
	public SimpleDataTableWritable(String[] headings, double[]... columnSources) {
		BuilderHelper.fillTranspose(this, columnSources, headings);
	}

	// =================================
	// Data Population Lifecycle Methods
	// =================================
	@Override
	public DataTableWritable addColumn(ColumnDataWritable column) {
		columns.add(column);
		return this;
	}
	
	@Override
	public ColumnDataWritable removeColumn(int index)
			throws IndexOutOfBoundsException {
		return columns.remove(index);
	}
	
	@Override
	public ColumnDataWritable setColumn(ColumnDataWritable column, int columnIndex) {
		ColumnDataWritable replaced = null;
		if (columnIndex < columns.size()) {
			replaced = columns.get(columnIndex);
			columns.set(columnIndex, column);
		} else if (columnIndex == columns.size()) {
			columns.add(column);
		} else {
			throw new IndexOutOfBoundsException(
					"The specified column index would leave a gap in the columns");
		}
		
		return replaced;
	}
	
	@Override
	public ColumnDataWritable[] getColumns() {
		ColumnDataWritable[] newCols = new ColumnDataWritable[columns.size()];
		
		for (int i = 0; i < newCols.length; i++) {
			newCols[i] = columns.get(i);
		}
		
		return newCols;
	}

	@Override
	public void setValue(String value, int row, int col) throws IndexOutOfBoundsException {
		ColumnDataWritable column = columns.get(col);
		column.setValue(value, row);
	}

	@Override
	public void setValue(Number value, int row, int col) throws IndexOutOfBoundsException {
		ColumnDataWritable column = columns.get(col);
		column.setValue(value, row);
	}

	@Override
	public void setValue(Object value, int row, int col) throws IndexOutOfBoundsException {
		ColumnDataWritable column = columns.get(col);
		column.setValue(value, row);
	}

	@Override
	public boolean isValid() {
		return columns != null && isCheckAllColumnsSameSize();
	}
	
	@Override
	public boolean isValid(int columnIndex) {
		return columns.get(columnIndex).isValid();
	}

	@Override
	public DataTable.Immutable toImmutable() {
		SimpleDataTable result = new SimpleDataTable(this, columns, properties, idIndex, idColumn);
		// invalidate current instance
		columns = null;
		description = null;
		return result;
	}


	// =======================
	// Global Metadata Methods
	// =======================
	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	@Override
	public Map<String, String> getProperties() {
		HashMap<String, String> tmp = new HashMap<String, String>();
		tmp.putAll(properties);
		return tmp;
	}

	@Override
	public Map<String, String> getProperties(int col) {
		return columns.get(col).getProperties();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String setProperty(String key, String value) {
		return properties.put(key, value);
	}

	@Override
	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public int getRowCount() {
		int result = 0;
		for (ColumnData column: columns) {
			result = Math.max(result, column.getRowCount());
		}
		return result;
	}

	@Override
	public ColumnData getColumn(int colIndex) {
		return columns.get(colIndex);
	}
	
	@Override
	public Integer getColumnByName(String name) {
		for (int i=0; i<columns.size(); i++) {
			if (name.equals(columns.get(i).getName())) {
				return i;
			}
		}
		return null;
	}



	// ====================
	// Find & Index Methods
	// ====================
	@Override
	public int[] findAll(int col, Object value) {
		return columns.get(col).findAll(value);
	}

	@Override
	public int findFirst(int col, Object value) {
		return columns.get(col).findFirst(value);
	}

	@Override
	public int findLast(int col, Object value) {
		return columns.get(col).findLast(value);
	}

	@Override
	public void setRowId(long id, int row) {
		if (idColumn == null) {
			// initialize
			idColumn = new ArrayList<Long>();
			idIndex = new HashMap<Long, Integer>();
		}

		if (row != idColumn.size()) {
			throw new IllegalArgumentException("ids need to be added sequentially");
		}

		// Explicit wrapping is done to minimize the creation/storage of new objects
		Integer value = Integer.valueOf(row);
		Long key = Long.valueOf(id);
		idColumn.add(row, key);
		idIndex.put(key, value);
	}

	@Override
	public Long getIdForRow(int row) {
		return idColumn.get(row);
	}

	@Override
	public boolean isIndexed(int col) {
		return columns.get(col).isIndexed();
	}

	@Override
	public DataTableWritable buildIndex(int col) {
		columns.get(col).buildIndex();
		return this;
	}

	@Override
	public int getRowForId(Long id) {
		if (idIndex == null) {
			return -1;
		}
		Integer result = idIndex.get(id);
		return (result == null)? -1: result;
	}

	@Override
	public boolean hasRowIds() {
		return idColumn != null;
	}

	// ===============
	// MAX-MIN Methods
	// ===============
	@Override
	public Double getMaxDouble(int col) {
		return columns.get(col).getMaxDouble();
	}

	@Override
	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	@Override
	public Integer getMaxInt(int col) {
		return columns.get(col).getMaxInt();
	}

	@Override
	public Integer getMaxInt() {
		// this is important to keep as it's likely to require less conversion from native type
		Integer result = null;
		for (int col=0; col<columns.size(); col++ ) {
			Integer max = getMaxInt(col);
			if (max != null) {
				result = (result == null)? max: Math.max(result, max);
			}
		}
		return result;
	}

	@Override
	public Double getMinDouble(int col) {
		return columns.get(col).getMinDouble();
	}

	@Override
	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	@Override
	public Integer getMinInt(int col) {
		return columns.get(col).getMinInt();
	}

	@Override
	public Integer getMinInt() {
		// this is important to keep as it's likely to require less conversion from native type
		Integer result = null;
		for (int col=0; col<columns.size(); col++ ) {
			Integer min = getMinInt(col);
			if (min != null) {
				result = (result == null)? min: Math.min(result, min);
			}
		}
		return result;
	}

	// =======================
	// Column Metadata Methods
	// =======================
	@Override
	public String getName(int col) {
		return columns.get(col).getName();
	}


	public void setName(String name, int col) {
		columns.get(col).setName(name);
	}

	@Override
	public String getProperty(int col, String key) {
		return columns.get(col).getProperty(key);
	}

	@Override
	public String getUnits(int col) {
		return columns.get(col).getUnits();
	}

	public void setUnits(String units, int col) {
		columns.get(col).setUnits(units);
	}

	@Override
	public Class<?> getDataType(int col) {
		return columns.get(col).getDataType();
	}

	@Override
	public String getDescription(int col) {
		return columns.get(col).getDescription();
	}

	@Override
	public Set<String> getPropertyNames(int col) {
		return columns.get(col).getPropertyNames();
	}

	// ===========================================
	// getXXX(int row, int col) cell value methods
	// ===========================================
	@Override
	public Integer getInt(int row, int col) {
		ColumnDataWritable column = columns.get(col);
		return (column != null)? column.getInt(row): null;
	}

	@Override
	public Float getFloat(int row, int col) {
		ColumnDataWritable column = columns.get(col);
		return (column != null)? column.getFloat(row): null;
	}

	@Override
	public String getString(int row, int col) {
		ColumnDataWritable column = columns.get(col);
		return (column != null)? column.getString(row): null;
	}

	@Override
	public Double getDouble(int row, int col) {
		ColumnDataWritable column = columns.get(col);
		return (column != null)? column.getDouble(row): null;
	}

	@Override
	public Object getValue(int row, int col) {
		ColumnDataWritable column = columns.get(col);
		return (column != null)? column.getValue(row): null;
	}

	@Override
	public Long getLong(int row, int col) {
		ColumnData column = columns.get(col);
		return (column != null)? column.getLong(row): null;
	}

	// =======================
	// Private Utility Methods
	// =======================

	private boolean isCheckAllColumnsSameSize() {
		for (ColumnData column: columns) {
			if (!column.getRowCount().equals(columns.get(0).getRowCount())) {
				return false;
			}
		}
		return true;
	}


}
