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
	/**
	 * Returns the number of sources.
	 * @return
	 */
	public int getSourceCount();
	
	/**
	 * Returns the index for the total incremental column.
	 * @return
	 */
	public int getIncrementalCol();
	
	/**
	 * Returns the total incremental value for the specified row.
	 * @param row
	 * @return
	 */
	public Double getIncremental(int row);
	
	/**
	 * Returns the column index of the incremental values of the specified source id (the IDENTIFIER of that source).
	 * 
	 * If the prediction data is loaded from a file and does not have an IDENTIFIER,
	 * the ordinal position of the source (1 based) is used as the identifier.
	 * 
	 * @param srcId
	 * @return
	 */
	public int getIncrementalColForSrc(Long srcId);
	
	/**
	 * Returns the incremental value for the specified source id (the IDENTIFIER of that source) and row.
	 * 
	 * If the prediction data is loaded from a file and does not have an IDENTIFIER,
	 * the ordinal position of the source (1 based) is used as the identifier.
	 * 
	 * @param srcId
	 * @return
	 */
	public Double getIncrementalForSrc(int row, Long srcId);
	
	/**
	 * Returns the index of the total Total column.
	 * @return
	 */
	public int getTotalCol();
	
	/**
	 * Returns the total Total value for the specified row.
	 * 
	 * @param row
	 * @return
	 */
	public Double getTotal(int row);
	
	/**
	 * Returns the column index of the total values of the specified source ID (the IDENTIFIER of that source).
	 * 
	 * If the prediction data is loaded from a file and does not have an IDENTIFIER,
	 * the ordinal position of the source (1 based) is used as the identifier.
	 * 
	 * @param srcId
	 * @return
	 */
	public int getTotalColForSrc(Long srcId);
	
	/**
	 * Returns the total value for the specified source id (the IDENTIFIER of that source) and row.
	 * 
	 * If the prediction data is loaded from a file and does not have an IDENTIFIER,
	 * the ordinal position of the source (1 based) is used as the identifier.
	 * 
	 * @param srcId
	 * @return
	 */
	public Double getTotalForSrc(int row, Long srcId);
	
}
