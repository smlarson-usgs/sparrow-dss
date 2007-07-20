package gov.usgswim.sparrow;

import gov.usgswim.sparrow.domain.Model;

public interface PredictionData extends ImmutableBuilder<PredictionData> {
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
	 * <p>One row per reach (i = reach index)</p>
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
	 * instance to a new PredictionDataBuilder instance.  All data in the new
	 * builder will be immutable, but it can be reassigned via set methods.
	 * 
	 * Builder instances should return themselves from this method, i.e., this
	 * method does not ensure a new instance if the current instance is writable.
	 * 
	 * @return
	 */
	public PredictionDataBuilder getBuilder();
	
}
