package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable.Immutable;

/**
 * A Datatable of sparrow prediction results.
 * 
 * SPARROW specific data access methods are provided so that clients do not need
 * to know how the data is structured in the table.
 * 
 * @author eeverman
 */
public interface PredictResult extends Immutable {
	public int getSourceCount();
	
	public int getIncrementalCol();
	public Double getIncremental(int row);
	
	public int getIncrementalColForSrc(int srcId);
	public Double getIncrementalForSrc(int row, int srcId);
	
	public int getTotalCol();
	public Double getTotal(int row);
	
	public int getTotalColForSrc(int srcId);
	public Double getTotalForSrc(int row, int srcId);
	
}
