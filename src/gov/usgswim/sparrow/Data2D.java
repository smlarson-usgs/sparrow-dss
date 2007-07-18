package gov.usgswim.sparrow;

/**
 * A 2D data array.
 */
public interface Data2D {
	public static final int[][] EMPTY_INT_2D_DATA = new int[0][0];
	public static final double[][] EMPTY_DOUBLE_2D_DATA = new double[0][0];
	
	public String[] getHeadings();

	/**
	 * Returns an appropriate wrapper object for the specified value.
	 * @param row
	 * @param col
	 * @return The value at the specified row/column in an appropriate wrapper type
	 * @throws IndexOutOfBoundsException if the row or column is outside the existing data bounds
	 */
	public Number getValueAt(int row, int col) throws IndexOutOfBoundsException;
	
	/**
	 * Returns an integer representation of the value at the specified row/column location.
	 * Double values are trimmed.
	 * 
	 * @param row
	 * @param col
	 * @return The value at the specified row/column, as an integer
	 * @throws IndexOutOfBoundsException if the row or column is outside the existing data bounds
	 */
	public int getInt(int row, int col) throws IndexOutOfBoundsException;
	
	/**
	 * Returns a double representation of the value at the specified row/column location.
	 * 
	 * @param row
	 * @param col
	 * @return The value at the specified row/column, as a double
	 * @throws IndexOutOfBoundsException if the row or column is outside the existing data bounds
	 */
	public double getDouble(int row, int col) throws IndexOutOfBoundsException;
	
	/**
	 * Returns a detached int[][] array of the data contained in the Data2D.
	 * 
	 * Changes made to the returned array are not written back to the Data2D
	 * instance.
	 * 
	 * @return
	 */
	public int[][] getIntData();
	
	/**
	 * Returns a detached double[][] array of the data contained in the Data2D.
	 * 
	 * Changes made to the returned array are not written back to the Data2D
	 * instance.
	 * 
	 * @return
	 */
	public double[][] getDoubleData();
	
	/**
	 * The number of rows in the data array.  Null Safe.
	 * @return
	 */
	public int getRowCount();

	/**
	 * The number of columns in the data array.  Null Safe.
	 * @return
	 */
	public int getColCount();
	
	/**
	 * Returns the largest value contained anywhere in the data
	 * @return
	 */
	public double findMaxValue();
	
	/**
	 * Finds the first instance of value in the specified column.
	 * 
	 * The search assumes that the data is ordered - that is, that values always
	 * increase.  If the value is not found, -1 is returned.
	 * 
	 * @param value The value searched for
	 * @param column the column to search
	 * @return The first row containing the specified value in the spec'ed column
	 * or -1 if the value cannot be found.
	 */
	public int orderedSearchFirst(double value, int column);
	
	/**
	 * Finds the last instance of value in the specified column.
	 * 
	 * The search assumes that the data is ordered - that is, that values always
	 * increase.  If the value is not found, -1 is returned.
	 * 
	 * @param value The value searched for
	 * @param column the column to search
	 * @return The last row containing the specified value in the spec'ed column
	 * or -1 if the value cannot be found.
	 */
	public int orderedSearchLast(double value, int column);

	/**
	 * Returns true if there area headings, though it is possible that the headings are all null or empty.
	 * @return
	 */
	public boolean hasHeadings();

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
	public String getHeading(int col);

	/**
	 * Gets the heading for the specified column (zero based index).
	 *
	 * If there are no headings or the column does not exist, null is returned if
	 * trimToEmpty is false, otherwise an empty string is returned.
	 *
	 *
	 * @param col The zero based column index
	 * @param trimToEmpty True to ensure that null is never returned.
	 * @return
	 */
	public String getHeading(int col, boolean trimToEmpty);
	
	/**
	 * Returns the index of the specified heading.
	 * 
	 * The comparison is not case-sensitive.  -1 is returned if the heading is not
	 * found.  Null names cannot be matched.
	 * 
	 * @param name The heading to look for (case insensitive)
	 * @return The index of the heading, or -1 if it is not found.
	 */
	public int findHeading(String name);
	
	
	/**
	 * Returns the index of the ID column.  -1 is returned if there is no index.
	 * @return
	 */
	public int getIdColumn();
	
	/**
	 * Returns the zero based row index of the row with the specified id.
	 * 
	 * If no row is found, -1 is returned.
	 * 
	 * @param id
	 * @return
	 */
	public int findRowById(Double id);
	
}
