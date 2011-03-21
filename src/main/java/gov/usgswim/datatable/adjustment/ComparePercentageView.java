package gov.usgswim.datatable.adjustment;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.FindHelper;
import gov.usgswim.datatable.impl.DataTableImmutableWrapper;

import java.util.Map;
import java.util.Set;


/**
 * A DataTable implementation that derives its from a comparison between two
 * DataTable instances.
 * 
 * The ID values (if any) of the first DataTable (the baseData) is the one used
 * for ID lookups.
 *
 * Comparisons are always in terms of the baseData - compData.
 * @NotThreadSafe
 */
public class ComparePercentageView implements DataTable{

	private static final long serialVersionUID = 1L;
	
	protected DataTable baseData;
	protected DataTable compData;
	protected int[] colMap;
	protected Double[] maxCompValues;
	protected boolean useDecimal;


	//============
	// CONSTRUCTOR
	// ===========
	/**
	 * Construct a new instance using the passed baseData and compData.
	 * The values returned from this instance at any row column are always
	 * baseData - compData.
	 * 
	 * @param baseData
	 * @param compData
	 */
	public ComparePercentageView(DataTable base, DataTable comp, boolean useDecimalPercentage) {
		this(base, comp, null, useDecimalPercentage);
	}
	
	/**
	 * @param base
	 * @param comp
	 * @param columnMapping -- mapping applied to column lookups on the comp table
	 * @param useDecimalPercentage
	 */
	public ComparePercentageView(DataTable base, DataTable comp, int[] columnMapping, boolean useDecimalPercentage) {
		this.baseData = base;
		this.compData = comp;
		this.colMap = (columnMapping != null && columnMapping.length > 0)? columnMapping: null;
		maxCompValues = new Double[baseData.getColumnCount()];
		this.useDecimal = useDecimalPercentage;
	}
	// =================
	// Lifecycle Methods
	// =================

	public boolean isValid() {
		return baseData != null && compData != null;
	}
	
	@Override
	public boolean isValid(int columnIndex) {
		return baseData.isValid(columnIndex) && compData.isValid(columnIndex);
	}

	public DataTable.Immutable toImmutable() {
		int[] colMapCopy = (colMap != null)? colMap.clone():null;
		ComparePercentageView immutableCore = new ComparePercentageView(baseData.toImmutable(), compData.toImmutable(), colMapCopy, useDecimal);
		// invalidate self
		baseData = null;
		compData = null;
		colMap = null;
		
		return new DataTableImmutableWrapper(immutableCore);
	}
	
	public boolean hasRowIds() {
		return baseData.hasRowIds();
	}

	public boolean isIndexed(int col) {
		// DOESN'T really make sense for this kind of view
		throw new UnsupportedOperationException("Not currently supported. Should decide whether it ought to be supported.");
	}


	// ===============
	// Utility Methods
	// ===============
	public double compare(int row, int col) throws IndexOutOfBoundsException {
		double base = baseData.getDouble(row, col);
		double comp = compData.getDouble(row, map(col));
		double dec = 0d;	//default zero percent change
		
		if (base == 0d) {
			if (comp > 0) {
				return Double.MAX_VALUE;
				// must return here, otherwise multiplying by 100 gives error
			} else if (comp < 0) {
				return  Double.MIN_VALUE;
				// must return here, otherwise multiplying by 100 gives error
			}
		} else {
			dec = (comp - base)/base;
		}
		
		return (useDecimal)? dec: 100d * dec;
	}
	
	protected int map(int c) {
		return (colMap == null)? c: colMap[c];
	}
	
	public double findMaxCompareValue(int column) {
		if (maxCompValues[column] == null) {

			double max = Double.MIN_VALUE;
			
			for (int r = 0; r < getRowCount(); r++)  {
				double d = Math.abs( compare(r, column) );
				if (d > max) max = d;
			}
			
			maxCompValues[column] = new Double(max);
			
		}
		return maxCompValues[column].doubleValue();
	}
	
	public double findMaxCompareValue() {

		double max = Double.MIN_VALUE;
		
		for (int i = 0; i < maxCompValues.length; i++)  {
			double d = findMaxCompareValue(i);
			if (d > max) max = d;
		}

		return max;
	}
	
	// ====================
	// INDEX & FIND Methods (may be inaccurate if rows are filtered
	// ====================
	public int[] findAll(int col, Object value) {
		return FindHelper.bruteForceFindAll(this, col, value);	
	}

	public int findFirst(int col, Object value) {
		return FindHelper.bruteForceFindFirst(this, col, value);
	}

	public int findLast(int col, Object value) {
		return FindHelper.bruteForceFindLast(this, col, value);
	}

	// ========================
	// Adjusted Max-Min Methods
	// ========================
	// TODO [IK] change the implementation of these later
	public Double getMaxDouble(int col) {
		return FindHelper.bruteForceFindMaxDouble(this, col);
	}

	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	public Integer getMaxInt(int col) {
		return getMaxDouble(col).intValue();
	}

	public Integer getMaxInt() {
		return getMaxDouble().intValue();
	}

	public Double getMinDouble(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col);
	}

	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	public Integer getMinInt(int col) {
		return getMinDouble(col).intValue();
	}

	public Integer getMinInt() {
		return getMinDouble().intValue();
	}


	// =================
	// DataTable Methods
	// =================
	/* Unsupported for this class */
	@Override
	public ColumnData getColumn(int colIndex) {
		throw new UnsupportedOperationException(
				"This method is not supported for this type of DataTable view, " +
				"since there is no real ColumnData instance containing the data. ");
	}
	
	public Integer getColumnByName(String name) {
		return baseData.getColumnByName(name);
	}

	public int getColumnCount() {
		return baseData.getColumnCount();
	}

	public Class<?> getDataType(int col) {
		return baseData.getDataType(col);
	}

	public String getDescription() {
		return baseData.getDescription();
	}

	public String getDescription(int col) {
		return baseData.getDescription(col);
	}

	public Long getIdForRow(int row) {
		return baseData.getIdForRow(row);
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
	
	@Override
	public Map<String, String> getProperties() {
		return baseData.getProperties();
	}
	@Override
	public Map<String, String> getProperties(int col) {
		return baseData.getProperties(col);
	}

	public int getRowCount() {
		return baseData.getRowCount();
	}

	public int getRowForId(Long id) {
		return baseData.getRowForId(id);
	}

	public String getUnits(int col) {
		return baseData.getUnits(col);
	}

	// =================
	// Adjusted getXXX()
	// =================
	public Double getDouble(int row, int col) {
		return compare(row, col);
	}

	public Float getFloat(int row, int col) {
		return Double.valueOf(compare(row, col)).floatValue();
	}

	public Integer getInt(int row, int col) {
		return Double.valueOf(compare(row, col)).intValue();
	}
	
	public Long getLong(int row, int col) {
		return Double.valueOf(compare(row, col)).longValue();
	}
	
	public String getString(int row, int col) {
		// Does it make sense to return comp for string column? I guess
		return Double.valueOf(compare(row, col)).toString();
	}

	public Object getValue(int row, int col) {
		if (Number.class.isAssignableFrom(getDataType(col))) {
			return 	Double.valueOf(compare(row, col));
		}
		// Does it make sense to return String? I guess
		return getString(row, col);
	}


}
