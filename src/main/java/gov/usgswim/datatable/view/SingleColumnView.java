package gov.usgswim.datatable.view;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.ColumnDataFromTable;

import java.util.Map;
import java.util.Set;

public class SingleColumnView implements DataView {

	private static final long serialVersionUID = 1L;

	public static final String OUT_OF_BOUNDS_MESSAGE = "%s has only a single column. %s is out of bounds.";

	protected final DataTable baseTable;
	protected final DataView baseView;
	protected final DataTableWritable baseWritable;
	protected final DataTable base;

	protected final int column;

	public SingleColumnView(DataTable table, int column) {
		this.baseTable = table;
		this.baseView = null;
		this.baseWritable = null;
		this.column = column;
		this.base = baseTable;
	}

	protected void checkColumnIndex(int col) {
		if (col != 0) {
			throw new IllegalArgumentException(
					String.format(OUT_OF_BOUNDS_MESSAGE, this.getClass().getSimpleName(), col));
		}
	}

	@Override
	public boolean isBasedOnDataTable() {
		return baseTable != null;
	}

	@Override
	public boolean isBasedOnDataTableWritable() {
		return baseWritable != null;
	}

	@Override
	public boolean isBasedOnView() {
		return baseView != null;
	}

	@Override
	public DataTable retrieveBaseTable() {
		return baseTable;
	}

	@Override
	public DataView retrieveBaseView() {
		return baseView;
	}

	@Override
	public DataTableWritable retrieveBaseWritable() {
		return baseWritable;
	}

	@Override
	public int[] findAll(int col, Object value) {
		checkColumnIndex(col);
		return base.findAll(column, value);
	}



	@Override
	public int findFirst(int col, Object value) {
		checkColumnIndex(col);
		return base.findFirst(column, value);
	}

	@Override
	public int findLast(int col, Object value) {
		checkColumnIndex(col);
		return base.findLast(column, value);
	}
	
	@Override
	public ColumnData getColumn(int colIndex) {
		
		if (colIndex < 0 || colIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Requested column index does not exist.");
		}
		
		ColumnDataFromTable col = new ColumnDataFromTable(this, colIndex);
		return col;
	}

	@Override
	public Integer getColumnByName(String name) {
		Integer result = base.getColumnByName(name);
		return (result == column)? 0: null;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Class<?> getDataType(int col) {
		checkColumnIndex(col);
		return base.getDataType(column);
	}

	@Override
	public String getDescription() {
		return base.getDescription();
	}

	@Override
	public String getDescription(int col) {
		checkColumnIndex(col);
		return base.getDescription(column);
	}

	/**
	 * <i><b>Note:</b></i> Index checking on this method is disabled for performance
	 * @see gov.usgswim.datatable.DataTable#getDouble(int, int)
	 */
	@Override
	public Double getDouble(int row, int col) {
		return base.getDouble(row, column);
	}

	/**
	 * <i><b>Note:</b></i> Index checking on this method is disabled for performance
	 * @see gov.usgswim.datatable.DataTable#getFloat(int, int)
	 */
	@Override
	public Float getFloat(int row, int col) {
		checkColumnIndex(col);
		return base.getFloat(row, column);
	}

	@Override
	public Long getIdForRow(int row) {
		return base.getIdForRow(row);
	}

	/**
	 * <i><b>Note:</b></i> Index checking on this method is disabled for performance
	 * @see gov.usgswim.datatable.DataTable#getInt(int, int)
	 */
	@Override
	public Integer getInt(int row, int col) {
		return base.getInt(row, column);
	}

	/**
	 * <i><b>Note:</b></i> Index checking on this method is disabled for performance
	 * @see gov.usgswim.datatable.DataTable#getLong(int, int)
	 */
	@Override
	public Long getLong(int row, int col) {
		return base.getLong(row, column);
	}

	@Override
	public Double getMaxDouble(int col) {
		checkColumnIndex(col);
		return base.getMaxDouble(column);
	}

	@Override
	public Double getMaxDouble() {
		return base.getMaxDouble(column);
	}

	@Override
	public Integer getMaxInt(int col) {
		checkColumnIndex(col);
		return base.getMaxInt(column);
	}

	@Override
	public Integer getMaxInt() {
		return base.getMaxInt(column);
	}

	@Override
	public Double getMinDouble(int col) {
		checkColumnIndex(col);
		return base.getMinDouble(column);
	}

	@Override
	public Double getMinDouble() {
		return base.getMinDouble(column);
	}

	@Override
	public Integer getMinInt(int col) {
		checkColumnIndex(col);
		return base.getMinInt(column);
	}

	@Override
	public Integer getMinInt() {
		return base.getMinInt(column);
	}

	@Override
	public String getName() {
		return base.getName();
	}

	@Override
	public String getName(int col) {
		checkColumnIndex(col);
		return base.getName(column);
	}

	@Override
	public String getProperty(String name) {
		return base.getProperty(name);
	}

	@Override
	public String getProperty(int col, String name) {
		checkColumnIndex(col);
		return base.getProperty(column, name);
	}

	@Override
	public Set<String> getPropertyNames() {
		return base.getPropertyNames();
	}

	@Override
	public Set<String> getPropertyNames(int col) {
		checkColumnIndex(col);
		return base.getPropertyNames(column);
	}
	
	@Override
	public Map<String, String> getProperties() { return base.getProperties(); }

	@Override
	public Map<String, String> getProperties(int col) {
		checkColumnIndex(col);
		return base.getProperties(col);
	}

	@Override
	public int getRowCount() {
		return base.getRowCount();
	}

	@Override
	public int getRowForId(Long id) {
		return base.getRowForId(id);
	}

	/**
	 * <i><b>Note:</b></i> Index checking on this method is disabled for performance
	 * @see gov.usgswim.datatable.DataTable#getString(int, int)
	 */
	@Override
	public String getString(int row, int col) {
		return base.getString(row, column);
	}

	@Override
	public String getUnits(int col) {
		checkColumnIndex(col);
		return base.getUnits(column);
	}

	/**
	 * <i><b>Note:</b></i> Index checking on this method is disabled for performance
	 * @see gov.usgswim.datatable.DataTable#getValue(int, int)
	 */
	@Override
	public Object getValue(int row, int col) {
		return base.getValue(row, column);
	}

	@Override
	public boolean hasRowIds() {
		return base.hasRowIds();
	}

	@Override
	public boolean isIndexed(int col) {
		checkColumnIndex(col);
		return base.isIndexed(column);
	}

	@Override
	public boolean isValid() {
		return base.isValid();
	}
	
	@Override
	public boolean isValid(int columnIndex) {
		return base.isValid(columnIndex);
	}

	@Override
	public Immutable toImmutable() {
		throw new UnsupportedOperationException("toImmutable() cannot be called on a view");
	}

}
