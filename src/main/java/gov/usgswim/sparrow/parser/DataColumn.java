package gov.usgswim.sparrow.parser;

import gov.usgswim.datatable.DataTable;

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

}