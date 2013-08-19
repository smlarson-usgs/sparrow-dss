package gov.usgs.cida.datatable.impl;

/**
 * A value coordinate within a DataTableSet, provide a unique table index,
 * row index and column index.
 * 
 * 
 * @author eeverman
 */
public class DataTableSetCoord {
	public int table;
	public int row;
	public int col;
	
	public DataTableSetCoord(int table, int row, int col) {
		this.table = table;
		this.row = row;
		this.col = col;
	}
}
