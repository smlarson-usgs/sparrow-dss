package gov.usgswim.sparrow;

/**
 * A Data2D implementation that derives its from a comparison between two
 * Data2D instances.
 * 
 * Comparisons are always in terms of the baseData - compData.
 */
public class Data2DCompare extends Data2DView {
	Data2D compData;
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
	public Data2DCompare(Data2D baseData, Data2D compData) {
		super(baseData, 0, baseData.getColCount());
		
		this.compData = compData;
		maxCompValues = new Double[baseData.getColCount()];
		maxCompRows = new Integer[baseData.getColCount()];
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
	public Data2DCompare(Data2D baseData, Data2D compData, int[] columnMapping) {
		super(baseData, 0, baseData.getColCount());
		
		this.compData = compData;
		colMap = columnMapping;
		maxCompValues = new Double[baseData.getColCount()];
		maxCompRows = new Integer[baseData.getColCount()];
	}
	
	public double compare(int row, int col) throws IndexOutOfBoundsException {
		return super.getDouble(row, col) - compData.getDouble(row, mapColumn(col));
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
		return maxCompValues[column].doubleValue();
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

}
