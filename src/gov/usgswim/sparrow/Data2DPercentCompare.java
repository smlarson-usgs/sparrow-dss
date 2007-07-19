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
	
	/**
	 * Construct a new instance using the passed baseData and compData.
	 * The values returned from this instance at any row column are always
	 * baseData - compData.
	 * 
	 * @param baseData
	 * @param compData
	 */
	public Data2DPercentCompare(Data2D baseData, Data2D compData, boolean useDecimalPercentage) {
		super(baseData, 0, baseData.getColCount());
		
		this.compData = compData;
		maxCompValues = new Double[baseData.getColCount()];
		decimalPercentage = useDecimalPercentage;
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
	public Data2DPercentCompare(Data2D baseData, Data2D compData, int[] columnMapping, boolean useDecimalPercentage) {
		super(baseData, 0, baseData.getColCount());
		
		this.compData = compData;
		colMap = columnMapping;
		maxCompValues = new Double[baseData.getColCount()];
		decimalPercentage = useDecimalPercentage;
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
	
	//TODO: Max value by column should be part of the interface
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
		return compare(row, col);
	}

	public int getInt(int row, int col) throws IndexOutOfBoundsException {
		return (int) compare(row, col);
	}

	public Number getValue(int row, int col) throws IndexOutOfBoundsException {
		return new Double( compare(row, col) );
	}

}
