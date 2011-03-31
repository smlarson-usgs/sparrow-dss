package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public abstract class FindHelper {

	// ===========================
	// Find Element Helper Methods
	// ===========================
	
	/**
	 * Finds all instances of the passed value in the column.
	 * 
	 * @param value	Any numeric value, which is converted to a double for comparison.
	 * @param array
	 * @return An array of rows numbers, or an empty array if the value isn't found.
	 */
	public static int[] findAllWithoutIndex(Object value, ColumnData column) {
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else if (value instanceof Number) {
			return findAllWithoutIndex((Number) value, column);
		} else {
			//Doing a non-number comparison
			List<Integer> preResult = new ArrayList<Integer>();
			int arraySize = column.getRowCount();
			for (int i=0; i<arraySize; i++) {
				Object current = column.getValue(i);
				if (value.equals(current)) {
					preResult.add(i);
				}
			}
			return toNonNullIntArray(preResult);
		}
	}
	
	/**
	 * Finds all instances of the passed value in the column.
	 * 
	 * @param value	Any numeric value, which is converted to a double for comparison.
	 * @param array
	 * @return An array of rows numbers, or an empty array if the value isn't found.
	 */
	public static int[] findAllWithoutIndex(Number value, ColumnData column) {
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			List<Integer> preResult = new ArrayList<Integer>();
			int arraySize = column.getRowCount();
			Double dblValue = value.doubleValue();
			for (int i=0; i<arraySize; i++) {
				Double current = column.getDouble(i);
				if (dblValue.equals(current)) {
					preResult.add(i);
				}
			}
			return toNonNullIntArray(preResult);
		}
	}
	
	/**
	 * Finds all instances of the passed value in the double array.
	 * 
	 * @param value	Any numeric value, which is converted to a double for comparison.
	 * @param array
	 * @return An array of rows numbers, or an empty array if the value isn't found.
	 */
	public static int[] findAllWithoutIndex(Number value, double[] array) {
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			List<Integer> preResult = new ArrayList<Integer>();
			int arraySize = Array.getLength(array);
			Double dblValue = value.doubleValue();
			for (int i=0; i<arraySize; i++) {
				double current = array[i];
				if (dblValue.equals(current)) {
					preResult.add(i);
				}
			}
			return toNonNullIntArray(preResult);
		}
	}
	
	/**
	 * Finds all instances of the passed value in the float array.
	 * 
	 * Values are converted to doubles for comparison.
	 * 
	 * @param value	Any numeric value, which is converted to a float for comparison.
	 * @param array
	 * @return An array of rows numbers, or an empty array if the value isn't found.
	 */
	public static int[] findAllWithoutIndex(Number value, float[] array) {
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			List<Integer> preResult = new ArrayList<Integer>();
			int arraySize = Array.getLength(array);
			Double dblValue = value.doubleValue();
			for (int i=0; i<arraySize; i++) {
				double current = array[i];
				if (dblValue.equals(current)) {
					preResult.add(i);
				}
			}
			return toNonNullIntArray(preResult);
		}
	}
	
	/**
	 * Finds all instances of the passed value in the long array.
	 * 
	 * @param value	Any numeric value, which is converted to a double for comparison.
	 * @param array
	 * @return An array of rows numbers, or an empty array if the value isn't found.
	 */
	public static int[] findAllWithoutIndex(Number value, long[] array) {
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			List<Integer> preResult = new ArrayList<Integer>();
			int arraySize = Array.getLength(array);
			Double dblValue = value.doubleValue();
			for (int i=0; i<arraySize; i++) {
				double current = array[i];
				if (dblValue.equals(current)) {
					preResult.add(i);
				}
			}
			return toNonNullIntArray(preResult);
		}
	}
	
	/**
	 * Finds all instances of the passed value in the int array.
	 * 
	 * @param value	Any numeric value, which is converted to a double for comparison.
	 * @param array
	 * @return An array of rows numbers, or an empty array if the value isn't found.
	 */
	public static int[] findAllWithoutIndex(Number value, int[] array) {
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			List<Integer> preResult = new ArrayList<Integer>();
			int arraySize = Array.getLength(array);
			Double dblValue = value.doubleValue();
			for (int i=0; i<arraySize; i++) {
				double current = array[i];
				if (dblValue.equals(current)) {
					preResult.add(i);
				}
			}
			return toNonNullIntArray(preResult);
		}
	}
	
	// =====================================
	// Brute Force Find in DataTable Methods
	// =====================================
	public static int bruteForceFindFirst(DataTable dt, int col, Object value) {
		
		if (value == null) {
			return -1;
		} else if (value instanceof Number) {
			return bruteForceFindFirst(dt, col, (Number) value);
		} else {

			int numRows = dt.getRowCount();
			
			//Searching on non-numeric data
			for (int row=0; row<numRows; row++) {
				if (dt.getValue(row, col).equals(value)) {
					return row;
				}
			}
			
			return -1;  //not found
		}
	}
	
	public static int bruteForceFindFirst(DataTable dt, int col, Number value) {
		
		if (value == null) {
			return -1;
		} else {

			int numRows = dt.getRowCount();
			
			if (Number.class.isAssignableFrom(dt.getDataType(col))) {
				//the data in this column is a number type
				
				//Handle all comparisons at Double precision
				double doubleVal = value.doubleValue();
				for (int row=0; row<numRows; row++) {
					if (dt.getDouble(row, col).equals(doubleVal)) {
						return row;
					}
				}
				
			} else {
				//Searching on non-numeric data
				for (int row=0; row<numRows; row++) {
					if (dt.getValue(row, col).equals(value)) {
						return row;
					}
				}
			}
			
			return -1;  //not found
		}
		
	}
	
	public static int bruteForceFindFirst(ColumnData column, Object value) {
		
		if (value == null) {
			return -1;
		} else if (value instanceof Number) {
			return bruteForceFindFirst(column, (Number) value);
		} else {

			int numRows = column.getRowCount();
			
			//Searching on non-numeric data
			for (int row=0; row<numRows; row++) {
				if (value.equals(column.getValue(row))) {
					return row;
				}
			}
			
			return -1;  //not found
		}
	}
	
	public static int bruteForceFindFirst(ColumnData column, Number value) {
		
		if (value == null) {
			return -1;
		} else {

			int numRows = column.getRowCount();
			
			if (Number.class.isAssignableFrom(column.getDataType())) {
				//the data in this column is a number type
				
				//Handle all comparisons at Double precision
				Double doubleVal = value.doubleValue();
				for (int row=0; row<numRows; row++) {
					if (doubleVal.equals(column.getDouble(row))) {
						return row;
					}
				}
				
			} else {
				//Searching on non-numeric data
				for (int row=0; row<numRows; row++) {
					if (value.equals(column.getValue(row))) {
						return row;
					}
				}
			}
			
			return -1;  //not found
		}
	}
	
	public static int bruteForceFindLast(DataTable dt, int col, Object value) {
		if (value == null) {
			return -1;
		} else if (value instanceof Number) {
			return bruteForceFindLast(dt, col, (Number) value);
		} else {

			int numRows = dt.getRowCount();
			
			//Searching on non-numeric data
			for (int row = numRows - 1; row > -1; row--) {
				if (dt.getValue(row, col).equals(value)) {
					return row;
				}
			}
			return -1;  //not found
		}
	}
	
	public static int bruteForceFindLast(DataTable dt, int col, Number value) {
		
		if (value == null) {
			return -1;
		} else {

			int numRows = dt.getRowCount();
			
			if (Number.class.isAssignableFrom(dt.getDataType(col))) {
				//the data in this column is a number type
				
				//Handle all comparisons at Double precision
				double doubleVal = value.doubleValue();
				for (int row = numRows - 1; row > -1; row--) {
					if (dt.getDouble(row, col).equals(doubleVal)) {
						return row;
					}
				}
				
			} else {
				//Searching on non-numeric data
				for (int row = numRows - 1; row > -1; row--) {
					if (dt.getValue(row, col).equals(value)) {
						return row;
					}
				}
			}
			
			return -1;  //not found
		}
		
	}
	
	public static int bruteForceFindLast(ColumnData column, Object value) {
		
		if (value == null) {
			return -1;
		} else if (value instanceof Number) {
			return bruteForceFindLast(column, (Number) value);
		} else {

			int numRows = column.getRowCount();
			
			//Searching on non-numeric data
			for (int row = numRows - 1; row > -1; row--) {
				if (value.equals(column.getValue(row))) {
					return row;
				}
			}
			
			return -1;  //not found
		}
	}
	
	public static int bruteForceFindLast(ColumnData column, Number value) {
		
		if (value == null) {
			return -1;
		} else {

			int numRows = column.getRowCount();
			
			if (Number.class.isAssignableFrom(column.getDataType())) {
				//the data in this column is a number type
				
				//Handle all comparisons at Double precision
				Double doubleVal = value.doubleValue();
				for (int row = numRows - 1; row > -1; row--) {
					if (doubleVal.equals(column.getDouble(row))) {
						return row;
					}
				}
				
			} else {
				//Searching on non-numeric data
				for (int row = numRows - 1; row > -1; row--) {
					if (value.equals(column.getValue(row))) {
						return row;
					}
				}
			}
			
			return -1;  //not found
		}
	}


	/**
	 * Finds all, returning an empty array if not found.
	 * @param dt
	 * @param col
	 * @param value
	 * @return
	 */
	public static int[] bruteForceFindAll(DataTable dt, int col, Object value) {
		
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else if (value instanceof Number) {
			return bruteForceFindAll(dt, col, (Number) value);
		} else {
		
			List<Integer> results = new ArrayList<Integer>();
			int numRows = dt.getRowCount();
			
			//Searching on non-numeric data
			for (int row=0; row<numRows; row++) {
				if (dt.getValue(row, col).equals(value)) results.add(row);
			}
			
			return toNonNullIntArray(results);
		}
	}
	
	/**
	 * Finds all, returning an empty array if not found.
	 * @param dt
	 * @param col
	 * @param value
	 * @return
	 */
	public static int[] bruteForceFindAll(DataTable dt, int col, Number value) {
		
		
		if (value == null) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			
			List<Integer> results = new ArrayList<Integer>();
			int numRows = dt.getRowCount();
			
			if (Number.class.isAssignableFrom(dt.getDataType(col))) {
				//the data in this column is a number type
				
				//Handle all comparisons at Double precision
				Double doubleVal = value.doubleValue();
				for (int row=0; row<numRows; row++) {
					if (doubleVal.equals(dt.getDouble(row, col))) results.add(row);
				}
				
			} else {
				//Searching on non-numeric data
				for (int row=0; row<numRows; row++) {
					if (value.equals(dt.getValue(row, col))) results.add(row);
				}
			}
			
			return toNonNullIntArray(results);
		}
		
		
	}

	// ============================
	// Find MAX-MIN Helper Methods
	// ============================
	
	public static Double findMaxDouble(ColumnData column) {
		
		if (Number.class.isAssignableFrom(column.getDataType())) {
			//Working with numeric data
			Double result = null;
			int arraySize = column.getRowCount();
			if  (arraySize > 0) {
				double max = column.getDouble(0);
				for (int i=1; i<arraySize; i++) {
					Double current = column.getDouble(i);
					
					if (current != null && ! Double.isNaN(current)) {
						max = Math.max(max, current);
					}
				}
				result = max;
			}
			return result;
		} else {
			return null;
		}
	}
	
	public static Double findMinDouble(ColumnData column) {
		
		if (Number.class.isAssignableFrom(column.getDataType())) {
			//Working with numeric data
			Double result = null;
			int arraySize = column.getRowCount();
			if  (arraySize > 0) {
				double min = column.getDouble(0);
				for (int i=1; i<arraySize; i++) {
					Double current = column.getDouble(i);
					
					if (current != null && ! Double.isNaN(current)) {
						min = Math.min(min, current);
					}
				}
				result = min;
			}
			return result;
		} else {
			return null;
		}
	}
	

	// =============================================
	// Brute Force Find Min-Max in DataTable Methods
	// =============================================
	
	public static Double bruteForceFindMaxDouble(DataTable dt, int col) {
		Double max = Double.MIN_VALUE;
		int rows = dt.getRowCount();
		for (int row=0; row < rows; row++) {
			Double value = dt.getDouble(row, col);
			if (value != null) {
				max = (max == null)? value: Math.max(max, value);
			}
		}
		return max;
	}
	
	public static Double bruteForceFindMaxDouble(DataTable dt) {
		Double max = null;
		int cols = dt.getColumnCount();
		for (int col=0; col < cols; col++) {
			Double value = dt.getMaxDouble(col);
			if (value != null) {
				max = (max == null)? value: Math.max(max, value);
			}
		}
		return max;
	}

	public static Double bruteForceFindMinDouble(DataTable dt, int col) {
		Double min = null;
		int rows = dt.getRowCount();
		for (int row=0; row < rows; row++) {
			Double value = dt.getDouble(row, col);
			if (value != null) {
				min = (min == null)? value: Math.min(min, value);
			}
		}
		return min;
	}

	public static Double bruteForceFindMinDouble(DataTable dt) {
		Double min = null;
		int cols = dt.getColumnCount();
		for (int col=0; col < cols; col++) {
			Double value = dt.getMinDouble(col);
			if (value != null) {
				min = (min == null)? value: Math.min(min, value);
			}
		}
		return min;
	}
	
	/**
	 * Creates a primative array of ints from an Integer List.
	 *
	 * @param source
	 * @return A list that is empty of the source is empty.
	 */
	public static int[] toNonNullIntArray(List<Integer> source) {
		if (source.size() == 0) {
			return ArrayUtils.EMPTY_INT_ARRAY;
		} else {
			int[] result = new int[source.size()];
			for (int i=0; i<source.size(); i++) {
				result[i] = source.get(i).intValue();
			}
			return result;
		}
		
	}
	
}
