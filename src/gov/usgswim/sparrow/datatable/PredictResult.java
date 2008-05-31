package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable.Immutable;

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
