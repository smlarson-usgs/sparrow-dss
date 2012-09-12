package gov.usgs.cida.datatable.impl;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;

import java.util.List;
import java.util.Map;

public class SparseDoubleColumnData extends AbstractColumnData implements ColumnData {
	protected Map<Integer, Double> values;
	protected int rowCount;	//Since our data can be sparse, we need to know how many rows
	protected Double defaultMissingValue;	//value to return for missing vals (may be null)
		// ===========
		// CONSTRUCTOR
		// ===========
		public SparseDoubleColumnData(Map<Integer, Double> values, String name, String units,
				String description,	Map<String, String> properties, Map<Object, int[]> index,
				int rowCount, Double defaultMissingValue) {
			
			super(name, Double.class, units, description, properties, index);
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
			return values;
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
	}
