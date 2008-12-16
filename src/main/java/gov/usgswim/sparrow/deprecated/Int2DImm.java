package gov.usgswim.sparrow.deprecated;

import gov.usgswim.Immutable;

import java.util.HashMap;

/**
 * A loose wrapper around a 2D double array, which includes optional column headings.
 * @deprecated
 */
@Immutable
public class Int2DImm extends Data2DImmAbstract {
	private final int[][] _data;
	
	private volatile HashMap<Double, Integer> idIndex;
	
	public Int2DImm(int[][] data) {
		this(data, null, -1, null);
	}
	
	public Int2DImm(int[][] data, String[] headings) {
		this(data, headings, -1, null);
	}
	
	public Int2DImm(int[][] data, String[] headings, Integer indexCol, int[] ids) {
		super((data != null)?data.length:0, (data != null && data.length > 0 && data[0] != null)?data[0].length:0, headings, indexCol, ids);
		_data = data;
		
		idIndex = buildIndex();
	}
	
	public boolean isDoubleData() { return false; }
	
	public Data2D toImmutable() {
		return buildIntImmutable(getIndexColumn());
	}
	
	public Data2D buildIntImmutable(int indexCol) {
		if (getIndexColumn() == indexCol) {
			return this;
		} else {
			return new Int2DImm(_data, getHeadings(), indexCol, getRowIds());
		}
	}
	
	public Data2D buildDoubleImmutable(int indexCol) {
		return buildIntImmutable(indexCol);
	}
	
	public int[][] getIntData() {
		return Data2DUtil.copyToIntData(_data);
	}
	
	public double[][] getDoubleData() {
		return Data2DUtil.copyToDoubleData(_data);
	}
	
	public Number getValue(int row, int col) throws IndexOutOfBoundsException {
		return new Integer(_data[row][col]);
	}
	
	public int getInt(int row, int col) throws IndexOutOfBoundsException {
		return _data[row][col];
	}
	

	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
		return _data[row][col];
	}
	
	/**
	 * A very simple search implementation
	 * @param value
	 * @param column
	 * @return
	 */
	public int orderedSearchFirst(double value, int column) {
		for (int r = 0; r < _data.length; r++)  {
			if (_data[r][column] == value) return r;
		}
		return -1;
	}
	
	/**
	 * A very simple search implementation
	 * @param value
	 * @param column
	 * @return
	 */
	public int orderedSearchLast(double value, int column) {
		for (int r = _data.length - 1; r >= 0; r--)  {
			if (_data[r][column] == value) return r;
		}
		return -1;
	}
	

	public int findRowByIndex(Double id) {
	
		if (idIndex != null) {
			Integer i = idIndex.get(id);
			if (i != null) {
				return i;
			} else {
				return -1;
			}
		} else {
			return -1;
		}

	}
	
	private HashMap<Double, Integer> buildIndex() {
		int rCount = getRowCount();
		int idCol = getIndexColumn();
		
		if (idCol > -1) {
			HashMap<Double, Integer> map = new HashMap<Double, Integer>(rCount, 1.1f);
			
			for (int r = 0; r < rCount; r++)  {
				map.put(getDouble(r, idCol), r);
			}
			
			return map;
		} else {
			return null;
		}
	}
	
	public int[] getIntColumn(int col) {
		int rCount = getRowCount();
		int[] newData = new int[rCount];
		
		for (int i=0; i<rCount; i++) {
			newData[i] = _data[i][col];
		}
		return newData;
	}

	public double[] getDoubleColumn(int col) {
		int rCount = getRowCount();
		double[] newData = new double[rCount];
		for (int i=0; i<rCount; i++) {
			newData[i] = _data[i][col];
		}
		return newData;
	}

	public int[] getIntRow(int row) {
		int cCount = getColCount();
		int[] newData = new int[cCount];
		
		for (int i=0; i<cCount; i++) {
			newData[i] = _data[row][i];
		}
		return newData;
	}

	public double[] getDoubleRow(int row) {
		int cCount = getColCount();
		double[] newData = new double[cCount];
		
		for (int i=0; i<cCount; i++) {
			newData[i] = _data[row][i];
		}
		return newData;
	}
}
