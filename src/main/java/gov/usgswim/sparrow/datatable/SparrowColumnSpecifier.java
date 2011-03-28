package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable;

/**
 * An inner class to bundle a DataTable and a column index together so that
 * it is possible to return these two together for methods returning the
 * data column.
 *
 * @author eeverman
 *
 */
public class SparrowColumnSpecifier {
	private final DataTable table;
	private final int column;
	private Integer contextId;	//Defined when we have a context
	private Long modelId;	//Defined only when we have a modelID but no context

	public SparrowColumnSpecifier(DataTable table, int column, Integer contextId) {
		this.table = table;
		this.column = column;
		this.contextId = contextId;
	}
	
	public SparrowColumnSpecifier(DataTable table, int column, Integer contextId, Long modelId) {
		this.table = table;
		this.column = column;
		this.contextId = contextId;
		this.modelId = modelId;
	}

	public DataTable getTable() {
		return table;
	}

	public int getColumn() {
		return column;
	}

	/**
	 * The PredictionContext ID this datacolumn is for.
	 * 
	 * It may be null in the case we are holding data that is model specific but
	 * not context specific, such as stream flow or other attribute data.
	 * @return
	 */
	public Integer getContextId() {
		return contextId;
	}
	
	/**
	 * The SparrowModel ID this datacolumn is for.
	 * 
	 * It may be null in the case we are holding data that is context specific,
	 * such as predicted data.  If we have a contextID, the model ID can be null.
	 * @return
	 */
	public Long getModelId() {
		return modelId;
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
		return table.getProperty(column, TableProperties.CONSTITUENT.getPublicName());
	}
	
	/**
	 * Shortcut to get the description of data column w/o digging to the table.
	 * @return
	 */
	public String getDescription() {
		return table.getDescription(column);
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