package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.ColumnData;
import java.util.Collections;

import java.util.Map;

/**
 * Holds a sparse hashmap of values for uses where the data is mostly empty.
 * Note that the constructor keeps a ref to the passed in hashmap.
 * @author eeverman
 */
public class SparseStringColumnData extends AbstractColumnData implements ColumnData {

	protected Map<Integer, String> values;
	protected int rowCount;	//Since our data can be sparse, we need to know how many rows
	protected String defaultMissingValue;	//value to return for missing vals (may be null)
	// ===========
	// CONSTRUCTOR
	// ===========

	public SparseStringColumnData(Map<Integer, String> values, String name, String units,
					String description, Map<String, String> properties, Map<Object, int[]> index,
					int rowCount, String defaultMissingValue) {

		super(name, String.class, units, description, properties, index);
		isValid = false;
		this.rowCount = rowCount;
		this.defaultMissingValue = defaultMissingValue;

		this.values = values;

		// safe, although I don't see it as necessary.
		isValid = true;
	}

	// ================
	// INSTANCE METHODS
	// ================
	public Integer getRowCount() {
		return rowCount;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	public ColumnData toImmutable() {
		return this;
	}

	//TODO:  This method should not be part of this class if it is immutable
	@Override
	protected Object getValues() {
		return Collections.unmodifiableMap(values);
	}

	// ----------------------------------
	// getXXX(int row) cell value methods
	// ----------------------------------
	public Double getDouble(int row) {
		if (values.containsKey(row)) {
			try {
				return Double.valueOf(values.get(row));
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public Integer getInt(int row) {
		if (values.containsKey(row)) {
			try {
				return Integer.valueOf(values.get(row));
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public Float getFloat(int row) {
		if (values.containsKey(row)) {
			try {
				return Float.valueOf(values.get(row));
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getString(int row) {
		if (values.containsKey(row)) {
			return values.get(row);
		} else {
			return defaultMissingValue;
		}
	}

	public Object getValue(int row) {
		return getDouble(row);
	}

	public Long getLong(int row) {
		if (values.containsKey(row)) {
			try {
				return Long.valueOf(values.get(row));
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}
