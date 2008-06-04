package gov.usgswim.sparrow.datatable;

import gov.usgswim.NotThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.FindHelper;
import gov.usgswim.datatable.impl.SimpleDataTableImmutable;

import java.util.Arrays;
import java.util.Set;

/**
 * A Data2D implementation that derives its from a comparison between two
 * Data2D instances.
 * 
 * The ID values (if any) of the first Data2D (the baseData) is the one used
 * for ID lookups.
 *
 * Comparisons are always in terms of the baseData - compData.
 * 
 * @deprecated This class does not properly support the getInt/Double methods
 * so that other views can extend it.  Other views will only return
 * TODO [IK] Kill this class. Not needed
 */
@NotThreadSafe
public class DataTableCompareOld implements DataTable{
	DataTable baseData, compData;
	int[] colMap;
	volatile private Double[] maxCompValues;  //Max deviation for each column.  Each value may be null if unknown
	volatile private Integer[] maxCompRows;	//The row (for each column) where the max comparison value was found.
	

	/**
	 * Construct a new instance using the passed baseData and compData.
	 * The values returned from this instance at any row column are always
	 * baseData - compData.
	 * 
	 * @param baseData
	 * @param compData
	 */
	public DataTableCompareOld( DataTable baseData, DataTable compData) {
		this.baseData = baseData;
		this.compData = compData;
		maxCompValues = new Double[baseData.getColumnCount()];
		maxCompRows = new Integer[baseData.getColumnCount()];
	}

	/**
	 * Construct a new instance using the passed baseData and compData.
	 * The values returned from this instance at any row column are always
	 * baseData - compData.
	 * 
	 * The column mapping specifies how the column in baseData map to compData.
	 * For instance, if column index 4 of baseData should be compared to column
	 * index 9 of compData, index 4 of the columnMapping should contain a 9.
	 * 
	 * The passed columnMapping array size must match the number of columns in
	 * baseData.  There is no such requirement to match columnMapping (ie, the
	 * compData may contain un-needed columns).
	 * 
	 * @param baseData
	 * @param compData
	 * @param columnMapping
	 */
	public DataTableCompareOld( DataTable baseData,  DataTable compData, int[] columnMapping) {
		this.baseData = baseData;
		this.compData = compData;
		colMap = columnMapping;
		maxCompValues = new Double[baseData.getColumnCount()];
		maxCompRows = new Integer[baseData.getColumnCount()];
	}

	/**
	 * Returns the difference between the base and and the comparison
	 * 
	 * @param row
	 * @param col
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public double compare(int row, int col) throws IndexOutOfBoundsException {
		Double base = baseData.getDouble(row, col);
		if (base != null) {
			// just return the difference of two numbers
			return base - compData.getDouble(row, mapColumn(col));
		} else {
			// null returned because underlying column type is not number type.
			String baseString = baseData.getString(row, col);
			if (baseString != null) {
				String compString = compData.getString(row, col);
				return baseString.compareTo(compString);
			} else {
				return Double.NaN;
			}
		}
	}

	public synchronized double findMaxCompareValue(int column) {
		if (maxCompValues[column] == null) {
			
			double max = Double.MIN_VALUE;	//The max value found
			int maxRow = 0;	//row of the max value

			for (int r = 0; r < getRowCount(); r++)  {
				double d = Math.abs( compare(r, column) );
				if (d > max) {
					max = d;
					maxRow = r;
				}
			}

			maxCompValues[column] = max;
			maxCompRows[column] = maxRow;

		}
		return maxCompValues[column];
	}

	public synchronized int findMaxCompareRow(int column) {
		if (maxCompRows[column] == null) {
			findMaxCompareValue(column);
		}
		return maxCompRows[column];
	}

	public synchronized double findMaxCompareValue() {

		double max = Double.MIN_VALUE;

		for (int i = 0; i < maxCompValues.length; i++)  {
			double d = findMaxCompareValue(i);
			if (d > max) max = d;
		}

		return max;
	}

	protected int mapColumn(int c) {
		if (colMap != null) {
			return colMap[c];
		} else {
			return c;
		}
	}
	// =================
	// Delegated Methods
	// =================

	public DataTableWritable copyWritable() {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] findAll(int col, Object value) {
		return FindHelper.bruteForceFindAll(this, col, value);
	}

	public int findFirst(int col, Object value) {
		return FindHelper.bruteForceFindFirst(this, col, value);
	}

	public int findLast(int col, Object value) {
		return FindHelper.bruteForceFindLast(this, col, value);
	}

	public Integer getColumnByName(String name) {
		return baseData.getColumnByName(name);
	}

	public int getColumnCount() {
		return baseData.getColumnCount();
	}

	public Class getDataType(int col) {
		return baseData.getDataType(col);
	}

	public String getDescription() {
		return baseData.getDescription();
	}

	public String getDescription(int col) {
		return baseData.getDescription(col);
	}

	public Double getDouble(int row, int col) {
		return compare(row, col);
	}

	public Float getFloat(int row, int col) {
		return Double.valueOf(compare(row, col)).floatValue();
	}

	public Long getIdForRow(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public Long getLong(int row, int col) {
		return Double.valueOf(compare(row, col)).longValue();
	}
	public Integer getInt(int row, int col) {
		return Double.valueOf(compare(row, col)).intValue();
	}

	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	public Double getMaxDouble(int col) {
		return FindHelper.bruteForceFindMaxDouble(this, col);
	}

	public Integer getMaxInt() {
		return FindHelper.bruteForceFindMaxDouble(this).intValue();
	}

	public Integer getMaxInt(int col) {
		return FindHelper.bruteForceFindMaxDouble(this, col).intValue();
	}

	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	public Double getMinDouble(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col);
	}

	public Integer getMinInt() {
		return FindHelper.bruteForceFindMinDouble(this).intValue();
	}

	public Integer getMinInt(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col).intValue();
	}

	public String getName() {
		return baseData.getName();
	}

	public String getName(int col) {
		return baseData.getName(col);
	}

	public String getProperty(String name) {
		return baseData.getProperty(name);
	}

	public String getProperty(int col, String name) {
		return baseData.getProperty(col, name);
	}

	public Set<String> getPropertyNames() {
		return baseData.getPropertyNames();
	}

	public Set<String> getPropertyNames(int col) {
		return baseData.getPropertyNames(col);
	}

	public int getRowCount() {
		return baseData.getRowCount();
	}

	public int getRowForId(Long id) {
		return baseData.getRowForId(id);
	}

	public String getString(int row, int col) {
		return getDouble(row, col).toString();
	}

	public String getUnits(int col) {
		return baseData.getUnits(col);
	}

	public Object getValue(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasRowIds() {
		return baseData.hasRowIds();
	}

	public boolean isIndexed(int col) {
		return baseData.isIndexed(col);
	}

	public boolean isValid() {
		return baseData.isValid();
	}


	public Immutable toImmutable() {
		int[] colMapCopy = (colMap != null)? colMap.clone():null;
		DataTableCompareOld immutableCore = new DataTableCompareOld(baseData.toImmutable(), compData.toImmutable(), colMapCopy);
		// invalidate self
		this.baseData = null;
		this.compData = null;
		this.colMap = null;
		return new SimpleDataTableImmutable(immutableCore);
	}

}

