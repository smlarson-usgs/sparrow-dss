package gov.usgs.cida.datatable.view;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.ColumnDataFromTable;
import gov.usgs.cida.datatable.impl.FindHelper;

import java.util.Map;
import java.util.Set;

/**
 * A base table view that can be subclassed as an easy starting point to create
 * other views.
 * 
 * @author eeverman
 * 
 */
public abstract class AbstractDataTableView implements DataView {
	public static Double ZERO = Double.valueOf(0);
	public static Double ONE = Double.valueOf(1);
	protected final DataTable base;

	// ===========
	// CONSTRUCTOR
	// ===========
	public AbstractDataTableView(DataTable sourceData) {
		this.base = sourceData;
	}
	
	// =================
	// Lifecycle Methods
	// =================

	public abstract DataTable.Immutable toImmutable();
	
	@Override
	public boolean isValid() {
		return base != null && base.isValid();
	}
	
	@Override
	public boolean isValid(int columnIndex) {
		return isValid() && base.isValid(columnIndex);
	}
	
	// ==========================
	// FindXXX() Methods
	// ==========================
	/**
	 * @see gov.usgswim.datatable.DataTable#findAll(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if
	 * the view multiplies the values due to rounding errors.
	 */
	@Deprecated
	@Override
	public int[] findAll(int col, Object value) {
		return FindHelper.bruteForceFindAll(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findFirst(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if
	 * the view multiplies the values due to rounding errors.
	 */
	@Override
	@Deprecated
	public int findFirst(int col, Object value) {
		return FindHelper.bruteForceFindFirst(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findLast(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if
	 * the view multiplies the values due to rounding errors.
	 */
	@Override
	@Deprecated
	public int findLast(int col, Object value) {
		return FindHelper.bruteForceFindLast(this, col, value);
	}

	// ========================
	// Max-Min Methods
	// ========================
	@Override
	public Double getMaxDouble(int col) {
		return base.getMaxDouble(col);
	}

	@Override
	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	@Override
	public Integer getMaxInt(int col) {
		return getMaxDouble(col).intValue();
	}

	@Override
	public Integer getMaxInt() {
		return getMaxDouble().intValue();
	}
	
	@Override
	public Double getMinDouble(int col) {
		return base.getMinDouble(col);
	}

	@Override
	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	@Override
	public Integer getMinInt(int col) {
		return getMinDouble(col).intValue();
	}
	
	@Override
	public Integer getMinInt() {
		return getMinDouble().intValue();
	}
	
	// =================
	// getXXX()
	// =================
	/**
	 * Note that subclasses can override just getValue() and getDouble() affect all
	 * the get[type] methods as they will all delegate to these two methods.
	 * @param row
	 * @param col
	 * @return 
	 */
	@Override
	public Object getValue(int row, int col) {
		return base.getValue(row, col);
	}
	
	/**
	 * Note that subclasses can override just getValue() and getDouble() affect all
	 * the get[type] methods as they will all delegate to these two methods.
	 * @param row
	 * @param col
	 * @return 
	 */
	@Override
	public Double getDouble(int row, int col) {
		return base.getDouble(row, col);
	}
	
	@Override
	public Integer getInt(int row, int col) {
		Double val = getDouble(row, col);
		if (val != null) {
			return val.intValue();
		} else {
			return null;
		}
	}

	@Override
	public Float getFloat(int row, int col) {
		Double val = getDouble(row, col);
		if (val != null) {
			return val.floatValue();
		} else {
			return null;
		}
	}
	
	@Override
	public Long getLong(int row, int col) {
		Double val = getDouble(row, col);
		if (val != null) {
			return val.longValue();
		} else {
			return null;
		}
	}
	
	//TODO:  This is a change from how null strings were handled in the Adjustment View
	@Override
	public String getString(int row, int col) {
		Object val = getValue(row, col);
		if (val != null) {
			return val.toString();
		} else {
			return null;
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
	@Override
	public Integer getColumnByName(String name) {
		return base.getColumnByName(name);
	}
	
	@Override
	public int getColumnCount() {
		return base.getColumnCount();
	}
	
	@Override
	public Class<?> getDataType(int col) {
		return base.getDataType(col);
	}
	
	@Override
	public String getDescription() {
		return base.getDescription();
	}
	
	@Override
	public String getDescription(int col) {
		return base.getDescription(col);
	}
	
	@Override
	public Long getIdForRow(int row) {
		return base.getIdForRow(row);
	}
	
	@Override
	public String getName() {
		return base.getName();
	}
	
	@Override
	public String getName(int col) {
		return base.getName(col);
	}
	
	@Override
	public String getProperty(String name) {
		return base.getProperty(name);
	}
	
	@Override
	public String getProperty(int col, String name) {
		return base.getProperty(col, name);
	}
	
	@Override
	public Set<String> getPropertyNames() {
		return base.getPropertyNames();
	}
	
	@Override
	public Set<String> getPropertyNames(int col) {
		return base.getPropertyNames(col);
	}

	@Override
	public Map<String, String> getProperties() {
		return base.getProperties();
	}
	
	@Override
	public Map<String, String> getProperties(int col) {
		return base.getProperties(col);
	}
	
	public int getRowCount() {
		return base.getRowCount();
	}
	
	@Override
	public int getRowForId(Long id) {
		return base.getRowForId(id);
	}
	
	@Override
	public String getUnits(int col) {
		return base.getUnits(col);
	}
	
	@Override
	public boolean hasRowIds() {
		return base.hasRowIds();
	}
	
	@Override
	public boolean isIndexed(int col) {
		return base.isIndexed(col);
	}
	
	//
	// DataView Methods
	public DataTable retrieveBaseTable() {
		if (base instanceof DataTable) {
			return (DataTable) base;
		} else {
			return null;
		}
	}

	public DataView retrieveBaseView() {
		if (base instanceof DataView) {
			return (DataView) base;
		} else {
			return null;
		}
	}

	public DataTableWritable retrieveBaseWritable() {
		if (base instanceof DataTableWritable) {
			return (DataTableWritable) base;
		} else {
			return null;
		}
	}

	public boolean isBasedOnView() {
		return (base instanceof DataView);
	}

	public boolean isBasedOnDataTable() {
		return (base instanceof DataTable);
	}

	public boolean isBasedOnDataTableWritable() {
		return (base instanceof DataTableWritable);
	}

}
