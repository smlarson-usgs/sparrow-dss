package gov.usgswim.sparrow;

import gov.usgswim.NotThreadSafe;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

@NotThreadSafe
public class Data2DBuilder implements Data2DWritable {
	private double[][] _data;
	private String[] _head;
	private Double maxValue;  //null unless we know for sure we have the max value
	
	private Object indexLock = new Object();
	private volatile int indexCol = -1;
	private volatile HashMap<Double, Integer> indexMap;
	
	//IDs and a map to index them.  Not volitile b/c it is supposed to be unchanging
	private HashMap<Integer, Integer> _idMap;
	private int[] _ids;
	

	/**
	 * Constructor that keeps the passed array as its underlying data.
	 * 
	 * Do not make changes to the passed array after calling this constructor.
	 * @param data
	 * @param headings
	 */
	public Data2DBuilder(double[][] data, String[] headings) {
		_data = data;
		_head = headings;
	}
	
	/**
	 * Constructor that keeps the passed array as its underlying data.
	 * 
	 * Do not make changes to the passed array after calling this constructor.
	 * @param data
	 * @param headings
	 * @param ids an array of IDs with length equals to the number of rows.
	 */
	public Data2DBuilder(double[][] data, String[] headings, int[] ids) {
		_data = data;
		_head = headings;
		_ids = ids;
		
		if (_ids != null) {
			if (_ids.length != getRowCount()) {
				throw new IllegalArgumentException("The length of the ID array must equal the number of rows in the data.");
			}
			
			rebuildIdMap();
		}
	}
	
	/**
	 * Constructor that copies the passed data - changes are not reflected back to
	 * the passed array.
	 * @param data
	 * @param headings
	 */
	public Data2DBuilder(int[][] data, String[] headings) {
		_data = Data2DUtil.copyToDoubleData(data);
		_head = headings;
	}
	
	/**
	 * Constructor that copies the passed data - changes are not reflected back to
	 * the passed array.
	 * @param data
	 * @param headings
	 * @param ids an array of IDs with length equals to the number of rows.
	 */
	public Data2DBuilder(int[][] data, String[] headings, int[] ids) {
		_data = Data2DUtil.copyToDoubleData(data);
		_head = headings;
		_ids = ids;
		
		if (_ids != null) {
			if (_ids.length != getRowCount()) {
				throw new IllegalArgumentException("The length of the ID array must equal the number of rows in the data.");
			}
			
			rebuildIdMap();
		}
	}
	
	/**
	 * Constructor that keeps the passed array as its underlying data.
	 * 
	 * Do not make changes to the passed array after calling this constructor.
	 * @param data
	 */
	public Data2DBuilder(double[][] data) {
		_data = data;
	}
	
	/**
	 * Constructor that copies the passed data - changes are not reflected back to
	 * the passed array.
	 * @param data
	 */
	public Data2DBuilder(int[][] data) {
		_data = Data2DUtil.copyToDoubleData(data);
	}
	
	public boolean isDoubleData() { return true; }
	
	public Data2D getImmutable() {
		return buildDoubleImmutable(getIndexColumn());
	}
	
	public Data2D buildIntImmutable(int indexCol) {
		int[][] newData = Data2DUtil.copyToIntData(_data);
		return new Int2DImm(newData, getHeadings(), indexCol, getRowIds());
	}
	
	public Data2D buildDoubleImmutable(int indexCol) {
		double[][] newData = Data2DUtil.copyToDoubleData(_data);
		return new Double2DImm(newData, getHeadings(), indexCol, getRowIds());
	}
	
	public int[][] getIntData() {
		return Data2DUtil.copyToIntData(_data);
	}
	
	public double[][] getDoubleData() {
		return Data2DUtil.copyToDoubleData(_data);
	}
	
	public Number getValue(int row, int col) throws IndexOutOfBoundsException {
		return new Double(_data[row][col]);
	}
	
	public int getInt(int row, int col) throws IndexOutOfBoundsException {
		return (int) _data[row][col];
	}
	

	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
		return _data[row][col];
	}
	
	public void setValueAt(Number value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException {
				
		if (row >= 0 && row < _data.length && col >=0 && _data[0] != null && col < _data[0].length) {
			if (value != null) {
				internalSet(row, col, ((Number) value).doubleValue());
			} else {
				internalSet(row, col, 0d);
			}
			
		  //Update the max value if one is calculated
		  if (maxValue != null) {
		    if (_data[row][col] > maxValue.doubleValue()) {
		      maxValue = new Double(_data[row][col]);
		    }
		  }
			
		} else {
			throw new IndexOutOfBoundsException("The row and/or column (" + row + "," + col + ") are beyond the range of the data array.");
		}
				
	}
	
	public void setValue(String value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException {
				
		if (row >= 0 && row < _data.length && col >=0 && _data[0] != null && col < _data[0].length) {
			if (value != null) {

				if (NumberUtils.isNumber(value)) {
					internalSet(row, col, NumberUtils.toDouble(value));
				} else {
					throw new IllegalArgumentException("'" + value + "' is not a valid number.");
				}

				
			} else {
				internalSet(row, col, 0d);
			}
			
		  //Update the max value if one is calculated
		  if (maxValue != null) {
		    if (_data[row][col] > maxValue.doubleValue()) {
		      maxValue = new Double(_data[row][col]);
		    }
		  }
			
		} else {
			throw new IndexOutOfBoundsException("The row and/or column (" + row + "," + col + ") are beyond the range of the data array.");
		}
				
	}
	
	private void internalSet(int r, int c, double v) {
		if (this.indexCol == c) {
			//there is an index and its on our current column

			synchronized (indexLock) {
				double oldIndexVal = _data[r][c];
				
				indexMap.remove(oldIndexVal);
				indexMap.put(v, r);
				
				_data[r][c] = v;
			}

		} else {
			_data[r][c] = v;
		}
		
	}
	
	public String[] getHeadings() {
		return Data2DUtil.copyStrings(_head);
	}
	
	/**
	 * The number of rows in the data array.  Null Safe.
	 * @return
	 */
	public int getRowCount() {
		if (_data != null) {
			return _data.length;
		} else {
			return 0;
		}
	}
	
	/**
	 * The number of columns in the data array.  Null Safe.
	 * @return
	 */
	public int getColCount() {
	  if (_data != null && _data[0] != null) {
	    return _data[0].length;
	  } else {
	    return 0;
	  }
	}
	
	public synchronized double findMaxValue() {
		if (maxValue == null) {
			if (_data != null && _data[0] != null) {
				
				double max = Double.MIN_VALUE;
				
				for (int r = 0; r < _data.length; r++)  {
					for (int c = 0; c < _data[0].length; c++)  {
						if (_data[r][c] > max) max = _data[r][c];
					}
				}
				
				maxValue = new Double(max);
			} else {
				return 0d;
			}
			
		}
		return maxValue.doubleValue();
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
	
	/**
	 * Returns true if there area headings, though it is possible that the headings are all null or empty.
	 * @return
	 */
	public boolean hasHeadings() {
		return (_head != null && _head.length > 0);
	}
	
	/**
	 * Gets the heading for the specified column (zero based index).
	 * 
	 * This method never returns null.  If the heading is null, there are no
	 * headings, or the specified column is out of bounds of the headings array,
	 * an empty string is returned.
	 * 
	 * 
	 * @param col The zero based column index
	 * @param trimToEmpty True to ensure that null is never returned.
	 * @return
	 */
	public String getHeading(int col) {
		return getHeading(col, true);
	}
	
	/**
	 * Gets the heading for the specified column (zero based index).
	 * 
	 * If there are no headings or the column does not exist, null is returned if
	 * trimToEmpty is false, otherwise an empty string is returned.
	 * 
	 * 
	 * @param col	The zero based column index
	 * @param trimToEmpty True to ensure that null is never returned.
	 * @return
	 */
	public String getHeading(int col, boolean trimToEmpty) {
		if (_head != null && _head.length > col) {

				if (trimToEmpty) {
				  return StringUtils.trimToEmpty(_head[col]);
				} else {
				  return _head[col];
				}

		} else {
			return trimToEmpty?StringUtils.EMPTY:null;
		}
	}
	
	public int findHeading(String name) {
		if (_head != null && name != null) {
			for(int i=0; i<_head.length; i++) {
				if (name.equalsIgnoreCase(_head[i])) {
				  return i;
				}
			}
		}
		
		return -1;
	}
	
	public void setIndexColumn(int colIndex) {
		synchronized (indexLock) {
			if (indexCol != colIndex) {
				indexCol = colIndex;
				
				if (indexCol != -1) {
					rebuildIndex();
				} else {
					indexMap = null;
				}
			}
		}
	}
	

	public int getIndexColumn() {
		return indexCol;
	}
	

	public int findRowByIndex(Double id) {
		
		synchronized (indexLock) {
			if (indexCol != -1) {
				Integer i = indexMap.get(id);
				if (i != null) {
					return i;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		}

	}
	
	private void rebuildIndex() {
		synchronized (indexLock) {
			HashMap<Double, Integer> map = new HashMap<Double, Integer>(this.getRowCount(), 1.1f);
			int rCount = getRowCount();
			
			for (int i = 0; i < rCount; i++)  {
				map.put(getDouble(i, indexCol), i);
			}
			
			indexMap = map;
		}
	}
	
	private synchronized void rebuildIdMap() {
		int rCount = getRowCount();

		if (_ids != null) {
			
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(rCount, 1.1f);
			
			for (int r = 0; r < rCount; r++)  {
				map.put(_ids[r], r);
			}
			
			_idMap = map;

		}
	}

	public int findRowById(Integer id) {
		if (_idMap != null) {
			Integer r = _idMap.get(id);
			if (r != null) {
				return r;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public Integer getIdForRow(int row) {
		if (_ids != null) {
			return _ids[row];
		} else {
			return null;
		}
	}

	public int[] getRowIds() {
		if (_ids != null) {
			int[] newIds = new int[_ids.length];
			System.arraycopy(_ids, 0, newIds, 0, _ids.length);
			return newIds;
		} else {
			return null;
		}
	}

	public int[] getIntColumn(int col) {
		int rCount = getRowCount();
		int[] newData = new int[rCount];
		
		for (int i=0; i<rCount; i++) {
			newData[i] = (int) _data[i][col];
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
			newData[i] = (int) _data[row][i];
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
