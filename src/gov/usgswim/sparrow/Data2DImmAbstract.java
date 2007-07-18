package gov.usgswim.sparrow;


import gov.usgswim.Immutable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


/**
 * An abstract Immutable* implementation of Data2D.
 * Subclasses must ensure that they are also Immutable.
 * 
 * *This class is immutable except for maxValue, which is lazy calculated in
 * a threadsafe fashion.
 */
@Immutable
public abstract class Data2DImmAbstract implements Data2D {
	
	private final String[] _head;
	private final int _rowCount;
	private final int _colCount;
	private final int _indexCol;
	
	//Not immutable, but threadsafe
	private volatile Double maxValue;
	
	public Data2DImmAbstract(int rowCount, int colCount, String[] headings, int indexCol) {
		_rowCount = rowCount;
		_colCount = colCount;
		_head = headings;
		_indexCol = indexCol;
	}
	
	
	/**
	 * The number of rows in the data array.  Null Safe.
	 * @return
	 */
	public int getRowCount() {
		return _rowCount;
	}
	
	/**
	 * The number of columns in the data array.  Null Safe.
	 * @return
	 */
	public int getColCount() {
	  return _colCount;
	}
	
	public synchronized double findMaxValue() {
		if (maxValue == null) {
			if (_rowCount > 0 && _colCount > 0) {
				
				double max = Double.MIN_VALUE;
				
				for (int r = 0; r < _rowCount; r++)  {
					for (int c = 0; c < _colCount; c++)  {
						if (getDouble(r, c) > max) max = getDouble(r, c);
					}
				}
				
				maxValue = new Double(max);
			} else {
				maxValue = Double.MIN_VALUE;
			}
			
		}
		return maxValue.doubleValue();
	}
	
	
	/**
	 * Returns true if there area headings, though it is possible that the headings are all null or empty.
	 * @return
	 */
	public boolean hasHeadings() {
		return (_head != null && _head.length > 0);
	}
	
	
	public String[] getHeadings() {
		return Data2DUtil.copyStrings(_head);
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

	public int getIdColumn() {
		return _indexCol;
	}
	
}
