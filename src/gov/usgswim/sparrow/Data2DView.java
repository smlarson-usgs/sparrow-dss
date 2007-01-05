package gov.usgswim.sparrow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

public class Data2DView implements Data2D {
	Data2D data;
	private Double maxValue;  //null unless we know for sure we have the max value
	
	int firstRow; //First included row - zero index
	int rowCount; //Number of rows
	int lastRow;  //First row NOT included - zero index
	
	int firstCol;	//First included column - zero index
	int colCount;	//Number of columns
	int lastCol;	//First colulum NOT included - zero index
	
	public Data2DView(Data2D data, int firstCol, int colCount) {
	
	  this(data, 0, data.getRowCount(), firstCol, colCount);

	}
	
	public Data2DView(Data2D data, int firstRow, int rowCount, int firstCol, int colCount) {
	
		this.data = data;
		
		this.firstRow = firstRow;
		this.rowCount = rowCount;
		this.lastRow = firstRow + rowCount;	//one beyond the index of the last row
		
		this.firstCol = firstCol;
		this.colCount = colCount;
		lastCol = firstCol + colCount;  //one beyond the index of the last column
		
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

	public Object getValueAt(int row, int col) throws IndexOutOfBoundsException {
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

	public void setValueAt(Object value, int row,
												 int col) throws IndexOutOfBoundsException,
																				 IllegalArgumentException {
	  col+=firstCol;
	  row+=firstRow;
	  if (col < lastCol && row < lastRow) {
	    data.setValueAt(value, row, col);
			
	    //Update the max value if one is calculated
	    if (maxValue != null) {
	      if (data.getDouble(row, col) > maxValue.doubleValue()) {
	        maxValue = new Double(data.getDouble(row, col));
	      }
	    }
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
			
			for (int r = firstRow; r < lastRow; r++)  {
				for (int c = firstCol; c < lastCol; c++)  {
					if (data.getDouble(r, c) > max) max = data.getDouble(r, c);
				}
			}
			
			maxValue = new Double(max);
			
		}
		return maxValue.doubleValue();
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
}
