package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.ColumnData;

import java.util.List;
import java.util.Map;
public class StandardLongColumnData extends AbstractColumnData implements ColumnData {
		protected long[] values;


		/**
		 * Constructs a new instance, copying metadata from the passed column
		 * and the values from the passed set of values.
		 * 
		 * @param copyFromColumn The column to copy from.
		 * @param newValues The new values to use.
		 * @param nullValue The primitive value to use if the newValues contains a null.
		 * @param index
		 */
		public StandardLongColumnData(ColumnData copyFromColumn, List<?> newValues,
				long nullValue, Map<Object, int[]> index) {
			
			super(copyFromColumn.getName(), Long.class,
					copyFromColumn.getUnits(), copyFromColumn.getDescription(),
					copyFromColumn.getProperties(), index);
			
			isValid = false;

			this.values = new long[newValues.size()];
			for (int i=0; i<newValues.size(); i++) {
				Number temp = (Number)newValues.get(i);
				this.values[i] = (temp == null)? nullValue: temp.longValue();
			}

			isValid = true;
		}
		
		/**
		 * Create a new column, specifying all data.
		 * 
		 * @param values The values to use.
		 * @param name The name of the column.
		 * @param units the units of the column.
		 * @param description
		 * @param properties
		 * @param createIndex If true, an index of the values is created.
		 */
		public StandardLongColumnData(long[] values, String name, String units,
				String description,	Map<String, String> properties, boolean createIndex) {
			
			super(name, Long.class, units, description, properties, null);
			
			isValid = false;
			
			this.values = values;

			if (createIndex) {
				index = buildIndex(this);
			}
			
			isValid = true;
		}
		
		/**
		 * Creates a new instance copied from the passed writable instance.
		 * The data is detached and stored as primitive longs.
		 * 
		 * @param copyFromColumn A column to copy data and metadata from
		 * @param createIndex If true, an index is built for the data.
		 * @param nullValue The primitive value to use if the passed column contains a null.
		 */
		public StandardLongColumnData(ColumnData copyFromColumn, boolean createIndex, long nullValue) {
			super(copyFromColumn.getName(), Long.class, copyFromColumn.getUnits(),
					copyFromColumn.getDescription(), copyFromColumn.getProperties(), null);
			isValid = false;

			
			int rowCount = copyFromColumn.getRowCount();
			values = new long[rowCount];
			
			for (int r = 0; r < rowCount; r++) {
				if (copyFromColumn.getValue(r) != null) {
					values[r] = copyFromColumn.getLong(r);
				} else {
					values[r] = nullValue;
				}
			}
			
			if (createIndex) {
				index = buildIndex(this);
			}

			isValid = true;
		}

		// ================
		// INSTANCE METHODS
		// ================
		public Integer getRowCount() {
			return values.length;
		}

		@Override
		public boolean isValid() {
			// TODO Auto-generated method stub
			return false;
		}


		public ColumnData toImmutable() {
			return this;
		}

		@Override
		protected Object getValues() {
			return values;
		}

		// ----------------------------------
		// getXXX(int row) cell value methods
		// ----------------------------------
		public Double getDouble(int row) {
			return Double.valueOf(values[row]);
		}

		public Integer getInt(int row) {
			return Long.valueOf(values[row]).intValue();
		}

		public Float getFloat(int row) {
			return Long.valueOf(values[row]).floatValue();
		}
		public String getString(int row) {
			return Long.valueOf(values[row]).toString();
		}

		public Object getValue(int row) {
			return values[row];
		}
		public Long getLong(int row) {
			return values[row];
		}


	}

