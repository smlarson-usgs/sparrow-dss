package gov.usgswim.datatable.impl;


import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;

public class StandardStringColumnDataWritable extends AbstractColumnDataWritable implements ColumnDataWritable {
	protected String DEFAULT_VALUE = "";

	public StandardStringColumnDataWritable(String name, String units) {
		super(name, String.class, units, null);
	}
	public void setValue(String value, int row) throws IndexOutOfBoundsException {
		BuilderHelper.fillIfNecessary(values, row, DEFAULT_VALUE);
		values.set(row, value);
	}

	public void setValue(Number value, int row) throws IndexOutOfBoundsException {
		BuilderHelper.fillIfNecessary(values, row, DEFAULT_VALUE);
		values.set(row, value.toString());
	}

	public void setValue(Object value, int row) throws IndexOutOfBoundsException {
		BuilderHelper.fillIfNecessary(values, row, DEFAULT_VALUE);
		values.set(row, value.toString());
	}
	@Override
	public boolean isValid() {
		return values != null;
	}

	public ColumnData toImmutable() {
		ColumnData result = new StandardStringColumnData(this, this.isIndexed(), DEFAULT_VALUE);
		// invalidate this instance
		index = null;
		values = null;
		invalidate();
		return result;
	}

	@Override
	protected String[] getValues() {
		if (values == null) return null;
		return values.toArray(new String[values.size()]);
	}

	// ----------------------------------
	// getXXX(int row) cell value methods
	// ----------------------------------
	public Double getDouble(int row) {
		return null;
	}
	public Integer getInt(int row) {
		return null;
	}
	public Float getFloat(int row) {
		return null;
	}
	public String getString(int row) {
		if (values == null) return null;
		
		if (row < values.size()) {
			Object value = values.get(row);
			return (value == null)? null: value.toString();
		} else {
			return null;
		}
	}
	public Object getValue(int row) {
		if (values == null) return null;
		if (row < values.size()) {
			return values.get(row);
		} else {
			return null;
		}
	}
	public Long getLong(int row) {
		return null;
	}

}
