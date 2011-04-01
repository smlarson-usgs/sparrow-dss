package gov.usgswim.sparrow.datatable;

import java.util.Map;

import java.util.Arrays;

import gov.usgswim.datatable.ColumnAttribs;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.AbstractColumnData;

public class SingleValueDoubleColumnData extends AbstractColumnData {

	private static final long serialVersionUID = 1L;
	private double valueForEveryRow;
	private int numberOfRows;
	
	public SingleValueDoubleColumnData(double valueForEveryRow,
			int numberOfRows, String name,
			String units, String desc, Map<String, String> properties) {
		
		super(name, Double.class, units, desc, properties, null);
		
		this.valueForEveryRow = valueForEveryRow;
		this.numberOfRows = numberOfRows;
	}
	
	public SingleValueDoubleColumnData(double valueForEveryRow,
			int numberOfRows, ColumnAttribs attribs) {
		
		super(attribs.getName(), Double.class, attribs.getUnits(),
				attribs.getDescription(), attribs.getProperties(null), null);
		
		this.valueForEveryRow = valueForEveryRow;
		this.numberOfRows = numberOfRows;
	}

	@Override
	protected Object getValues() {
		double[] values = new double[numberOfRows];
		Arrays.fill(values, valueForEveryRow);
		return values;
	}

	@Override
	public Double getDouble(int row) {
		//No reason to check for out-of-bounds
		return valueForEveryRow;
	}

	@Override
	public Float getFloat(int row) {
		return (float) valueForEveryRow;
	}

	@Override
	public Integer getInt(int row) {
		return (int) valueForEveryRow;
	}

	@Override
	public Long getLong(int row) {
		return (long) valueForEveryRow;
	}

	@Override
	public Integer getRowCount() {
		return numberOfRows;
	}

	@Override
	public String getString(int row) {
		return Double.toString(valueForEveryRow);
	}

	@Override
	public Object getValue(int row) {
		return Double.valueOf(valueForEveryRow);
	}

	@Override
	public ColumnData toImmutable() {
		return this;
	}

}
