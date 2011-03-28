package gov.usgswim.datatable.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.ColumnDataFromTable;

public class ManipulableView implements DataTable {

	private static final long serialVersionUID = 1L;

	public enum AGGREGATION_MODE{
		AVERAGE,
		MIN,
		MAX,
		MEDIAN,
		STDDEV
	}

	protected DataTable source;
	protected List<double[]> multiplicativeWeights = new ArrayList<double[]>();
	protected List<double[]> divisionWeights = new ArrayList<double[]>();
	protected double[] consolidatedWeight;
	protected String[] groupByValues;
	protected AGGREGATION_MODE mode;


	public ManipulableView(DataTable source) {
		this.source = source;
	}

	// =========================
	// VIEW MANIPULATION METHODS
	// =========================
	public void applyMultiplicativeWeights(double[]... doubleWeights) {
		for (double[] weight: doubleWeights) {
			if (weight.length == source.getRowCount()) {
				multiplicativeWeights.add(weight);
			} else {
				throw new IllegalArgumentException(
						"size of weight: " + weight.length
						+ " does not match size of source: "
						+ source.getRowCount());
			}
		}
	}

	public void applyDivisionWeights(double[]... doubleWeights) {
		for (double[] weight: doubleWeights) {
			if (weight.length == source.getRowCount()) {
				divisionWeights.add(weight);
			} else {
				throw new IllegalArgumentException("size of weight: " + weight.length + " does not match size of source: " + source.getRowCount());
			}
		}
	}

	public void applyGroupBy(String[] groupByValues, AGGREGATION_MODE mode) {
		this.groupByValues = groupByValues;
		this.mode = mode;
		// Note that no order is specified. This may yet be important
	}

	/**
	 * Call this method once before any of the getXXXValue() methods but after
	 * adding the weights.
	 *
	 */
	public void consolidateWeights() {
		if (consolidatedWeight == null) {
			consolidatedWeight = new double[source.getRowCount()];
			for (int row = 0; row < source.getRowCount(); row++) {
				double cWeight = 1;
				for (double[] weight: multiplicativeWeights) {
					cWeight *= weight[row];
				}
				for (double[] weight: divisionWeights) {
					cWeight /= weight[row];
				}
				consolidatedWeight[row] = cWeight;
			}
		}
	}

	public void reset() {
		multiplicativeWeights.clear();
		divisionWeights.clear();
		consolidatedWeight = null;
		groupByValues = null;
		AGGREGATION_MODE mode = null;
	}

	public void aggregate() {

	}


	// ================
	// GETVALUE METHODS
	// ================
	@Override
	public Double getDouble(int row, int col) {
		return consolidatedWeight[row] * source.getDouble(row, col);
	}

	@Override
	public Float getFloat(int row, int col) {return getDouble(row, col).floatValue();}

	@Override
	public Integer getInt(int row, int col) {return getDouble(row, col).intValue();}

	@Override
	public Long getLong(int row, int col) {return getDouble(row, col).longValue();}

	@Override
	public String getString(int row, int col) {
		// String values are not weighted
		return source.getString(row, col);
	}

	@Override
	public Object getValue(int row, int col) {
		Object value = source.getValue(row, col);
		if (value instanceof String) {
			return value;
		}
		// TODO this is not strictly correct, but not sure if there really is a correct way to handle it.
		return getDouble(row, col);
	}

	public Double getAggregateDouble(int row, int col) {
		throw new UnsupportedOperationException("implement this!");
	}

	public Float getAggregateFloat(int row, int col) {return getAggregateDouble(row, col).floatValue();}

	public Integer getAggregateInt(int row, int col) {return getAggregateDouble(row, col).intValue();}

	public Long getAggregateLong(int row, int col) {return getAggregateDouble(row, col).longValue();}

	public String getAggregateString(int row, int col) {
		// String values are not weighted
		return source.getString(row, col);
	}

	public Object getAggregateValue(int row, int col) {
		Object value = source.getValue(row, col);
		if (value instanceof String) {
			return value;
		}
		// TODO this is not strictly correct, but not sure if there really is a correct way to handle it.
		return getAggregateDouble(row, col);
	}
	// =================
	// DELEGATED METHODS
	// =================
	
	@Override
	public ColumnData getColumn(int colIndex) {
		
		if (colIndex < 0 || colIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Requested column index does not exist.");
		}
		
		ColumnDataFromTable col = new ColumnDataFromTable(this, colIndex);
		return col;
	}
	
	@Override
	public Integer getColumnByName(String name) {return source.getColumnByName(name);}

	@Override
	public int getColumnCount() {return source.getColumnCount();}

	@Override
	public Class<?> getDataType(int col) {return source.getDataType(col);}

	@Override
	public String getDescription() {return source.getDescription();}

	@Override
	public String getDescription(int col) {return source.getDescription(col);}

	@Override
	public Long getIdForRow(int row) {return source.getIdForRow(row);}

	@Override
	public String getName() {return source.getName();}

	@Override
	public String getName(int col) {return source.getName(col);}

	@Override
	public String getProperty(int col, String name) {return source.getProperty(col, name);}

	@Override
	public String getProperty(String name) {return source.getProperty(name);}

	@Override
	public Set<String> getPropertyNames() {return source.getPropertyNames();}

	@Override
	public Set<String> getPropertyNames(int col) {return source.getPropertyNames(col);}

	@Override
	public Map<String, String> getProperties() { return source.getProperties(); }

	@Override
	public Map<String, String> getProperties(int col) { return source.getProperties(col); }
	
	@Override
	public int getRowCount() {return source.getRowCount();}

	@Override
	public int getRowForId(Long id) {return source.getRowForId(id);}

	@Override
	public String getUnits(int col) {return source.getUnits(col);}

	@Override
	public boolean hasRowIds() {return source.hasRowIds();}

	@Override
	public boolean isIndexed(int col) {return source.isIndexed(col);}

	@Override
	public boolean isValid() {return source.isValid();}
	
	@Override
	public boolean isValid(int columnIndex) { return source.isValid(columnIndex);}

	// ===================
	// UNSUPPORTED METHODS
	// ===================
	@Override
	public int[] findAll(int col, Object value) {
		throw new UnsupportedOperationException("findAll() not supported on a view");
	}

	@Override
	public int findFirst(int col, Object value) {
		throw new UnsupportedOperationException("findFirst() not supported on a view");
	}

	@Override
	public int findLast(int col, Object value) {
		throw new UnsupportedOperationException("findLast() not supported on a view");
	}

	@Override
	public Double getMaxDouble(int col) {
		throw new UnsupportedOperationException("getMaxDouble() not supported on a view");
	}

	@Override
	public Double getMaxDouble() {
		throw new UnsupportedOperationException("getMaxDouble() not supported on a view");
	}

	@Override
	public Integer getMaxInt(int col) {
		throw new UnsupportedOperationException("getMaxInt() not supported on a view");
	}

	@Override
	public Integer getMaxInt() {
		throw new UnsupportedOperationException("getMaxInt() not supported on a view");
	}

	@Override
	public Double getMinDouble(int col) {
		throw new UnsupportedOperationException("getMinDouble() not supported on a view");
	}

	@Override
	public Double getMinDouble() {
		throw new UnsupportedOperationException("getMinDouble() not supported on a view");
	}

	@Override
	public Integer getMinInt(int col) {
		throw new UnsupportedOperationException("findLast() not supported on a view");
	}

	@Override
	public Integer getMinInt() {
		throw new UnsupportedOperationException("getMinInt() not supported on a view");
	}

	@Override
	public Immutable toImmutable() {
		throw new UnsupportedOperationException("toImmutable() not supported on a view");
	}

}
