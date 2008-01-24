package gov.usgswim.sparrow;

import gov.usgswim.ImmutableBuilder;
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
public interface PredictData extends ImmutableBuilder<PredictData> {


	/**
	 * Returns the IDs used to indentify and look up sources
	 * 
	 * SourceIds is a two column Data2D with integer data and it has one row
	 * for each source.  Each row contains the source IDENTIFIER (col 0) and
	 * the DB unique ID (col 1) for a source for the model.  Row position in this
	 * dataset is equal to the column position of the source  in the sourceValue
	 * dataset.
	 * 
	 * For example, this content:<br>
	 * [10] [7392]<br>
	 * [15] [4723]<br>
	 * [17] [4782]<br>
	 * Would mean that column 0 of the src data has an IDENTIFIER of 10 and a db
	 * unique id of 7392.
	 * 
	 * <h4>Data Columns from SOURCE, sorted by SORT_ORDER.</h4>
	 * <ol>
	 * <li>[column 0] Source IDENTIFIER - This column is indexed.
	 * <li>[column 1] SOURCE_ID - DB ID for the source.
	 * </ol>
	 * @return
	 */
	public Data2D getSrcIds();

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
	public Data2D getTopo();

	/**
	 * Returns the coef data.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the coefficient for source k at reach i</p>
	 *
	 * @return The Data2D data
	 */
	public Data2D getCoef();

	/**
	 * Returns the source data
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the source value for source k at reach i</p>
	 *
	 * @return The Data2D data
	 */
	public Data2D getSrc();

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
	public Data2D getDecay();

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
	public Data2D getSys();

	public Data2D getAncil();

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
	public PredictData getImmutable();
	
	/**
	 * Creates an immutable version of the instance optionally forcing all member
	 * variables to be immutable.
	 * This can be 'bad', since new arrays need to be created for all underlying
	 * data.
	 * 
	 * @param forceImmutableMembers If true, copies are made of mutable data
	 * @return
	 */
	public PredictData getImmutable(boolean forceImmutableMembers);
	
}
