package gov.usgswim.sparrow;

/**
 * Adds 'set' methods to the Data2D interface
 */
public interface Data2DWritable extends Data2D {
		public void setValueAt(Number value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException;
	
	public void setValue(String value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException;
			
	public void setIdColumn(int colIndex);
}
