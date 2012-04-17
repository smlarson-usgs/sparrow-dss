package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable.Immutable;

/**
 * A Datatable of sparrow prediction results with convenience getXXX methods
 * appropriate to the content
 * 
 * SPARROW specific data access methods are provided so that clients do not need
 * to know how the data is structured in the table.
 * 
 * One guarantee that this class makes is that the Total column for a given series
 * always follows the individual source columns.  For instance, all the per source
 * incremental columns are immediately followed by the total incremental column.
 * 
 * @author eeverman
 */
public interface PredictResult extends Immutable {

	/**
	 * Returns the number of sources.
	 * @return A count.
	 */
	public int getSourceCount();
	
	/**
	 * Returns the column index for the incremental column, which is
	 * the non-decayed total addition for all sources.
	 * @return A column index.
	 */
	public int getIncrementalCol();
	
	/**
	 * Returns the column index for the decayed incremental column, which is
	 * the decayed total addition for all sources.
	 * @return A column index.
	 */
	public int getDecayedIncrementalCol();
	
	/**
	 * Returns the index of the total load column.
	 * @return A column index value.
	 */
	public int getTotalCol();
	
	/**
	 * Returns the column index of the first source specific incremental column.
	 * 
	 * @return A column index.
	 */
	public int getFirstIncrementalColForSrc();
	
	/**
	 * Returns the column index of the first source specific decayed incremental
	 * column.
	 * 
	 * @return A column index.
	 */
	public int getFirstDecayedIncrementalColForSrc();
	
	/**
	 * Returns the column index of the first source specific total load
	 * column.
	 * 
	 * @return A column index value.
	 */
	public int getFirstTotalColForSrc();
	
	
	/**
	 * Returns the zero based column index of the incremental values of the
	 * specified source.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return
	 */
	public int getIncrementalColForSrc(Integer srcId);
	
	/**
	 * Returns the zero based column index of the decayed incremental values for
	 * the specified source.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return
	 */
	public int getDecayedIncrementalColForSrc(Integer srcId);
	
	/**
	 * Returns the zero based index of the Total column for the specified source.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return The index of the total column for the spec'ed source.
	 */
	public int getTotalColForSrc(Integer srcId);
	
	/**
	 * Returns the incremental addition (not decayed by instream decay) of the
	 * model constituent for all sources at this reach, for the specified row.
	 * 
	 * @param row The row index (zero based) of the reach
	 * @return The incremental addition from all sources at this reach
	 */
	public Double getIncremental(int row);
	
	/**
	 * Returns the decayed incremental addition (decayed by instream decay) of the
	 * model constituent for all sources at this reach, for the specified row.
	 * 
	 * @param row The row index (zero based) of the reach
	 * @return The decayed incremental addition from all sources at this reach
	 */
	public Double getDecayedIncremental(int row);
	
	/**
	 * Returns the incremental addition (not decayed by instream decay) of the
	 * model constituent for the specified source at this reach.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param row The row index (zero based) of the reach
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return The incremental addition from the specified source at this reach
	 */
	public Double getIncrementalForSrc(int row, Integer srcId);
	
	/**
	 * Returns the decayed incremental addition (decayed by instream decay) of the
	 * model constituent for the specified source at this reach.
	 * 
	 * If the predict data is loaded from a file and does not have source IDs,
	 * the 1 based ordinal position of the source is used as the source ID.
	 * 
	 * @param row The row index (zero based) of the reach
	 * @param srcId The source id, or if none due to a file loaded dataset,
	 * the 1 based ordinal of the source.
	 * @return The decayed incremental addition of the specified source at this reach
	 */
	public Double getDecayedIncrementalForSrc(int row, Integer srcId);
	
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
	public Double getTotalForSrc(int row, Integer srcId);
	
}
