package gov.usgswim.sparrow;

import java.io.Serializable;

import gov.usgswim.ImmutableBuilder;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.domain.Model;

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

	/**
	 * Returns the soruce metadata, which includes ids, names, units, and other metadata.
	 * 
	 * SrcMetadata contains a row for each source in the model.  The source
	 * identifier (model specific, it is not based on db ID) is the row id for each
	 * row.  Row position in this dataset is equal to the column position of the
	 * source  in the sourceValue dataset.
	 * 
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <h5>IDENTIFIER - The Row ID (not a column). The Model specific ID for the source (starting w/ 1)</h5>
	 * <ol>
	 * <li>SOURCE_ID - (long) The database unique ID for the source
	 * <li>NAME - (String) The full (long text) name of the source
	 * <li>DISPLAY_NAME - (String) The short name of the source, used for display
	 * <li>DESCRIPTION - (String) A description of the source (could be long)
	 * <li>CONSTITUENT - (String) The name of the Constituent being measured
	 * <li>UNITS - (STring) The units the constituent is measured in
	 * <li>PRECISION - (int) The number of decimal places
	 * <li>IS_POINT_SOURCE (boolean) 'T' or 'F' values that can be mapped to boolean.
	 * </ol>
	 * @return
	 */
	public DataTable getSrcMetadata();

	/**
	 * Returns the topo data.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ.</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] FNODE - The from node
	 * <li>[i][1] TNODE - The to node
	 * <li>[i][2] IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * <li>[i][3] (optional - this column and others ignored) HYDSEQ - Sort identifier such that any lower number reach is upstream of a higher numbered one.
	 * </ol>
	 *
	 * NOTE:  Node indexes that do not start at zero or have large skips will
	 * require more memory to process.
	 * @return The Data2D data
	 */
	public DataTable getTopo();

	/**
	 * Returns the coef data.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the coefficient for source k at reach i</p>
	 *
	 * @return The Data2D data
	 */
	public DataTable getCoef();

	/**
	 * Returns the source data
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the source value for source k at reach i</p>
	 *
	 * @return The Data2D data
	 */
	public DataTable getSrc();

	/**
	 * Returns the decay data.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream decay at reach i.<br>
	 * This decay is assumed to be at mid-reach and already computed as such.
	 * That is, it would normally be the sqr root of the instream decay, and
	 * it is assumed that this value already has the square root taken.
	 * <li>src[i][1] == the upstream decay at reach i.<br>
	 * This decay is applied to the load coming from the upstream node.
	 * <li>Additional columns ignored
	 * </ol>
	 *
	 * @return
	 */
	public DataTable getDecay();

	/**
	 * Returns the system information, which is optional but required if the
	 * results are to be correlated to other data in the db.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).
	 * Row ID is assigned same as column 0.</p>
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 *
	 * @return
	 */
	public DataTable getSys();

	public DataTable getAncil();

	public Model getModel();

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

}

