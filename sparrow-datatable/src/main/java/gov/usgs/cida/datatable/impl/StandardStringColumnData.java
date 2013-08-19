package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.ColumnData;

import java.util.List;
import java.util.Map;

public class StandardStringColumnData extends AbstractColumnData implements ColumnData {
	protected String[] values;

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
	public StandardStringColumnData(ColumnData copyFromColumn, List<?> values,
			String nullValue, Map<Object, int[]> index) {
		
		super(copyFromColumn.getName(), String.class, copyFromColumn.getUnits(),
				copyFromColumn.getDescription(), copyFromColumn.getProperties(), index);
		
		isValid = false;

		this.values = new String[values.size()];
		for (int i=0; i<values.size(); i++) {
			Object temp = values.get(i);
			if (temp != null) {
				this.values[i].toString();
			} else {
				this.values[i] = nullValue;
			}
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
	public StandardStringColumnData(String[] values, String name, String units,
			String description,	Map<String, String> properties, boolean createIndex) {
		
		super(name, String.class, units, description, properties, null);
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
	public StandardStringColumnData(ColumnData copyFromColumn, boolean createIndex, String nullValue) {
		super(copyFromColumn.getName(), String.class, copyFromColumn.getUnits(),
				copyFromColumn.getDescription(), copyFromColumn.getProperties(), null);
		isValid = false;

		
		int rowCount = copyFromColumn.getRowCount();
		values = new String[rowCount];
		
		for (int r = 0; r < rowCount; r++) {
			Object temp = copyFromColumn.getValue(r);
			if (temp != null) {
				values[r] = temp.toString();
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
		return (values != null);
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
		return null;
	}
	public Integer getInt(int row) {
		return null;
	}
	public Float getFloat(int row) {
		return null;
	}
	public String getString(int row) {
		return values[row];
	}
	public Object getValue(int row) {
		return values[row];
	}
	public Long getLong(int row) {
		return null;
	}



}
