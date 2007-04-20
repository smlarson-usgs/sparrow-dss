package gov.usgswim.sparrow;

/**
 * A Data2D implementation that derives its from a comparison between two
 * Data2D instances.
 * 
 * Comparisons are always in terms of the baseData - compData.
 */
public class Data2DPercentCompare extends Data2DView {
	Data2D compData;
	int[] colMap;
	private Double[] maxCompValues;  //Max deviation for each column.  Each value may be null if unknown
	boolean decimalPercentage = false;
	boolean returnCompValuesAsValues = false;	//if true, calling getValue, getInt, getDouble will return the compare value.
	
	/**
	 * Construct a new instance using the passed baseData and compData.
	 * The values returned from this instance at any row column are always
	 * baseData - compData.
	 * 
	 * @param baseData
	 * @param compData
	 */
	public Data2DPercentCompare(Data2D baseData, Data2D compData, boolean useDecimalPercentage, boolean returnCompValuesAsValues) {
		super(baseData, 0, baseData.getColCount());
		
		this.compData = compData;
		maxCompValues = new Double[baseData.getColCount()];
		decimalPercentage = useDecimalPercentage;
		this.returnCompValuesAsValues = returnCompValuesAsValues;
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
	public Data2DPercentCompare(Data2D baseData, Data2D compData, int[] columnMapping, boolean useDecimalPercentage, boolean returnCompValuesAsValues) {
		super(baseData, 0, baseData.getColCount());
		
		this.compData = compData;
		colMap = columnMapping;
		maxCompValues = new Double[baseData.getColCount()];
		decimalPercentage = useDecimalPercentage;
		this.returnCompValuesAsValues = returnCompValuesAsValues;
	}
	
	public double compare(int row, int col) throws IndexOutOfBoundsException {
		double base = super.getDouble(row, col);
		double comp = compData.getDouble(row, mapColumn(col));
		double dec = 0d;	//default zero percent change
		
		if (base == 0d) {
			if (comp > 0) {
				dec = Double.MAX_VALUE;
			} else if (comp < 0) {
				dec = Double.MIN_VALUE;
			}
		} else {
			dec = (comp - base)/base;
		}
		
		if (decimalPercentage) {
			return dec;
		} else {
			return 100d * dec;
		}
	}
	
	public synchronized double findMaxCompareValue(int column) {
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

	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
		if (returnCompValuesAsValues) {
			return compare(row, col);
		} else {
			return super.getDouble(row, mapColumn(col));
		}
	}

	public int getInt(int row, int col) throws IndexOutOfBoundsException {
		if (returnCompValuesAsValues) {
			return (int) compare(row, col);
		} else {
			return super.getInt(row, mapColumn(col));
		}
	}

	public Object getValueAt(int row, int col) throws IndexOutOfBoundsException {
		
		if (returnCompValuesAsValues) {
			return new Double( compare(row, col) );
		} else {
			return super.getValueAt(row, mapColumn(col));
		}
	}

	public void setValueAt(Object value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException {									 
												 
		if (returnCompValuesAsValues) {
			throw new UnsupportedOperationException("Cannot set values on this view when returning the compared values as real values");
		} else {
			super.setValueAt(value, row, mapColumn(col));
		}
	}
}
