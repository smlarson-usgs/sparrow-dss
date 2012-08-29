package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.ColumnIndex;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.HashMapColumnIndex;

import java.util.Map;
import java.util.Set;

public abstract class AbstractDataTableBase implements DataTable {

	protected final DataTable base;

	public AbstractDataTableBase(DataTable baseTable) {
		super();
		base = baseTable;
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public abstract Double getMaxDouble(int col);
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public abstract Double getMaxDouble();

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public abstract Double getMinDouble(int col);
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public abstract Double getMinDouble();
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public abstract Object getValue(int row, int col);
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public abstract Double getDouble(int row, int col);
	
	
	//
	//The find methods are not abstract but are likely to be overridden if the
	//subclass is presenting a modified view of the base data which is can be
	//meaningfully searched.
	//
	@Override
	public int[] findAll(int col, Object value) {
		return base.findAll(col, value);
	}

	@Override
	public int findFirst(int col, Object value) {
		return base.findFirst(col, value);
	}

	@Override
	public int findLast(int col, Object value) {
		return base.findLast(col, value);
	}
	
	//These getMax/getMin methods are safe to leave as-is - just just delegate
	//to the corresponding getXDouble methods.
	@Override
	public Integer getMaxInt(int col) {
		return getMaxDouble(col).intValue();
	}
	
	@Override
	public Integer getMaxInt() {
		return getMaxDouble().intValue();
	}

	@Override
	public Integer getMinInt(int col) {
		return getMinDouble(col).intValue();
	}

	@Override
	public Integer getMinInt() {
		return getMinDouble().intValue();
	}
	
	public ColumnIndex getIndex() {
		if (base instanceof DataTable.Immutable) {
			return ((DataTable.Immutable) base).getIndex();
		} else {
			return new HashMapColumnIndex(base);
		}
	}
	
	//
	//The getValue type methods are generally safe to leave since they delegate
	//to getDouble()
	
	/**
	 * This implementation returns the integer value of getDouble.
	 * {@inheritDoc} 
	 */
	@Override
	public Integer getInt(int row, int col) {
		return getDouble(row, col).intValue();
	}

	/**
	 * This implementation returns the float value of getDouble.
	 * {@inheritDoc} 
	 */
	@Override
	public Float getFloat(int row, int col) {
		return getDouble(row, col).floatValue();
	}
	
	/**
	 * This implementation returns the long value of getDouble.
	 * {@inheritDoc} 
	 */
	@Override
	public Long getLong(int row, int col) {
		return getDouble(row, col).longValue();
	}
	
	/**
	 * This implementation returns the string value of getDouble.
	 * {@inheritDoc} 
	 */
	@Override
	public String getString(int row, int col) {
		return getDouble(row, col).toString();
	}
	
	
	//
	//The metadata methods are generally safe to leave as is.
	//Note:  Watch out for the isIndexed() method, which may be incorrect
	//if a values are modified in the view.
	//Also note that isValid() only checks the base table.
	//
	@Override
	public Integer getColumnByName(String name) {
		return base.getColumnByName(name);
	}

	@Override
	public int getColumnCount() {
		return base.getColumnCount();
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
	public int getRowCount() {
		return base.getRowCount();
	}

	@Override
	public int getRowForId(Long id) {
		return base.getRowForId(id);
	}
	
	@Override
	public boolean hasRowIds() {
		return base.hasRowIds();
	}

	@Override
	public boolean isIndexed(int col) {
		return base.isIndexed(col);
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
	public Class<?> getDataType(int col) {
		return base.getDataType(col);
	}

	@Override
	public String getUnits(int col) {
		return base.getUnits(col);
	}

	@Override
	public Map<String, String> getProperties() {
		return base.getProperties();
	}
	@Override
	public Map<String, String> getProperties(int col) {
		return base.getProperties(col);
	}

}