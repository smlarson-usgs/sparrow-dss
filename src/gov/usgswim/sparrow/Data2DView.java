package gov.usgswim.sparrow;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;

public class Data2DView implements Data2D {
	protected final Data2D data;
	protected volatile Double maxValue;  //null unless we know for sure we have the max value
	
	final int firstRow; //First included row - zero index
	final int rowCount; //Number of rows
	final int lastRow;  //First row NOT included - zero index
	
	final int firstCol;	//First included column - zero index
	final int colCount;	//Number of columns
	final int lastCol;	//First column NOT included - zero index
	
	/*
	 * Subclasses may be editable, so these objects need to remain volatile and
	 * protected.
	 */
	protected Object indexLock = new Object();
	protected volatile int indexCol = -1;
	protected volatile HashMap<Double, Integer> idIndex;
	
	public Data2DView(Data2D data, int firstCol, int colCount) {
	  this(data, 0, data.getRowCount(), firstCol, colCount, -1);
	}
	
	public Data2DView(Data2D data, int firstCol, int colCount, int indexCol) {
	  this(data, 0, data.getRowCount(), firstCol, colCount, indexCol);
	}
	
	public Data2DView(Data2D data, int firstRow, int rowCount, int firstCol, int colCount) {
		this(data, firstRow, rowCount, firstCol, colCount, -1);
	}
	
	
	/**
	 * Index column refers to the column index of the columns as they will be
	 * in the new Data2DView.  For instance, if firstCol is specified as 1,
	 * an indexCol of zero would make that column (column 1 in the original data
	 * and column zero in this view) the index column.  In otherwords, it must
	 * be less than colCount.
	 * 
	 * @param data
	 * @param firstRow
	 * @param rowCount
	 * @param firstCol
	 * @param colCount
	 * @param indexCol
	 */
	public Data2DView(Data2D data, int firstRow, int rowCount, int firstCol, int colCount, int indexCol) {
	
		this.data = data;
		
		this.firstRow = firstRow;
		this.rowCount = rowCount;
		this.lastRow = firstRow + rowCount;	//one beyond the index of the last row
		
		this.firstCol = firstCol;
		this.colCount = colCount;
		lastCol = firstCol + colCount;  //one beyond the index of the last column
		
		this.indexCol = indexCol;
		
		if (data == null)
			throw new IllegalArgumentException("The Data2D argument cannot be null");
		
	  if (firstRow < 0 || firstRow >= data.getRowCount())
	    throw new IllegalArgumentException(
	      "The firstRow arg cannot be less then zero or " +
	      "greater then the max row index of the source data"
	    );
		
	  if (rowCount < 1 || lastRow > data.getRowCount())
	    throw new IllegalArgumentException(
	      "The rowCount argument cannot be less then one or " +
	      "exceed the max row index of the source data"
	    );
			
		if (firstCol < 0 || firstCol >= data.getColCount())
			throw new IllegalArgumentException(
				"The firstCol arg cannot be less then zero or " +
				"greater then the max column index of the source data"
			);
			
		if (colCount < 1 || lastCol > data.getColCount())
			throw new IllegalArgumentException(
				"The colCount argument cannot be less then one or " +
				"exceed the max column index of the source data"
			);
			
		if (indexCol < -1 || indexCol >= colCount) {
			throw new IllegalArgumentException(
				"The indexCol argument must be less then the colCount"
			);
		}

	}
	
	public int[][] getIntData() {
		int rc = getRowCount();
		int cc = getColCount();
			
		if (rc > 0 && cc > 0) {

			int[][] out = new int[rc][];
			
			for (int r = 0; r < rc; r++)  {
				int[] row = new int[cc];
				for (int c = 0; c < cc; c++)  {
					row[c] = getInt(r, c);
				}
				
				out[r] = row;
			}
			
			return out;

		} else {
			return Data2D.EMPTY_INT_2D_DATA;
		}
	}
	
	public double[][] getDoubleData() {
		int rc = getRowCount();
		int cc = getColCount();
		
		if (rc > 0 && cc > 0) {
				
			double[][] out = new double[rc][];
			
			for (int r = 0; r < rc; r++)  {
				double[] row = new double[cc];
				for (int c = 0; c < cc; c++) {
					row[c] = getDouble(r, c);
				}
				out[r] = row;
			}
			
			return out;

		} else {
			return Data2D.EMPTY_DOUBLE_2D_DATA;
		}
	}

	public String[] getHeadings() {
		if (data.hasHeadings()) {

			String[] out = new String[colCount];
			for (int i=firstCol, ii=0; i<lastCol; i++, ii++)  {
				out[ii] = data.getHeading(i);
			}
			
			return out;
			
		} else {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		
	}
	
	public int findHeading(String name) {
		if (data.hasHeadings() && name != null) {
			for (int i=firstCol, ii=0; i<lastCol; i++, ii++)  {
				if (name.equalsIgnoreCase(data.getHeading(i))) {
				  return i - firstCol;
				}
			}
		}
		
		return -1;

	}

	public Number getValueAt(int row, int col) throws IndexOutOfBoundsException {
		col+=firstCol;
		row+=firstRow;
		if (col < lastCol && row < lastRow) {
			return data.getValueAt(row, col);
		} else {
			throw new IndexOutOfBoundsException("The row/column (" + (row - firstRow) + ", " + (col - firstCol) + ") exceeds the data bounds");
		}
	}

	public int getInt(int row, int col) throws IndexOutOfBoundsException {
	  col+=firstCol;
	  row+=firstRow;
	  if (col < lastCol && row < lastRow) {
	    return data.getInt(row, col);
	  } else {
	    throw new IndexOutOfBoundsException("The row/column (" + (row - firstRow) + ", " + (col - firstCol) + ") exceeds the data bounds");
	  }
	}

	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
	  col+=firstCol;
	  row+=firstRow;
	  if (col < lastCol && row < lastRow) {
	    return data.getDouble(row, col);
	  } else {
	    throw new IndexOutOfBoundsException("The row/column (" + (row - firstRow) + ", " + (col - firstCol) + ") exceeds the data bounds");
	  }
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColCount() {
		return colCount;
	}
	
	public synchronized double findMaxValue() {
		if (maxValue == null) {

			double max = Double.MIN_VALUE;
			
			for (int r = 0; r < getRowCount(); r++)  {
				for (int c = 0; c < getColCount(); c++)  {
					if (getDouble(r, c) > max) max = getDouble(r, c);
				}
			}
			
			maxValue = new Double(max);
			
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
		for (int r = 0; r < getRowCount(); r++)  {
			if (getDouble(r, column) == value) return r;
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
		for (int r = getRowCount() - 1; r >= 0; r--)  {
			if (getDouble(r, column) == value) return r;
		}
		return -1;
	}

	public boolean hasHeadings() {
		return data.hasHeadings();
	}

	public String getHeading(int col) {
	  col+=firstCol;
	  if (col < lastCol) {
	    return data.getHeading(col);
	  } else {
	    throw new IndexOutOfBoundsException("Column " + col + " exceeds last column, " + (lastCol - 1));
	  }
	}

	public String getHeading(int col, boolean trimToEmpty) {
	  col+=firstCol;
	  if (col < lastCol) {
	    return data.getHeading(col, trimToEmpty);
	  } else {
	    throw new IndexOutOfBoundsException("Column " + col + " exceeds last column, " + (lastCol - 1));
	  }
	}

	public int getIdColumn() {
		return indexCol;
	}
	

	public int findRowById(Double id) {
		
		synchronized (indexLock) {
		
			//lazy build
			if (idIndex == null && indexCol > -1) rebuildIndex();
			
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

	}
	
	protected void rebuildIndex() {
		if (indexCol > -1) {
			synchronized (indexLock) {
				HashMap<Double, Integer> map = new HashMap<Double, Integer>(this.getRowCount(), 1.1f);
				int rCount = getRowCount();
				
				for (int i = 0; i < rCount; i++)  {
					map.put(getDouble(i, indexCol), i);
				}
				
				idIndex = map;
			}
		}
	}
}
