package gov.usgswim.sparrow.deprecated;


/**
 * Adds 'set' methods to the Data2D interface
 * @deprecated
 */
public interface Data2DWritable extends Data2D {
		public void setValueAt(Number value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException;
	
	public void setValue(String value, int row, int col)
			throws IndexOutOfBoundsException, IllegalArgumentException;
			
	public void setIndexColumn(int colIndex);
}
