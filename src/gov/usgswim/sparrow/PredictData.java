package gov.usgswim.sparrow;

import gov.usgswim.ImmutableBuilder;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.PredictDataImm;
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
public interface PredictData extends ImmutableBuilder<PredictDataImm> {

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
	 * Maps a source id to its column index in the src data.
	 *
	 * If there is no source id map, it is assumed that there are no IDs for the sources (i.e.,
	 * the prediction is being run from a text file), and ID are auto generated
	 * such that the first column of the sources is given an id of 1 (not zero).
	 *
	 * See the Adjustment class, which implements the same strategy (and should
	 * be kept in sync).
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public int mapSourceId(int id) throws Exception;

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

	/**
	 * Creates an immutable version of the instance, making a shallow copy of
	 * member variables.
	 * 
	 * If the member variables are mutable, they will remain mutable.
	 * @return
	 */
	public PredictDataImm toImmutable();

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

}

