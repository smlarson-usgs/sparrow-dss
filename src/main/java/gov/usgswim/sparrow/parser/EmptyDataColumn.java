package gov.usgswim.sparrow.parser;

import gov.usgswim.datatable.DataTable;

public class EmptyDataColumn extends DataColumn {

	protected String units;
	protected String constituent;
	
	public EmptyDataColumn(Integer contextId, String units, String constituent) {
		super(null, -1, contextId);
		this.units = units;
		this.constituent = constituent;
	}

	@Override
	public int getColumnCount() {
		return 0;
	}

	@Override
	public String getColumnName() {
		return "";
	}

	@Override
	public Double getDouble(int row) {
		return null;
	}

	@Override
	public Long getIdForRow(int row) {
		return null;
	}

	@Override
	public int getRowCount() {
		return 0;
	}

	@Override
	public String getTableProperty(String name) {
		return null;
	}

	@Override
	public String getUnits() {
		return units;
	}
	
	@Override
	public String getConstituent() {
		return constituent;
	}

	@Override
	public boolean hasRowIds() {
		return false;
	}

}
