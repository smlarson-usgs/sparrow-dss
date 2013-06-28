package gov.usgs.cida.datatable.impl;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;
import java.util.Collections;

import java.util.List;
import java.util.Map;

/**
 * Holds a sparse hashmap of values for uses where the data is mostly empty.
 * Note that the constructor keeps a ref to the passed in map.
 * @author eeverman
 */

public class SparseDoubleColumnDataWritable extends AbstractColumnDataWritable implements ColumnDataWritable {
	//hiding fields
	protected String units;
	protected String name;

	protected Map<Integer, Double> values;
	protected int rowCount;	//Since our data can be sparse, we need to know how many rows
	protected Double defaultMissingValue;	//value to return for missing vals (may be null)
		// ===========
		// CONSTRUCTOR
		// ===========
		public SparseDoubleColumnDataWritable(Map<Integer, Double> values, String name, String units,
				String description,	Map<String, String> properties, Map<Object, int[]> index,
				int rowCount, Double defaultMissingValue) {

			super(name, Double.class, units, properties);
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

		@Override
		protected Object getValues() {
			return Collections.unmodifiableMap(values);
		}

		// ----------------------------------
		// getXXX(int row) cell value methods
		// ----------------------------------
		public Double getDouble(int row) {

			if (values.containsKey(row)) {
				return values.get(row);
			} else {
				return defaultMissingValue;
			}

		}
		public Integer getInt(int row) {
			if (values.containsKey(row)) {
				return values.get(row).intValue();
			} else {
				if (defaultMissingValue != null) {
					return defaultMissingValue.intValue();
				} else {
					return null;
				}
			}
		}
		public Float getFloat(int row) {
			if (values.containsKey(row)) {
				return values.get(row).floatValue();
			} else {
				if (defaultMissingValue != null) {
					return defaultMissingValue.floatValue();
				} else {
					return null;
				}
			}
		}
		public String getString(int row) {
			if (values.containsKey(row)) {
				return values.get(row).toString();
			} else {
				if (defaultMissingValue != null) {
					return defaultMissingValue.toString();
				} else {
					return null;
				}
			}
		}
		public Object getValue(int row) {
			return getDouble(row);
		}

		public Long getLong(int row) {
			if (values.containsKey(row)) {
				return values.get(row).longValue();
			} else {
				if (defaultMissingValue != null) {
					return defaultMissingValue.longValue();
				} else {
					return null;
				}
			}
		}
	public void setRowCount(int rowCount){
		this.rowCount = rowCount;
	}
	@Override
	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}

	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}

	@Override
	public void setValue(String value, int row) throws IndexOutOfBoundsException {
		throw new UnsupportedOperationException("Type not supported."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setValue(Number value, int row) throws IndexOutOfBoundsException {
		this.setValue(value.doubleValue(), row);
	}
	public void setValue(Double value, int row) throws IndexOutOfBoundsException {
		this.values.put(row, value.doubleValue());
	}
	@Override
	public void setValue(Object value, int row) throws IndexOutOfBoundsException {
		throw new UnsupportedOperationException("Type not supported"); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void buildIndex() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	}
