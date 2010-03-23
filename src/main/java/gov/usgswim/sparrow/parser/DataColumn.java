package gov.usgswim.sparrow.parser;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.datatable.TableProperties;

/**
 * An inner class to bundle a DataTable and a column index together so that
 * it is possible to return these two together for methods returning the
 * data column.
 *
 * @author eeverman
 *
 */
public class DataColumn {
	private final DataTable table;
	private final int column;
	private Integer contextId;

	public DataColumn(DataTable table, int column, Integer contextId) {
		this.table = table;
		this.column = column;
		this.contextId = contextId;
	}

	public DataTable getTable() {
		return table;
	}

	public int getColumn() {
		return column;
	}

	public Integer getContextId() {
		return contextId;
	}
	
	/**
	 * Shortcut to get a double value w/o digging to the table.
	 * @param row
	 * @return
	 */
	public Double getDouble(int row) {
		return table.getDouble(row, column);
	}
	
	/**
	 * Shortcut to determine if there are row IDs w/o digging to the table.
	 * @return
	 */
	public boolean hasRowIds() {
		return table.hasRowIds();
	}
	
	/**
	 * Shortcut to get a row ID w/o digging to the table.
	 * @param row
	 * @return
	 */
	public Long getIdForRow(int row) {
		return table.getIdForRow(row);
	}
	
	/**
	 * Shortcut to get the units of data column w/o digging to the table.
	 * @return
	 */
	public String getUnits() {
		return table.getUnits(column);
	}
	
	/**
	 * Returns the thing being measured.
	 * @return
	 */
	public String getConstituent() {
		return table.getProperty(TableProperties.CONSTITUENT.name());
	}
	
	/**
	 * Shortcut to get the name of data column w/o digging to the table.
	 * @return
	 */
	public String getColumnName() {
		return table.getName(column);
	}
	
	/**
	 * Shortcut to get a property of data table w/o digging to the table.
	 * @param name
	 * @return
	 */
	public String getTableProperty(String name) {
		return table.getProperty(name);
	}
	
	/**
	 * Shortcut to the table rowCount.
	 * @return
	 */
	public int getRowCount() {
		return table.getRowCount();
	}
	
	/**
	 * Shortcut to the table columnCount.
	 * @return
	 */
	public int getColumnCount() {
		return table.getColumnCount();
	}

}