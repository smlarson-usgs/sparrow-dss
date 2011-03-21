package gov.usgswim.datatable;

public interface ColumnDataWritable extends ColumnData {

	// -----------------------------
	// METAINFO MODIFICATION METHODS
	// -----------------------------
	public void setUnits(String units);
	public void setName(String name);
	public void setProperty(String key, String value);
	public void setDescription(String desc);
	
	// -------------------------
	// DATA MODIFICATION METHODS
	// -------------------------
	public void setValue(String value, int row) throws IndexOutOfBoundsException;
	public void setValue(Number value, int row) throws IndexOutOfBoundsException;
	public void setValue(Object value, int row) throws IndexOutOfBoundsException;
	
	// ---------------
	public void buildIndex();

}
