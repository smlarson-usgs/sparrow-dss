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
public class PredictDataBuilder implements PredictData {

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
	protected DataTable srcIds;

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
			this.srcIds = srcIDs.toImmutable();
			//this.srcIds = srcIDs.buildIntImmutable(0); old code
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
	public void setSrcIds(DataTable sourceIds) {
		if (sourceIds != null) {
			this.srcIds = sourceIds;
		} else {
			this.srcIds = null;
		}
	}

	public DataTable getSrcIds() {
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

			int i = srcIds.findFirst(0, id);

			if (i > -1) {
				// Running from database, so has a sourceid table
				return i;
			} else  {
				throw new Exception ("Source for id " + id + " not found");
			}
		} else {
			// Running from text file so assume columns in order
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
	 * @return The Data2D data
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


	public PredictDataImm toImmutable() {
		// TODO:  Model should have an immutable builder
		return new PredictDataImm(
				(getTopo() != null)?getTopo().toImmutable():null,
						(getCoef() != null)?getCoef().toImmutable():null,
								(getSrc() != null)?getSrc().toImmutable():null,
										(getSrcIds() != null)?getSrcIds().toImmutable():null,
												(getDecay() != null)?getDecay().toImmutable():null,
														(getSys() != null)?getSys().toImmutable():null,
																(getAncil() != null)?getAncil().toImmutable():null,
																		(getModel() != null)?getModel():null
		);

	}

//	public PredictData2 getImmutable(boolean forceImmutableMembers) {
//	//TODO:  Model should have an immutable builder
//	if (forceImmutableMembers) {
//	return new PredictData2Imm(
//	(getTopo() != null)?getTopo().toImmutable():null,
//	(getCoef() != null)?getCoef().toImmutable():null,
//	(getSrc() != null)?getSrc().toImmutable():null,
//	(getSrcIds() != null)?getSrcIds().toImmutable():null,
//	(getDecay() != null)?getDecay().toImmutable():null,
//	(getSys() != null)?getSys().toImmutable():null,
//	(getAncil() != null)?getAncil().toImmutable():null,
//	(getModel() != null)?getModel():null
//	);
//	} else {
//	return new PredictData2Imm(
//	getTopo(),
//	getCoef(),
//	getSrc(),
//	getSrcIds(),
//	getDecay(),
//	getSys(),
//	getAncil(),
//	getModel()
//	);
//	}

//	}

	public PredictDataBuilder getBuilder() {
		return this;
	}


	static void sampleTheDataLoad(PredictDataBuilder dataSet, String name) {
		//		System.out.println("===== " + name + " ====");
		//		System.out.println(dataSet.getSrcIds().getDouble(0,0) + "-" +dataSet.getSrcIds().getDouble(0,1));
		//		System.out.println(dataSet.getSrcIds().getDouble(1,0) + "-" +dataSet.getSrcIds().getDouble(1,1));
		//		System.out.println(dataSet.getSrcIds().getDouble(2,0) + "-" +dataSet.getSrcIds().getDouble(2,1));
		//		System.out.println(dataSet.getSrcIds().getDouble(3,0) + "-" +dataSet.getSrcIds().getDouble(3,1));
		//
		//		System.out.println(dataSet.getSys().getDouble(0,0) + "-" +dataSet.getSys().getDouble(0,1));
		//		System.out.println(dataSet.getSys().getDouble(1,0) + "-" +dataSet.getSys().getDouble(1,1));
		//		System.out.println(dataSet.getSys().getDouble(2,0) + "-" +dataSet.getSys().getDouble(2,1));
		//		System.out.println(dataSet.getSys().getDouble(3,0) + "-" +dataSet.getSys().getDouble(3,1));
		//
		//		System.out.println(dataSet.getTopo().getDouble(0,0) + "-" +dataSet.getTopo().getDouble(0,1) + dataSet.getTopo().getDouble(0,2) + "-" +dataSet.getTopo().getDouble(0,3));
		//		System.out.println(dataSet.getTopo().getDouble(1,0) + "-" +dataSet.getTopo().getDouble(1,1) + dataSet.getTopo().getDouble(1,2) + "-" +dataSet.getTopo().getDouble(1,3));
		//		System.out.println(dataSet.getTopo().getDouble(2,0) + "-" +dataSet.getTopo().getDouble(2,1) + dataSet.getTopo().getDouble(2,2) + "-" +dataSet.getTopo().getDouble(2,3));
		//		System.out.println(dataSet.getTopo().getDouble(3,0) + "-" +dataSet.getTopo().getDouble(3,1) + dataSet.getTopo().getDouble(3,2) + "-" +dataSet.getTopo().getDouble(3,3));
		//
		//		System.out.println(dataSet.getCoef().getDouble(0,0) + "-" +dataSet.getCoef().getDouble(0,1) + dataSet.getCoef().getDouble(0,2) + "-" +dataSet.getCoef().getDouble(0,3));
		//		System.out.println(dataSet.getCoef().getDouble(1,0) + "-" +dataSet.getCoef().getDouble(1,1) + dataSet.getCoef().getDouble(1,2) + "-" +dataSet.getCoef().getDouble(1,3));
		//		System.out.println(dataSet.getCoef().getDouble(2,0) + "-" +dataSet.getCoef().getDouble(2,1) + dataSet.getCoef().getDouble(2,2) + "-" +dataSet.getCoef().getDouble(2,3));
		//		System.out.println(dataSet.getCoef().getDouble(3,0) + "-" +dataSet.getCoef().getDouble(3,1) + dataSet.getCoef().getDouble(3,2) + "-" +dataSet.getCoef().getDouble(3,3));
		//
		//		System.out.println(dataSet.getDecay().getDouble(0,0) + "-" +dataSet.getDecay().getDouble(0,1));	
		//		System.out.println(dataSet.getDecay().getDouble(1,0) + "-" +dataSet.getDecay().getDouble(1,1));
		//		System.out.println(dataSet.getDecay().getDouble(2,0) + "-" +dataSet.getDecay().getDouble(2,1));
		//		System.out.println(dataSet.getDecay().getDouble(3,0) + "-" +dataSet.getDecay().getDouble(3,1));
		//
		//		System.out.println(dataSet.getSrc().getDouble(0,0) + "-" +dataSet.getSrc().getDouble(0,1));
		//		System.out.println(dataSet.getSrc().getDouble(1,0) + "-" +dataSet.getSrc().getDouble(1,1));
		//		System.out.println(dataSet.getSrc().getDouble(2,0) + "-" +dataSet.getSrc().getDouble(2,1));
		//		System.out.println(dataSet.getSrc().getDouble(3,0) + "-" +dataSet.getSrc().getDouble(3,1));
	}
}

