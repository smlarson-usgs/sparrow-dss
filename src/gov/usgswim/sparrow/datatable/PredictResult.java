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
    //	public static final String SOURCE_NAME = "sourceName";
	// TODO [IK] add javadocs and enums
	//  AggregateType: none | sum ValueType: incremental | total
	/**
	 * ColumnData property name for tracking the type of prediction result, inc
	 * or inc_total
	 */
	public static final String VALUE_TYPE_PROP = "type";
	
	/**
	 * ColumnData property name for tracking whether a column is a total/sum
	 * column. If value is null or "none", then it is not a column.
	 */
	public static final String AGGREGATE_TYPE_PROP = "aggregate";
	
	/**
	 * ColumnData property name for tracking the type of element that the
	 * column's values represent.
	 */
	public static final String CONSTITUENT_PROP = "constituent";
	
	/**
	 * ColumnData property name for tracking the precision used when reporting
	 * the column's values for display.
	 */
	public static final String PRECISION_PROP = "precision";

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
