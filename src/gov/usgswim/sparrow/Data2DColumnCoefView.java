package gov.usgswim.sparrow;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

public class Data2DColumnCoefView implements Data2D {
	Data2D data;
	private Double maxValue;  //null unless we know for sure we have the max value
	
	double[] coef;
	
	private Object indexLock = new Object();
	private volatile int indexCol = -1;
	private volatile HashMap<Double, Integer> idIndex;
	
	public Data2DColumnCoefView(Data2D data) {
		this.data = data;
		
		coef = new double[data.getColCount()];
		for (int i = 0; i < coef.length; i++)  {
			coef[i] = 1d;
		}
	}
	
	
	public Data2DColumnCoefView(Data2D data, double[] coef) {
	
	  this.data = data;
		
		if (coef != null) {
			if (data.getColCount() != coef.length)
				throw new IllegalArgumentException(
					"The coef array must be the same length as the number of data columns."
				);
			this.coef = coef;
		} else {
			coef = new double[data.getColCount()];
			
			for (int i = 0; i < coef.length; i++)  {
				coef[i] = 1d;
			}
			
		}
	}
	
	public void setCoef(int col, double coef) throws IndexOutOfBoundsException {
		this.coef[col] = coef;
	}
	
	public double getCoef(int col) throws IndexOutOfBoundsException {
		return coef[col];
	}

	public String[] getHeadings() {
		return data.getHeadings();
	}

	public Object getValueAt(int row, int col) throws IndexOutOfBoundsException {
		Object val = data.getValueAt(row, col);
		return new Double(((Number)val).doubleValue() * coef[col]);
	}

	public int getInt(int row, int col) throws IndexOutOfBoundsException {
	    return (int)(data.getDouble(row, col) * coef[col]);
	}

	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
		return data.getDouble(row, col) * coef[col];
	}

	public void setValueAt(Object value, int row,
												 int col) throws IndexOutOfBoundsException,
																				 IllegalArgumentException {
	  throw new UnsupportedOperationException("Cannot edit data in a this view.");
	}

	public int getRowCount() {
		return data.getRowCount();
	}

	public int getColCount() {
		return data.getColCount();
	}
	
	public synchronized double findMaxValue() {
		if (maxValue == null) {

			double max = Double.MIN_VALUE;
			int rc = data.getRowCount();
			int cc = data.getColCount();
			
			for (int r = 0; r < rc; r++)  {
				for (int c = 0; c < cc; c++)  {
					if (data.getDouble(r, c) > max) max = data.getDouble(r, c);
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
		int rc = data.getRowCount();
		
		for (int r = 0; r < rc; r++)  {
			if (data.getDouble(r, column) == value) return r;
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
		for (int r = data.getRowCount() - 1; r >= 0; r--)  {
			if (data.getDouble(r, column) == value) return r;
		}
		return -1;
	}

	public boolean hasHeadings() {
		return data.hasHeadings();
	}

	public String getHeading(int col) {
	  return data.getHeading(col);
	}

	public String getHeading(int col, boolean trimToEmpty) {
	  return data.getHeading(col, trimToEmpty);
	}
	
	public int findHeading(String name) {
		return data.findHeading(name);
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
	

	public int getIdColumn() {
		return indexCol;
	}
	

	public int findRowById(Double id) {
		
		synchronized (indexLock) {
			if (indexCol != -1) {
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
	
	private void rebuildIndex() {
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
