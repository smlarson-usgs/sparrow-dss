package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;

import java.util.List;
import java.util.Map;

public class StandardIntColumnData extends AbstractColumnData implements ColumnData {
	protected int[] values;

	// ===========
	// CONSTRUCTOR
	// ===========
	
	/**
	 * Constructs a new instance, copying metadata from the passed column
	 * and the values from the passed set of values.
	 * 
	 * @param copyFromColumn The column to copy from.
	 * @param newValues The new values to use.
	 * @param nullValue The primitive value to use if the newValues contains a null.
	 * @param index
	 */
	public StandardIntColumnData(ColumnData copyFromColumn, List<?> values,
			int nullValue, Map<Object, int[]> index) {
		
		super(copyFromColumn.getName(), Integer.class, copyFromColumn.getUnits(),
				copyFromColumn.getDescription(), copyFromColumn.getProperties(), index);
		
		isValid = false;

		// copy the values. I don't think I can use generics here because of the primitives

		this.values = new int[values.size()];
		for (int i=0; i<values.size(); i++) {
			Number temp = (Number) values.get(i);
			this.values[i] = (temp == null)? nullValue: temp.intValue();
		}


		this.index = index; // Consider making a copy of the index to be really
		// safe, although I don't see it as necessary.
		
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
	public StandardIntColumnData(int[] values, String name, String units,
			String description,	Map<String, String> properties, boolean createIndex) {
		
		super(name, Integer.class, units, description, properties, null);
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
	public StandardIntColumnData(ColumnData copyFromColumn, boolean createIndex, int nullValue) {
		super(copyFromColumn.getName(), Integer.class, copyFromColumn.getUnits(),
				copyFromColumn.getDescription(), copyFromColumn.getProperties(), null);
		isValid = false;

		
		int rowCount = copyFromColumn.getRowCount();
		values = new int[rowCount];
		
		for (int r = 0; r < rowCount; r++) {
			if (copyFromColumn.getValue(r) != null) {
				values[r] = copyFromColumn.getInt(r);
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
		return isValid;
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
		return values[row];
	}
	public Float getFloat(int row) {
		return Integer.valueOf(values[row]).floatValue();
	}
	public String getString(int row) {
		return Integer.valueOf(values[row]).toString();
	}
	public Object getValue(int row) {
		return values[row];
	}
	public Long getLong(int row) {
		return Long.valueOf(values[row]);
	}

}
