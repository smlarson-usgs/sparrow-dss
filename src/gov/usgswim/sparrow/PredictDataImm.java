package gov.usgswim.sparrow;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.Model;

import java.util.HashMap;

/**
 * An immutable implementation of PredictData.
 * PredictData instances contain all the data required to run a prediction.
 * 
 * This class can be cached.
 */
@Immutable
public class PredictDataImm implements PredictData {


	/**
	 * Contains the metadata for the model
	 */
	private final Model model;
	
	
	/**
	 * One row per reach (i = reach index).  Row ID is assigned same as column 0.
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 */
	private final Data2D sys;
	
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
	private final Data2D topo;
	
	/**
	 * The coef's for each reach-source.
	 * coef[i][k] == the coefficient for source k at reach i
	 */
	private final Data2D coef;
	
	/**
	 * The source amount for each reach-source.
	 * Columns in this data are ordered by the SORT_ORDER column in the database.
	 * src[i][k] == the amount added via source k at reach i
	 */
	private final Data2D src;
	
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
	private final Data2D srcIds;
	
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
	private final Data2D decay;
	
	/**
	 * Optional ancillary data.
	 * The structure of this data is not currently defined.
	 */
	private final Data2D ancil;
	
	
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
	public PredictDataImm(Data2D topo, Data2D coef, Data2D src, Data2D srcIDs,
				Data2D decay, Data2D sys, Data2D ancil, Model model) {
				
		this.model = model;
		this.topo = topo;
		this.coef = coef;
		this.src = src;
		this.decay = decay;
		this.sys = sys;
		this.ancil = ancil;
		
		if (srcIDs != null) {
			this.srcIds = srcIDs.buildIntImmutable(0);
		} else {
			srcIds = null;
		}

	}
	
	public int mapSourceId(int id) throws Exception {
		if (srcIds != null) {
		
			int i = srcIds.findRowByIndex((double)id);

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
	
	public Data2D getSrcIds() {
		return srcIds;
	}
	
	public Data2D getTopo() {
		return topo;
	}

	public Data2D getCoef() {
		return coef;
	}

	public Data2D getSrc() {
		return src;
	}

	public Data2D getDecay() {
		return decay;
	}

	public Data2D getSys() {
		return sys;
	}

	public Data2D getAncil() {
		return ancil;
	}

	public Model getModel() {
		return model;
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
		return new PredictDataBuilder(
			(getTopo() != null)?getTopo().getImmutable():null,
			(getCoef() != null)?getCoef().getImmutable():null,
			(getSrc() != null)?getSrc().getImmutable():null,
			(getSrcIds() != null)?getSrcIds().getImmutable():null,
			(getDecay() != null)?getDecay().getImmutable():null,
			(getSys() != null)?getSys().getImmutable():null,
			(getAncil() != null)?getAncil().getImmutable():null,
			(getModel() != null)?getModel():null
		);
	}
}
