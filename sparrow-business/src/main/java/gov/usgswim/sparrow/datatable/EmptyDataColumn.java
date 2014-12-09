package gov.usgswim.sparrow.datatable;


public class EmptyDataColumn extends SparrowColumnSpecifier {

	protected String units;
	protected String constituent;
	
	public EmptyDataColumn(Integer contextId, String units, String constituent, Long modelId) {
		super(null, -1, contextId, modelId);
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
