package gov.usgswim.sparrow;

/**
 * A 2D data array.
 */
public interface Data2D {
	public String[] getHeadings();

	/**
	 * Returns an appropriate wrapper object for the specified value.
	 * @param row
	 * @param col
	 * @return The value at the specified row/column in an appropriate wrapper type
	 * @throws IndexOutOfBoundsException if the row or column is outside the existing data bounds
	 */
	public Object getValueAt(int row, int col) throws IndexOutOfBoundsException;
	
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
	 * Sets the value at a specified position.
	 * Implementations may do some type conversions of the value to coerce it to
	 * the proper type, but if unable to convert, an IllegalArgumentException is
	 * thrown.
	 * <p>
	 * This method will not add new rows or columns to fit a value.  If the
	 * row or column is outside the existing boundry, an IndexOutOfBoundsException
	 * is thrown.
	 * 
	 * @param value
	 * @param row
	 * @param col
	 * @throws IndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 */
	public void setValueAt(Object value, int row, int col) throws IndexOutOfBoundsException, IllegalArgumentException;
	
	
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
	 * @param trimToEmpty True to ensure that null is never returned.
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
}
