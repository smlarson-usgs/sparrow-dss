package gov.usgswim.sparrow.datatable;

import java.util.Map;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.AbstractColumnData;

/**
 * This is a wrapper datacolumn that takes its data as a double[][] and a column
 * index into that array.  It does not modify the passed in array and holds a
 * reference to it.  Thus, immutability is coorperative w/ the caller.
 * 
 * 
 * @author eeverman
 *
 */
public class ImmutableDoubleColumn extends AbstractColumnData {

	private final double[][] values;	//One column in this contains the data for this column
	private final int dataCol;	//The column containing the data
	
	public ImmutableDoubleColumn(double[][] values, int dataCol, String name, String units,
	    String desc, Map<String, String> properties) {
		
		super(name, Double.class, units, desc, properties, null);
		
		this.values = values;
		this.dataCol = dataCol;
	}

	protected Object getValues() {
		throw new UnsupportedOperationException();
	}

	public Double getDouble(int row) {
		return values[row][dataCol];
	}

	public Float getFloat(int row) {
		return (float) values[row][dataCol];
	}

	public Integer getInt(int row) {
		return (int) values[row][dataCol];
	}

	public Long getLong(int row) {
		return (long) values[row][dataCol];
	}

	public Integer getRowCount() {
		return values.length;
	}

	public String getString(int row) {
		return Double.toString(values[row][dataCol]);
	}

	public Object getValue(int row) {
		return values[row][dataCol];
	}

	public ColumnData toImmutable() {
		return this;
	}

}
