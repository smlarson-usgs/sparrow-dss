package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.*;
import java.util.*;

/**
 *
 * @author eeverman
 */
public abstract class AbstractDataTableSet<D extends DataTable> implements DataTableSet<D> {

	protected String name;
	protected String description;
	
	public AbstractDataTableSet(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	//////////
	// DataTableSet Interface
	//////////
	
	@Override
	public String getTableName(int tableIndex) {
		return getTable(tableIndex).getName();
	}

	@Override
	public String getTableDescription(int tableIndex) {
		return getTable(tableIndex).getDescription();
	}
	
	@Override
	public Set<String> getTablePropertyNames(int tableIndex) {
		return getTable(tableIndex).getPropertyNames();
	}
		
	@Override
	public Map<String, String> getTableProperties(int tableIndex) {
		return getTable(tableIndex).getProperties();
	}
	
	@Override
	public boolean isTableValid(int tableIndex) {
		DataTable[] tables = this.getTables();
		return tables[tableIndex].isValid();
	}

	@Override
	public int getTableColumnCount(int tableIndex) {
		return getTable(tableIndex).getColumnCount();
	}
	
	@Override
	public int getTableRowCount(int tableIndex) {
		return getTable(tableIndex).getRowCount();
	}
	
	//////////
	// DataTable Interface
	//////////
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getColumnCount() {
		int cnt = 0;
		DataTable[] tables = getTables();
		for (DataTable t : tables) {
			cnt+= t.getColumnCount();
		}
		return cnt;
	}

	//
	// All of these methods just read from the first table
	//////////
	@Override
	public int getRowCount() {
		if (getTableCount() == 0) {
			return 0;
		} else {
			return getTable(0).getRowCount();
		}
	}

	@Override
	public boolean hasRowIds() {
		if (getTableCount() == 0) {
			return false;
		} else {
			return getTable(0).hasRowIds();
		}
	}

	@Override
	public String getProperty(String name) {
		if (getTableCount() == 0) {
			return null;
		} else {
			return getTable(0).getProperty(name);
		}
	}

	@Override
	public Set<String> getPropertyNames() {
		if (getTableCount() == 0) {
			return Collections.emptySet();
		} else {
			return getTable(0).getPropertyNames();
		}
	}
	
	@Override
	public Map<String, String> getProperties() {
		if (getTableCount() == 0) {
			return Collections.emptyMap();
		} else {
			return getTable(0).getProperties();
		}
	}
	
	@Override
	public Long getIdForRow(int row) {
		if (getTableCount() == 0) {
			return null;
		} else {
			return getTable(0).getIdForRow(row);
		}
	}
	
	@Override
	public int getRowForId(Long id) {
		if (getTableCount() == 0) {
			return -1;
		} else {
			return getTable(0).getRowForId(id);
		}
	}
	
	@Override
	public ColumnIndex getIndex() {
		if (getTableCount() == 0) {
			return new NoIdsColumnIndex(0);
		} else {
			return getTable(0).getIndex();
		}
	}

	/**
	 * Verifies that there is at least one table and that all tables have the
	 * same number of rows.
	 * @return 
	 */
	@Override
	public boolean isValid() {
		DataTable[] tables = getTables();
		
		if (tables.length == 0) return false;
		
		int cnt = tables[0].getRowCount();
		for (DataTable t : tables) {
			if (t.getRowCount() != cnt || t.isValid() == false)
					return false;
		}
		return true;
	}
	
	@Override
	public DataTableSetCoord getCoord(int row, int col) {
		int tblCnt = getTableCount();
		
		if (col < 0) throw new IndexOutOfBoundsException("The column index cannot be negative.");
		
		int crossedOffCols = 0;
		for (int t = 0; t < tblCnt; t++) {
			int currentTabColCnt = getTableColumnCount(t);
			
			if (crossedOffCols + currentTabColCnt > col) {
				//The request column is in the current table
				int colWithinCurrentTable = col - crossedOffCols;
				return new DataTableSetCoord(t, row, colWithinCurrentTable);
			} else {
				//not in this table
				crossedOffCols+= currentTabColCnt;
			}
		}
		
		throw new IndexOutOfBoundsException("The specified column " + col + " is beyond the number of columns available.");
	}
	
	

	@Override
	public Double getDouble(int row, int col) {
		DataTableSetCoord c = getCoord(row, col);
		return getTable(c.table).getDouble(c.row, c.col);
	}

	@Override
	public Integer getInt(int row, int col) {
		DataTableSetCoord c = getCoord(row, col);
		return getTable(c.table).getInt(c.row, c.col);
	}

	@Override
	public Float getFloat(int row, int col) {
		DataTableSetCoord c = getCoord(row, col);
		return getTable(c.table).getFloat(c.row, c.col);
	}

	@Override
	public Long getLong(int row, int col) {
		DataTableSetCoord c = getCoord(row, col);
		return getTable(c.table).getLong(c.row, c.col);
	}

	@Override
	public String getString(int row, int col) {
		DataTableSetCoord c = getCoord(row, col);
		return getTable(c.table).getString(c.row, c.col);
	}

	@Override
	public Object getValue(int row, int col) {
		DataTableSetCoord c = getCoord(row, col);
		return getTable(c.table).getValue(c.row, c.col);
	}

	@Override
	public Double getMaxDouble(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getMaxDouble(c.col);
	}

	@Override
	public Double getMaxDouble() {
		DataTable[] tables = getTables();
		
		if (tables.length == 0) return null;
		
		Double val = Double.NEGATIVE_INFINITY;
		for (DataTable t : tables) {
			Double curMax = t.getMaxDouble();
			if (curMax > val) val = curMax;
		}
		return val;
	}

	@Override
	public Double getMinDouble(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getMinDouble(c.col);
	}

	@Override
	public Double getMinDouble() {
		DataTable[] tables = getTables();
		
		if (tables.length == 0) return null;
		
		Double val = Double.POSITIVE_INFINITY;
		for (DataTable t : tables) {
			Double curVal = t.getMinDouble();
			if (curVal < val) val = curVal;
		}
		return val;
	}

	@Override
	public Integer getMaxInt(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getMaxInt(c.col);
	}

	@Override
	public Integer getMaxInt() {
		DataTable[] tables = getTables();
		
		if (tables.length == 0) return null;
		
		Integer val = Integer.MIN_VALUE;
		for (DataTable t : tables) {
			Integer curMax = t.getMaxInt();
			if (curMax > val) val = curMax;
		}
		return val;
	}

	@Override
	public Integer getMinInt(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getMinInt(c.col);
	}

	@Override
	public Integer getMinInt() {
		DataTable[] tables = getTables();
		
		if (tables.length == 0) return null;
		
		Integer val = Integer.MAX_VALUE;
		for (DataTable t : tables) {
			Integer curMin = t.getMinInt();
			if (curMin < val) val = curMin;
		}
		return val;
	}
	
	
	//DataTable Column specific methods
	//
	
	@Override
	public Map<String, String> getProperties(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getProperties(c.col);
	}

	@Override
	public String getName(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getName(c.col);
	}

	@Override
	public String getDescription(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getDescription(c.col);
	}

	@Override
	public String getUnits(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getUnits(c.col);
	}

	@Override
	public Class<?> getDataType(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getDataType(c.col);
	}

	@Override
	public String getProperty(int col, String name) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getProperty(c.col, name);
	}

	@Override
	public Set<String> getPropertyNames(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getPropertyNames(c.col);
	}

	@Override
	public ColumnData getColumn(int col) throws UnsupportedOperationException {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).getColumn(c.col);
	}

	/**
	 * REturns the first column found of the specified name
	 * @param name
	 * @return 
	 */
	@Override
	public Integer getColumnByName(String name) {
		DataTable[] tables = getTables();
		
		for (DataTable t : tables) {
			Integer c = t.getColumnByName(name);
			if (c != null) return c;
		}
		return null;
	}

	@Override
	public boolean isValid(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).isValid(c.col);
	}

	@Override
	public boolean isIndexed(int col) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).isIndexed(c.col);
	}

	@Override
	public int findFirst(int col, Object value) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).findFirst(c.col, value);
	}

	@Override
	public int findLast(int col, Object value) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).findLast(c.col, value);
	}

	@Override
	public int[] findAll(int col, Object value) {
		DataTableSetCoord c = getCoord(-1, col);
		return getTable(c.table).findAll(c.col, value);
	}
	

}
