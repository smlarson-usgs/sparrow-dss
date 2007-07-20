package gov.usgswim.sparrow;

import gov.usgswim.NotThreadSafe;

import java.util.HashMap;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.math.NumberUtils;

/**
 * Writable Data2DView that keeps writes in a sparse local hashmap and does
 * not write them back to the underlying data.
 * 
 * This implementation will accept attempts to set a value to null - it will
 * remove the local value entry so that the underlying value is restored.
 */
@NotThreadSafe
public class Data2DViewWriteLocal extends Data2DView implements Data2DWritable {
	HashMap<Integer, Double> sparseData = new HashMap<Integer, Double>(7);


	public Data2DViewWriteLocal(Data2D data) {
	  super(data, 0, data.getRowCount(), 0, data.getColCount());
	}
	
	public Data2DViewWriteLocal(Data2D data, int indexCol) {
	  super(data, 0, data.getRowCount(), 0, data.getColCount(), indexCol);
	}
	
	public Data2DViewWriteLocal(Data2D data, int firstCol, int colCount) {
	  super(data, 0, data.getRowCount(), firstCol, colCount);
	}
	
	public Data2DViewWriteLocal(Data2D data, int firstCol, int colCount, int indexCol) {
	  super(data, 0, data.getRowCount(), firstCol, colCount, indexCol);
	}
	
	public Data2DViewWriteLocal(Data2D data, int firstRow, int rowCount, int firstCol, int colCount) {
		super(data, firstRow, rowCount, firstCol, colCount);
	}
	
	public Data2DViewWriteLocal(Data2D data, int firstRow, int rowCount, int firstCol, int colCount, int indexCol) {
		super(data, firstRow, rowCount, firstCol, colCount, indexCol);
	}

	/**
	 * This returns true b/c double precision data is stored locally for edits.
	 * @return
	 */
	public boolean isDoubleData() { return true; }
	
	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
		Integer hash = getHashID(row, col);
		
		if (sparseData.containsKey(hash)) {
			return sparseData.get(hash);
		} else {
			return super.getDouble(row, col);
		}
	}

	public int getInt(int row, int col) throws IndexOutOfBoundsException {
		Integer hash = getHashID(row, col);
		
		if (sparseData.containsKey(hash)) {
			return sparseData.get(hash).intValue();
		} else {
			return super.getInt(row, col);
		}
	}

	public Number getValue(int row, int col) throws IndexOutOfBoundsException {
		Integer hash = getHashID(row, col);
		
		if (sparseData.containsKey(hash)) {
			return sparseData.get(hash);
		} else {
			return super.getValue(row, col);
		}
	}
	
	public void setValueAt(Number value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException {
				
		if (row >= 0 && col >=0 && row < getRowCount() && col < getColCount()) {
			internalSet(row, col, value);
		} else {
			throw new IndexOutOfBoundsException("The row and/or column (" + row + "," + col + ") are beyond the range of the data array.");
		}
				
	}
	
	public void setValue(String value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException {
				
		if (row >= 0 && col >=0 && row < getRowCount() && col < getColCount()) {

			if (value == null) {
				internalSet(row, col, null);
			} else if (NumberUtils.isNumber(value)) {
				internalSet(row, col, NumberUtils.toDouble(value));
			} else {
				throw new IllegalArgumentException("'" + value + "' is not a valid number.");
			}


		} else {
			throw new IndexOutOfBoundsException("The row and/or column (" + row + "," + col + ") are beyond the range of the data array.");
		}
				
	}

	/**
	 * Returns a hash value for the passed row/column combination.
	 * This value is used to store and retrieve values from the sparse local
	 * hashMap of values, sparseData.
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	protected Integer getHashID(int row, int col) {
		return new HashCodeBuilder().append(row).append(col).toHashCode();
	}
	
	
	protected void internalSet(int r, int c, Number v) {
		
		//Both of these conditions must be true b/c the index is lazy created
		if (indexCol == c && idIndex != null) {
			//there is an index and its on our current column	
			
			synchronized (indexLock) {
				double oldIndexVal = getDouble(r, c);
				
				//remove old index and sparseData entry (if it exists)
				sparseData.remove(getHashID(r, c));
				idIndex.remove(oldIndexVal);
				
				if (v == null) {
					//Put underlying data back into index
					idIndex.put(getDouble(r, c), r);
				} else {
					//write new data to index and sparse data
					sparseData.put(getHashID(r, c), v.doubleValue());
					idIndex.put(v.doubleValue(), r);
				}
			}

		} else {
			if (v == null) {
				sparseData.remove(getHashID(r, c));	//remove old sparseData entry (if it exists)
			} else {
				sparseData.put(getHashID(r, c), v.doubleValue());	//store new data in sparseData
			}
		}
		
		//TODO:  If we remove the max value, it will not detect and find the new maxvalue
		//Update the max value if one is calculated
		if (maxValue != null) {
			if (getDouble(r, c) > maxValue.doubleValue()) {
				maxValue = new Double(getDouble(r, c));
			}
		}
		
	}
	
	public void setIdColumn(int colIndex) {
		synchronized (indexLock) {
			if (indexCol != colIndex) {
				indexCol = colIndex;
				
				if (indexCol != -1) {
					rebuildIndex();
				} else {
					idIndex = null;
				}
			}
		}
	}

}
