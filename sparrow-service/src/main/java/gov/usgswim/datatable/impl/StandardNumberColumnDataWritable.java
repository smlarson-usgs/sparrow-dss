package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;

import java.util.ArrayList;
import java.util.List;
public class StandardNumberColumnDataWritable<T extends Number>
	extends AbstractColumnDataWritable
	implements ColumnDataWritable {


	/**
	 * static variable to control whether or not instances of this class
	 * will fail silently or throw type exceptions
	 */
	public static volatile boolean DO_NOT_THROW_TYPE_EXCEPTIONS = false;

	protected List<T> values = new ArrayList<T>(); // hides parent AbstractColumnDataWritable.values
	protected T DEFAULT_VALUE;
	protected Class<?> type; // hides parent type field

	public StandardNumberColumnDataWritable() {
		super(null, Number.class, null, null);
	}

	public StandardNumberColumnDataWritable(String name, String units) {
		super(name, Number.class, units, null);
	}

	public StandardNumberColumnDataWritable(String name, String units, Class<T> clazz) {
		super(name, Number.class, units, null);
		this.type = clazz;
	}

	public void setValue(String value, int row) {
		throw new UnsupportedOperationException("Number types only accepted");
	}

	@SuppressWarnings("unchecked")
	public void setValue(Number value, int row)  throws IndexOutOfBoundsException {
		BuilderHelper.fillIfNecessary(values, row, DEFAULT_VALUE);
		if (this.type == null) {
			values.set(row, (T) value);
			// Since this is the first entry, infer the type. Note that this should
			// REALLY be the .TYPE, but the operations are easier using the
			// wrapper class than the primitive TYPE class
			this.type = value.getClass();
		} else if (this.type.isInstance(value)) {
			values.set(row, (T)value);
		} else {
			values.set(row, (T) BuilderHelper.convertNumber(this.type, value));
		}
	}

	public void setValue(Object value, int row) throws IndexOutOfBoundsException {
		if (value instanceof Number) {
			setValue((Number) value, row);
		} else {
			throw new UnsupportedOperationException("Number types only accepted");
		}
	}

	@Override
	public Integer getRowCount() {
		return values.size();
	}

	@Override
	public boolean isValid() {
		return values != null;
	}

	public StandardNumberColumnDataWritable<T> setType(Class<?> type){
		this.type = type;
		return this;
	}


	public ColumnData toImmutable() {
		
		double defaultValue = 0d;
		
		if (DEFAULT_VALUE != null) {
			defaultValue = DEFAULT_VALUE.doubleValue();
		}
		
		ColumnData result = null;
		if (this.type == Float.class) {
			result = new StandardFloatColumnData(this, values, (float)defaultValue, index);
		} else if (this.type == Integer.class) {
			result = new StandardIntColumnData(this, values, (int)defaultValue, index);
		} else if (this.type == Double.class) {
			result = new StandardDoubleColumnData(this, values, defaultValue, index);
		} else if (this.type == Long.class) {
			result = new StandardLongColumnData(this, values, (long)defaultValue, index);
		} else {
			// TODO add other primitive types
			// not recognized, so return null and DON'T invalidate this instance
			return null;
		}

		// invalidate this instance
		values = null;
		invalidate();
		return result;
	}

	/**
	 * @see gov.usgswim.datatable.impl.AbstractColumnData#getDataType()
	 * @throws UnsupportedOperationException
	 *             if the type has not been defined either explicitly or
	 *             implicitly.
	 */
	@Override
	public Class<?> getDataType() throws UnsupportedOperationException{
		if (this.type == null && values != null) {
			if (values.size() > 0) {
				this.type = values.get(0).getClass();
			} else {
				if (!DO_NOT_THROW_TYPE_EXCEPTIONS) {
					throw new UnsupportedOperationException(
							"dataType of a generic column cannot be read"
							+ " unless it has been set explicitly or"
							+ " until it has some non empty elements");
				}
			}
		}
		return this.type;
	};

	@Override
	protected Object getValues() {
		return this.values.toArray(new Number[values.size()]);
	};

	@Override
	protected List<T> getValuesList() {
		return this.values;
	}



	// ----------------------------------
	// getXXX(int row) cell value methods
	// ----------------------------------
	public Double getDouble(int row) {
		if (row < values.size()) {
			return values.get(row).doubleValue();
		} else {
			return null;
		}
	}
	public Integer getInt(int row) {
		if (row < values.size()) {
			return values.get(row).intValue();
		} else {
			return null;
		}
	}
	public Float getFloat(int row) {
		if (row < values.size()) {
			return values.get(row).floatValue();
		} else {
			return null;
		}
	}
	public Object getValue(int row) {
		if (row < values.size()) {
			return values.get(row);
		} else {
			return null;
		}
	}
	public String getString(int row) {
		if (row < values.size()) {
			return values.get(row).toString();
		} else {
			return null;
		}
	}
	public Long getLong(int row) {
		if (row < values.size()) {
			return values.get(row).longValue();
		} else {
			return null;
		}
	}


}
