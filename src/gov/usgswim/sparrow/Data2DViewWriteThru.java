package gov.usgswim.sparrow;

import gov.usgswim.NotThreadSafe;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * A writable Data2D view that allows edits to be made thru to the underlying Data2d instance.
 * 
 * 
 * 
 * Not Threadsafe.  Synchronization in this class is only used to prevent
 * the index column from changing during index updates and assignments.
 */
@NotThreadSafe
public class Data2DViewWriteThru extends Data2DView implements Data2DWritable {
	Data2DWritable data;

	public Data2DViewWriteThru(Data2DWritable data, int firstCol, int colCount) {
	  super(data, 0, data.getRowCount(), firstCol, colCount);
		this.data = data;
	}
	
	public Data2DViewWriteThru(Data2DWritable data, int firstCol, int colCount, int indexCol) {
	  super(data, 0, data.getRowCount(), firstCol, colCount, indexCol);
		this.data = data;
	}
	
	public Data2DViewWriteThru(Data2DWritable data, int firstRow, int rowCount, int firstCol, int colCount) {
		super(data, firstRow, rowCount, firstCol, colCount);
		this.data = data;
	}
	
	public Data2DViewWriteThru(Data2DWritable data, int firstRow, int rowCount, int firstCol, int colCount, int indexCol) {
		super(data, firstRow, rowCount, firstCol, colCount, indexCol);
		this.data = data;
	}

	public void setValueAt(Number value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException {
				
		if (row >= 0 && col >=0 && row < getRowCount() && col < getColCount()) {
			if (value != null) {
				internalSet(row, col, value);
			} else {
				internalSet(row, col, 0d);
			}
			
		} else {
			throw new IndexOutOfBoundsException("The row and/or column (" + row + "," + col + ") are beyond the range of the data array.");
		}
				
	}
	
	public void setValue(String value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException {
				
		if (row >= 0 && col >=0 && row < getRowCount() && col < getColCount()) {
			if (value != null) {

				if (NumberUtils.isNumber(value)) {
					internalSet(row, col, NumberUtils.toDouble(value));
				} else {
					throw new IllegalArgumentException("'" + value + "' is not a valid number.");
				}

			} else {
				internalSet(row, col, 0d);
			}

		} else {
			throw new IndexOutOfBoundsException("The row and/or column (" + row + "," + col + ") are beyond the range of the data array.");
		}
				
	}

	
	protected void internalSet(int r, int c, Number v) {
	
		//Both of these conditions must be true b/c the index is lazy created
		if (indexCol == c && idIndex != null) {
			//there is an index and its on our current column

			synchronized (indexLock) {
				double oldIndexVal = getDouble(r, c);
				
				idIndex.remove(oldIndexVal);
				idIndex.put(v.doubleValue(), r);

				data.setValueAt(v, r + firstRow, c + firstCol);
			}

		} else {
			data.setValueAt(v, r + firstRow, c + firstCol);
		}
		
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
