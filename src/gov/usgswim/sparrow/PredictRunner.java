package gov.usgswim.sparrow;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.Model;

import org.apache.commons.lang.StringUtils;

/**
 * A simple SPARROW prediction implementation.
 *
 * Note:  It is assumed that the reach order in the topo, coef, and src arrays
 * all match, and that the reach order is such that reach(n) never flows to
 * reach(<n).
 */
public class PredictRunner {

	/* NOTE : ALL JAVA ARRAYS ARE ALWAYS ZERO BASED */

	/**
	 * The parent of all child values.  If not passed in, it is created.
	 */
	protected PredictData predictData;
	
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
	 * src[i][k] == the amount added via source k at reach i
	 */
	protected DataTable src;

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
	 * The number of nodes
	 */
	protected int nodeCount;

	/**
	 * Construct a new instance.
	 * 
	 * This constructor figures out the number of nodes, which is non-ideal for
	 * larger data sets.
	 * 
	 * @param topo
	 * @param coef
	 * @param src
	 */
	public PredictRunner(DataTable topo, DataTable coef, DataTable src, DataTable decay) {
		this.topo = topo; //assign the passed values to the class variables
		this.coef = coef;
		this.src = src;
		this.decay = decay;

		int maxNode = topo.getMaxInt();
		
		this.predictData = new PredictDataImm(topo, coef, src, null, decay, null, null, null);

		nodeCount = maxNode + 1;
	}

//	/**
//	 * Construct a new instance using a PredictionDataSet.
//	 * 
//	 * This constructor figures out the number of nodes, which is non-ideal for
//	 * larger data sets.
//	 * @deprecated
//	 * @param data An all-in-one data object
//	 * 
//	 */
//	public PredictRunner(PredictData data) {
//		this.topo = BuilderHelper.fill(new SimpleDataTableWritable(),data.getTopo().getIntData(), null); //assign the passed values to the class variables
//		this.coef = BuilderHelper.fill(new SimpleDataTableWritable(),data.getCoef().getDoubleData(), null);
//		this.src = BuilderHelper.fill(new SimpleDataTableWritable(),data.getSrc().getDoubleData(), null);
//		this.decay = BuilderHelper.fill(new SimpleDataTableWritable(),data.getDecay().getDoubleData(), null);
//
//		int maxNode = topo.getMaxInt();
//
//		nodeCount = maxNode + 1;
//	}

	public PredictRunner(PredictData data) {
		this.topo = data.getTopo(); //assign the passed values to the class variables
		this.coef = data.getCoef();
		this.src = data.getSrc();
		this.decay = data.getDecay();

		int maxNode = topo.getMaxInt();

		this.predictData = data;
		nodeCount = maxNode + 1;
	}


	public PredictResult doPredict() {
		int reachCount = topo.getRowCount();	//# of reachs is equal to the number of 'rows' in topo
		int sourceCount = src.getColumnCount(); //# of sources is equal to the number of 'columns' in an arbitrary row (row zero)

		/*
		 * The number of predicted values per reach (k = number of sources, i = reach)
		 * [i, 0 ... (k-1)]		incremental added at reach, per source k (NOT decayed, just showing what comes in)
		 * [i, k ... (2k-1)]	total at reach (w/ up stream contrib), per source k (decayed)
		 * [i, (2k)]					total incremental contribution at reach (NOT decayed)
		 * [i, (2k + 1)]			grand total at reach (incremental + from node).  Comparable to measured. (decayed)
		 */
		int rchValColCount = (sourceCount * 2) + 2;	


		double rchVal[][] = new double[reachCount][rchValColCount];

		/*
		 * Array of accumulated values at nodes
		 */
		double nodeVal[][] = new double[nodeCount][sourceCount];


		//Iterate over all reaches
		for (int i = 0; i < reachCount; i++)  {

			double rchIncTotal = 0d;	//incremental for all sources (NOT decayed)
			double rchGrandTotal = 0d;	//all sources + all from upstream node (decayed)


			//Iterate over all sources
			for (int k = 0; k < sourceCount; k++)  {

				//temp var to store the incremental per source k.
				//Land delivery and coeff both included in coef value.     (NOT decayed)
				double rchSrcVal = coef.getDouble(i, k) * src.getDouble(i, k);

				rchVal[i][k] = rchSrcVal;	//store to out array

				//total at reach (w/ up stream contrib) per source k (Decayed)
				rchVal[i][k + sourceCount] =
					(rchSrcVal * decay.getDouble(i, 0)) /* Just the decayed source */
					+
					(nodeVal[ topo.getInt(i, 0) ][k] * decay.getDouble(i, 1)); /* Just the decayed upstream portion */

				//Accumulate at downstream node if this reach transmits
				if (topo.getInt(i, 2) != 0) {
					nodeVal[ topo.getInt(i, 1) ][k] += rchVal[i][k + sourceCount];
				}

				rchIncTotal += rchSrcVal;		//add to incremental total for all sources at reach
				rchGrandTotal += rchVal[i][k + sourceCount];	//add to grand total for all sources (w/ upsteam) at reach
			}

			rchVal[i][2 * sourceCount] = rchIncTotal;	//incremental for all sources (NOT decayed)
			rchVal[i][(2 * sourceCount) + 1] = rchGrandTotal;	//all sources + all from upstream node (Decayed)

		}

		return PredictResult.buildPredictResult(rchVal, predictData);

	}

}

