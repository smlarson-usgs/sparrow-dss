package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable.Immutable;

/**
 * A Datatable of sparrow prediction results with convenience getXXX methods
 * appropriate to the content
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
	 * @return A count.
	 */
	public int getSourceCount();
	
	/**
	 * Returns the column index for the incremental column.
	 * @return A column index.
	 */
	public int getIncrementalCol();
	
	/**
	 * Returns the incremental addition of the model constituent for all sources
	 * at this reach, as identified by the specified row.
	 * 
	 * Note that the incremental addition is not decayed by the instream decay
	 * coef.:  It is the amount of load enter this reach, not the incremental
	 * amount leaving the reach.
	 * 
	 * @param row The row number of the reach
	 * @return The incremental addition from all sources at this reach
	 */
	public Double getIncremental(int row);
	
	/**
	 * Returns the column index of the incremental values of the specified source.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return
	 */
	public int getIncrementalColForSrc(Long srcId);
	
	/**
	 * Returns the incremental addition of the model constituent for the
	 * specified source at this reach, as specified by the source id.
	 * at this reach, as identified by the specified row.
	 * 
	 * Note that the incremental addition is not decayed by the instream decay
	 * coef.:  It is the amount of load enter this reach, not the incremental
	 * amount leaving the reach.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return The incremental addition from the specified source at this reach
	 */
	public Double getIncrementalForSrc(int row, Long srcId);
	
	/**
	 * Returns the index of the total load column.
	 * @return A column index value.
	 */
	public int getTotalCol();
	
	/**
	 * Returns the Total load (including upstream load) for all sources arriving
	 * at the bottom of this reach.
	 * 
	 * This is also called the 'measurable' load, since this is the load that
	 * you could theoretically measure (if you could measure a average value from
	 * a past year) in the stream.  It is also the value that is used to calibrate
	 * the model to the calibration sites.
	 * 
	 * @param row The row number of the reach
	 * @return The total load with upstream load, combined for all sources.
	 */
	public Double getTotal(int row);
	
	/**
	 * Returns the index of the Total column for the specified source.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return The index of the total column for the spec'ed source.
	 */
	public int getTotalColForSrc(Long srcId);
	
	/**
	 * Returns the Total load (including upstream load) for a specified source
	 * arriving at the bottom of this reach.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param row The row number of the reach
	 * @param srcId The source identifier, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return The total load with upstream load, for a specified source.
	 */
	public Double getTotalForSrc(int row, Long srcId);
	
}
