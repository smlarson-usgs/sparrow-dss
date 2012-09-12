package gov.usgs.cida.datatable.filter;

import gov.usgs.cida.datatable.DataTable;

public class ColumnRangeFilter implements ColumnFilter {
    protected int startCol;
    protected int colCount;
    
    public ColumnRangeFilter(int startCol, int colCount) {
        if (startCol < 0 || colCount <= 0) {
        	throw new IllegalArgumentException(
        		"The startCol must be greater than -1 and the colCount must be greater than zero"
        	);
        	
        }
        assert startCol >= 0;
        assert colCount > 0;
        
        this.startCol = startCol;
        this.colCount = colCount;
    }
    
    public boolean accept(DataTable source, int colNum) {
        return (colNum >= startCol) && (colNum < startCol + colCount);
    }
}
