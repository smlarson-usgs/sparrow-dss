package gov.usgswim.sparrow;

import gov.usgswim.sparrow.domain.Model;


/**
 * A mutable implementation of PredictData.
 * PredictData instances contain alll the data required to run a prediction.
 *
 * This class is not thread safe!  Once created, this class may be cached and
 * used for prediction runs, so do not reassign or change the values it contains!
 */
public class PredictDataBuilder implements PredictData {


	/**
	 * Contains the metadata for the model
	 */
	protected Model model;
	
	
	/**
	 * One row per reach (i = reach index)
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 */
	protected Data2D sys;
	
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
	protected Data2D topo;
	
	/**
	 * The coef's for each reach-source.
	 * coef[i][k] == the coefficient for source k at reach i
	 */
	protected Data2D coef;
	
	/**
	 * The source amount for each reach-source.
	 * Columns in this data are ordered by the SORT_ORDER column in the database.
	 * src[i][k] == the amount added via source k at reach i
	 */
	protected Data2D src;
	
	/**
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
	 */
	protected Data2D srcIds;
	
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
	protected Data2D decay;
	
	/**
	 * Optional ancillary data.
	 * The structure of this data is not currently defined.
	 */
	protected Data2D ancil;
	
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
	public PredictDataBuilder(Data2D topo, Data2D coef, Data2D src, Data2D srcIDs, Data2D decay,
				Data2D sys, Data2D ancil, Model model) {
				
		this.model = model;
		this.topo = topo;
		this.coef = coef;
		this.src = src;
		this.decay = decay;
		this.sys = sys;
		this.ancil = ancil;
		
		if (srcIDs != null) {
			this.srcIds = srcIDs.buildIntImmutable(0);
		}

	}
	
	
	/**
	 * Assigns the IDs used to look up sources
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
	 * If passed as null, it is assumed that there are no IDs for the sources (i.e.,
	 * the prediction is being run from a text file), and ID are auto generated
	 * in which the first column of the sources is given an id of 1 (not zero).
	 * 
	 * @param sourceIds
	 */
	public void setSrcIds(Data2D sourceIds) {
		if (sourceIds != null) {
			this.srcIds = sourceIds.buildIntImmutable(0);
		} else {
			this.srcIds = null;
		}
	}
	
	public Data2D getSrcIds() {
		return srcIds;
	}
	
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
	public int mapSourceId(int id) throws Exception {
		if (srcIds != null) {
		
			int i = srcIds.findRowById((double)id);

			if (i > -1) {
				return i;
			} else  {
				throw new Exception ("Source for id " + id + " not found");
			}
		} else {
			if (id > 0) {
				return id - 1;
			} else {
				throw new Exception("Invalid source id " + id + ", which must be greater then zero.");
			}
		}
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
	public void setTopo(Data2D topo) {
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
	public Data2D getTopo() {
		return topo;
	}

	/**
	 * Set the Coef data
	 *
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the coefficient for source k at reach i</p>
	 * @param coef
	 */
	public void setCoef(Data2D coef) {
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
	public Data2D getCoef() {
		return coef;
	}
	 
	 
	/**
	 * Sets the source data
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the source value for source k at reach i</p>
	 * @param src
	 */
	public void setSrc(Data2D src) {
		this.src = src;
	}

	/**
	 * Returns the source data
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index).  coef[i][k] == the source value for source k at reach i</p>
	 * 
	 * @return The Data2D data
	 */
	public Data2D getSrc() {
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
	public void setDecay(Data2D decay) {
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
	public Data2D getDecay() {
		return decay;
	}

	/**
	 * Assigns the system information, which is optional but required if the
	 * results are to be correlated to other data in the db.
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
	public void setSys(Data2D sys) {
		this.sys = sys;
	}

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
	public Data2D getSys() {
		return sys;
	}

	public void setAncil(Data2D ancil) {
		this.ancil = ancil;
	}

	public Data2D getAncil() {
		return ancil;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	

	public PredictData getImmutable() {
		return getImmutable(false);
	}
	
	public PredictData getImmutable(boolean forceImmutableMembers) {
		//TODO:  Model should have an immutable builder
		if (forceImmutableMembers) {
			return new PredictDataImm(
				(getTopo() != null)?getTopo().getImmutable():null,
				(getCoef() != null)?getCoef().getImmutable():null,
				(getSrc() != null)?getSrc().getImmutable():null,
				(getSrcIds() != null)?getSrcIds().getImmutable():null,
				(getDecay() != null)?getDecay().getImmutable():null,
				(getSys() != null)?getSys().getImmutable():null,
				(getAncil() != null)?getAncil().getImmutable():null,
				(getModel() != null)?getModel():null
			);
		} else {
			return new PredictDataImm(
				getTopo(),
				getCoef(),
				getSrc(),
				getSrcIds(),
				getDecay(),
				getSys(),
				getAncil(),
				getModel()
			);
		}
	}

	public PredictDataBuilder getBuilder() {
		return this;
	}
}
