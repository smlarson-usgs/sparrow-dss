package gov.usgswim.sparrow;

import gov.usgswim.Immutable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.domain.Model;

/**
 * An immutable implementation of PredictData.
 * PredictData instances contain all the data required to run a prediction.
 * 
 * This class can be cached.
 */
@Immutable
public class PredictDataImm extends AbstractPredictData {


  private static final long serialVersionUID = 13546441L;


	/**
	 * Contains the metadata for the model
	 */
	private final Model model;

	/**
	 * 	 * One row per reach (i = reach index).  Row ID is assigned same as column 0.
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 * 
	 * 
	 * Invariant topographic info about each reach
	 * i = reach index
	 * [i][0]	from node index
	 * [i][1]	too node index
	 * [i][2]	'if transmit' is 1 if the reach transmits to its too-node
	 * 
	 * NOTE:  We assume that the node indexes start at zero and have no skips.
	 * Thus, nodeCount must equal the largest node index + 1
	 */
	private final DataTable topo;

	/**
	 * The coef's for each reach-source.
	 * coef[i][k] == the coefficient for source k at reach i
	 */
	private final DataTable coef;

	/**
	 * The source amount for each reach-source.
	 * Columns in this data are ordered by the SORT_ORDER column in the database.
	 * src[i][k] == the amount added via source k at reach i
	 */
	private final DataTable src;


	private final DataTable srcMetadata;

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
	private final DataTable decay;

	/**
	 * Optional ancillary data.
	 * The structure of this data is not currently defined.
	 */
	private final DataTable ancil;


	/**
	 * Constructs a new dataset w/ all data tables defined.
	 * See matching method docs for complete definitions of each parameter.
	 * 
	 * srcIDs is a single column DataTable with integer data.  See setSourceIDs for
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
	public PredictDataImm(DataTable topo, DataTable coef, DataTable src, DataTable srcIDs,
			DataTable decay, DataTable ancil, Model model) {

		this.model = model;
		this.topo = topo;
		this.coef = coef;
		this.src = src;
		this.decay = decay;
		this.ancil = ancil;
		this.srcMetadata = srcIDs;
	}

	public DataTable getSrcMetadata() {
		return srcMetadata;
	}

	public DataTable getTopo() {
		return topo;
	}

	public DataTable getCoef() {
		return coef;
	}

	public DataTable getSrc() {
		return src;
	}

	public DataTable getDecay() {
		return decay;
	}

	public DataTable getAncil() {
		return ancil;
	}

	public Model getModel() {
		return model;
	}

	public PredictData toImmutable() {
		return this;
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
		// toimmutable calls should be unnecessary
		return new PredictDataBuilder(
			(getTopo() != null)?getTopo().toImmutable():null,
			(getCoef() != null)?getCoef().toImmutable():null,
			(getSrc() != null)?getSrc().toImmutable():null,
			(getSrcMetadata() != null)?getSrcMetadata().toImmutable():null,
			(getDecay() != null)?getDecay().toImmutable():null,
			(getAncil() != null)?getAncil().toImmutable():null,
			(getModel() != null)?getModel():null
		);
	}
}
