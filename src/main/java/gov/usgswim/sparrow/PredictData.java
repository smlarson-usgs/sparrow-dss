package gov.usgswim.sparrow;

import gov.usgswim.ImmutableBuilder;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.domain.SparrowModel;

import java.io.Serializable;

/**
 * A PredictData instance contains all the numerical data needed to run a prediction.
 * 
 * Instances may be be mutable or immutable, but will be forced
 * immutable (via the ImmutableBuilder interface) so that they can be cached.
 * 
 * Note that instances will tend to be very large since they can contain many
 * thousands of rows of data.
 * 
 * Compared to PredictRequest, this class contains the actual data
 * needed to run the prediction whereas PredictRequest only contains
 * the ID of the model to run, adjustment information, and the type of prediction
 * to run.
 */
public interface PredictData extends ImmutableBuilder<PredictData>, Serializable {
	// topo columns
	public static final int MODEL_REACH_COL = 0;
	public static final int FNODE_COL = 1;
	public static final int TNODE_COL = 2;
	public static final int IFTRAN_COL = 3;
	public static final int HYDSEQ_COL = 4;
	
	// decay columns
	public static final int INSTREAM_DECAY_COL = 0;
	public static final int UPSTREAM_DECAY_COL = 1;
	
	/**
	 * Returns the source metadata, which includes ids, names, units, and other metadata.
	 * 
	 * 
	 * 
	 * SrcMetadata contains a row for each source type in the model.  The source type
	 * identifier (model specific, it is not based on db ID) is the row id for each
	 * row.  Row position in this dataset is equal to the column position of the
	 * source  in the sourceValue dataset.
	 * 
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <h5>IDENTIFIER - The Row ID (not a column). The SparrowModel specific ID for the source (starting w/ 1)</h5>
	 * <ol>
	 * <li>SOURCE_ID - (long) The database unique ID for the source
	 * <li>NAME - (String) The full (long text) name of the source
	 * <li>DISPLAY_NAME - (String) The short name of the source, used for display
	 * <li>DESCRIPTION - (String) A description of the source (could be long)
	 * <li>CONSTITUENT - (String) The name of the Constituent being measured
	 * <li>UNITS - (String) The units the constituent is measured in
	 * <li>PRECISION - (int) The number of decimal places
	 * <li>IS_POINT_SOURCE (boolean) 'T' or 'F' values that can be mapped to boolean.
	 * </ol>
	 * @return DataTable
	 */
	public DataTable getSrcMetadata();
	/**

	 *
	 * @return
	 */

	/**
	 * Returns the topographic data
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).
	 * Row ID is IDENTIFIER REACH_ID</p>
	 * <ol>
	 * <li>Index: - The model specific reach identifier
	 * <li>[0][0] MODEL_REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] FNODE - The from node
	 * <li>[i][2] TNODE - The to node
	 * <li>[i][3] IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * <li>[i][4] (optional - this column and others ignored) HYDSEQ - Sort identifier such that any lower number reach is upstream of a higher numbered one.
	 * </ol>
	 *
	 * NOTE:  Node indexes that do not start at zero or have large skips will
	 * require more memory to process.
	 * @return The DataTable data
	 */
	public DataTable getTopo();

	/**
	 * Returns the coef data.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the coefficient for source k at reach i</p>
	 *
	 * @return The DataTable data
	 */
	public DataTable getCoef();

	/**
	 * Returns the source data
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the source value for source k at reach i</p>
	 *
	 * @return The DataTable data
	 */
	public DataTable getSrc();

	/**
	 * Returns the decay data.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream delivery at reach i.<br>
	 * This delivery is assumed to be at mid-reach and already computed as such.
	 * That is, it would normally be 1 minus the sqr root of the total decay,
	 * but this conversion is already done for this value.
	 * 
	 * <li>src[i][1] == the upstream delivery at reach i.<br>
	 * This delivery is applied to the load coming from the upstream node.
	 * This is really a combined term, [frac] * [total delivery].  Think of
	 * nodes as having two sides, an upper side that accumulates all the load
	 * from the upstream reach or (in the case of a confluence) reaches, and a
	 * lower side of that has the load entering this reach.  Frac is the
	 * fraction of the load on the 'top' of the upstream node that enters this
	 * reach.  Frac is typically 1 unless the river splits.  Total delivery
	 * is the fraction entering the top of this reach that makes it to the,
	 * bottom, i.e., the converse of total decay.
	 * Thus, our combined upstream delivery is the fraction of the load reaching
	 * the 'top' of the upstream node that makes it to the bottom of this reach.
	 * <li>Additional columns ignored
	 * </ol>
	 *
	 * @return
	 */
	public DataTable getDelivery();

	public DataTable getAncil();

	public SparrowModel getModel();

	/**
	 * Returns an editable copy of the current PredictionDataSet.
	 *
	 * The copy simply copies the data (as immutable Data2D's) from the current
	 * instance to a new PredictionDataBuilder instance.  ALL data in the new
	 * builder will be immutable, but it can be reassigned via set methods.
	 *
	 * Builder instances should return themselves from this method, i.e., this
	 * method does not ensure a new instance if the current instance is writable.
	 *
	 * @return
	 */
	public PredictDataBuilder getBuilder();


//	/**
//	* Creates an immutable version of the instance optionally forcing all member
//	* variables to be immutable.
//	* This can be 'bad', since new arrays need to be created for all underlying
//	* data.
//	* 
//	* @param forceImmutableMembers If true, copies are made of mutable data
//	* @return
//	*/
//	public PredictData2 getImmutable(boolean forceImmutableMembers);
	
	
	/**
	 * Returns the source column index corresponding to the passed source ID.
	 * 
	 * @param id The model specific source id (not the db id)
	 * @return
	 * @throws Exception If the source ID cannot be found.
	 */
	public int getSourceIndexForSourceID(Integer id) throws Exception;
	
	/**
	 * Returns the source ID for the passed source index.
	 * 
	 * The source index is the sequential index of the source, starting with zero,
	 * when the sources are sorted by their source order.  The source ID (Identifier)
	 * is the model unique ID assigned to that source, which may or may not match
	 * the index.
	 * 
	 * The source index is also the order by which all source related data is
	 * stored in tables.  Thus, a zero index number means that the source values
	 * are stored in the first column of the src table.
	 * 
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public Long getSourceIdForSourceIndex(int index) throws Exception;
	
	/**
	 * Returns the row index corresponding to the passed reach id.
	 * 
	 * This row index applies to all 'per-reach' datatables.
	 * 
	 * @param id The model specific reach id (not the db id)
	 * @return
	 * @throws Exception If the reach ID cannot be found.
	 */
	public int getRowForReachID(Long id) throws Exception;
	
	/**
	 * Returns the row index corresponding to the passed reach id.
	 * 
	 * This row index applies to all 'per-reach' datatables.
	 * 
	 * @param id The model specific reach id (not the db id)
	 * @return
	 * @throws Exception If the reach ID cannot be found.
	 */
	public int getRowForReachID(Integer id) throws Exception;

	/**
	 * Returns a reach Identifier for the row (not the db ID)
	 *
	 * @param row The zero based row number
	 * @return reach ID for the row.
	 */
	public Long getIdForRow(int row);
}

