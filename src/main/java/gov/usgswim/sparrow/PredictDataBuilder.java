package gov.usgswim.sparrow;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.domain.Model;

/**
 * A mutable implementation of PredictData.
 * PredictData instances contain alll the data required to run a prediction.
 *
 * This class is not thread safe!  Once created, this class may be cached and
 * used for prediction runs, so do not reassign or change the values it contains!
 */
public class PredictDataBuilder extends AbstractPredictData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 351354348641L;


	/**
	 * Contains the metadata for the model
	 */
	protected Model model;


	/**
	 * One row per reach (i = reach index).  Row ID is assigned same as column 0.
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 */
	protected DataTable sys;

	/**
	 * Invariant topographic info about each reach
	 * i = reach index
	 * [i][0]	from node index
	 * [i][1]	too node index
	 * [i][2]	'if transmit' is 1 if the reach transmits to its too-node
	 * 
	 * NOTE:  We assume that the node indexes start at zero and have no skips.
	 * Thus, nodeCount must equal the largest node index + 1
	 */
	protected DataTable topo;

	/**
	 * The coef's for each reach-source.
	 * coef[i][k] == the coefficient for source k at reach i
	 */
	protected DataTable coef;

	/**
	 * The source amount for each reach-source.
	 * Columns in this data are ordered by the SORT_ORDER column in the database.
	 * src[i][k] == the amount added via source k at reach i
	 */
	protected DataTable src;

	protected DataTable srcMetadata;

	/**
	 * The stream and resevor decay.  The values in the array are *actually* 
	 * delivery, which is (1 - decay).  I.E. the delivery calculation is already
	 * done.
	 * 
	 * src[i][0] == the instream decay at reach i.
	 *   This decay is assumed to be at mid-reach and already computed as such.
	 *   That is, it would normally be the sqr root of the instream decay, and
	 *   it is assumed that this value already has the square root taken.
	 * src[i][1] == the upstream decay at reach i.
	 *   This decay is applied to the load coming from the upstream node.
	 */
	protected DataTable decay;

	/**
	 * Optional ancillary data.
	 * The structure of this data is not currently defined.
	 */
	protected DataTable ancil;

	public PredictDataBuilder() {
	}


	/**
	 * Constructs a new dataset w/ all data tables defined.
	 * See matching method docs for complete definitions of each parameter.
	 * 
	 * srcIDs is a single column Data2D with integer data.  See setSourceIDs for
	 * details.
	 * 
	 * @param topo
	 * @param coef
	 * @param src
	 * @param decay
	 * @param sys
	 * @param ancil
	 * @param model
	 * @param srcIDs
	 */
	public PredictDataBuilder(DataTable topo, DataTable coef, DataTable src, DataTable srcIDs, DataTable decay,
			DataTable sys, DataTable ancil, Model model) {

		this.model = model;
		this.topo = topo;
		this.coef = coef;
		this.src = src;
		this.decay = decay;
		this.sys = sys;
		this.ancil = ancil;

		if (srcIDs != null) {
			this.srcMetadata = srcIDs.toImmutable();
			//this.srcIds = srcIDs.buildIntImmutable(0); old code
		}

	}


	/**
	 * Assigns the soruce metadata, which includes ids, names, units, and other metadata.
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
	 * <li>UNITS - (String) The units the constituent is measured in
	 * <li>PRECISION - (int) The number of decimal places
	 * <li>IS_POINT_SOURCE (boolean) 'T' or 'F' values that can be mapped to boolean.
	 * </ol>
	 * 
	 * @param sourceMetaata
	 */
	public void setSrcMetadata(DataTable sourceMetaata) {
		if (sourceMetaata != null) {
			this.srcMetadata = sourceMetaata;
		} else {
			this.srcMetadata = null;
		}
	}

	public DataTable getSrcMetadata() {
		return srcMetadata;
	}


	/**
	 * Assigns the topo data.
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ.  One row per reach (i = reach index)</h4>
	 * <ol>
	 * <li>[i][0] FNODE - The from node
	 * <li>[i][1] TNODE - The to node
	 * <li>[i][2] IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * </ol>
	 * 
	 * NOTE:  Node indexes that do not start at zero or have large skips will
	 * require more memory to process.
	 * @param topo
	 */
	public void setTopo(DataTable topo) {
		this.topo = topo;
	}

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
	public DataTable getTopo() {
		return topo;
	}

	/**
	 * Set the Coef data
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the coefficient for source k at reach i</p>
	 * @param coef
	 */
	public void setCoef(DataTable coef) {
		this.coef = coef;
	}

	/**
	 * Returns the coef data.
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the coefficient for source k at reach i</p>
	 * 
	 * @return The Data2D data
	 */
	public DataTable getCoef() {
		return coef;
	}


	/**
	 * Sets the source data
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the source value for source k at reach i</p>
	 * @param src
	 */
	public void setSrc(DataTable src) {
		this.src = src;
	}

	/**
	 * Returns the source data
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the source value for source k at reach i</p>
	 * 
	 * @return The DataTable data
	 */
	public DataTable getSrc() {
		return src;
	}

	/**
	 * Assigns the decay values.
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream decay at reach i.<br>
	 *   This decay is assumed to be at mid-reach and already computed as such.
	 *   That is, it would normally be the sqr root of the instream decay, and
	 *   it is assumed that this value already has the square root taken.
	 * <li>src[i][1] == the upstream decay at reach i.<br>
	 *   This decay is applied to the load coming from the upstream node.
	 * <li>Additional columns ignored
	 * </ol>
	 * 
	 * @param decay
	 */
	public void setDecay(DataTable decay) {
		this.decay = decay;
	}

	/**
	 * Returns the decay data.
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream decay at reach i.<br>
	 *   This decay is assumed to be at mid-reach and already computed as such.
	 *   That is, it would normally be the sqr root of the instream decay, and
	 *   it is assumed that this value already has the square root taken.
	 * <li>src[i][1] == the upstream decay at reach i.<br>
	 *   This decay is applied to the load coming from the upstream node.
	 * <li>Additional columns ignored
	 * </ol>
	 * 
	 * @return
	 */
	public DataTable getDecay() {
		return decay;
	}

	/**
	 * Assigns the system information, which is used to correlate to other data in the db.
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 *
	 * @param sys
	 */
	public void setSys(DataTable sys) {
		this.sys = sys;
	}

	/**
	 * Returns the system information, which is used to correlate to other data in the db.
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
	public DataTable getSys() {
		return sys;
	}

	public void setAncil(DataTable ancil) {
		this.ancil = ancil;
	}

	public DataTable getAncil() {
		return ancil;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}


	public PredictData toImmutable() {
		// TODO:  Model should have an immutable builder
		DataTable topo2 = (getTopo() != null)?getTopo().toImmutable():null;
		DataTable coef2 = (getCoef() != null)?getCoef().toImmutable():null;
		DataTable source2 = (getSrc() != null)?getSrc().toImmutable():null;
		DataTable sourceIds2 = (getSrcMetadata() != null)?getSrcMetadata().toImmutable():null;
		DataTable decay2 = (getDecay() != null)?getDecay().toImmutable():null;
		DataTable sys2 = (getSys() != null)?getSys().toImmutable():null;
		DataTable ancil2 = (getAncil() != null)?getAncil().toImmutable():null;
		Model model2 = (getModel() != null)?getModel():null;
		
		return new PredictDataImm(topo2, coef2, source2, sourceIds2, decay2, sys2, ancil2, model2);

	}



	public PredictDataBuilder getBuilder() {
		return this;
	}

}

