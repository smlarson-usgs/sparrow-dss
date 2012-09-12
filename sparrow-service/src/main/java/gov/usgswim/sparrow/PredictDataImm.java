package gov.usgswim.sparrow;

import gov.usgswim.Immutable;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.domain.SparrowModel;

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
	private final SparrowModel model;

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
	 * @see PredictData#getDelivery()
	 */
	private final DataTable delivery;

	
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
	 * @param srcIDs
	 * @param delivery
	 * @param ancil
	 * @param model
	 */
	public PredictDataImm(DataTable topo, DataTable coef, DataTable src, DataTable srcIDs,
			DataTable delivery, SparrowModel model) {

		this.model = model;
		this.topo = topo;
		this.coef = coef;
		this.src = src;
		this.delivery = delivery;
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

	public DataTable getDelivery() {
		return delivery;
	}

	public SparrowModel getModel() {
		return model;
	}

	public PredictData toImmutable() {
		return this;
	}

//	public PredictData2 getImmutable(boolean forceImmutableMembers) {
//	//TODO:  SparrowModel should have an immutable builder
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
			(getDelivery() != null)?getDelivery().toImmutable():null,
			(getModel() != null)?getModel():null
		);
	}
}
