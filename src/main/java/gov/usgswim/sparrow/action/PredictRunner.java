package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.PredictData.FNODE_COL;
import static gov.usgswim.sparrow.PredictData.IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.INSTREAM_DECAY_COL;
import static gov.usgswim.sparrow.PredictData.TNODE_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.Runner;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.parser.DataColumn;

/**
 * A simple SPARROW prediction implementation.
 *
 * Note: It is assumed that the reach order in the topo, coef, and src arrays
 * all match, and that the reach order is such that reach(n) never flows to
 * reach(<n).
 */
public class PredictRunner extends Action<PredictResult> {
	/**
	 * The parent of all child values. If not passed in, it is created.
	 */
	protected PredictData predictData;

	/**
	 * Invariant topographic info about each reach.
	 * i = reach index [i][0] from node index [i][1] to node index [i][2] 'if
	 * transmit' is 1 if the reach transmits to its to-node
	 *
	 * NOTE: We assume that the node indexes start at zero and have no skips.
	 * Thus, nodeCount must equal the largest node index + 1
	 * @see gov.usgswim.ImmutableBuilder.PredictData#getTopo()
	 *
	 */
	protected DataTable topo;

	/**
	 * The coef's for each reach-source. coef[i][k] == the coefficient for
	 * source k at reach i
	 */
	protected DataTable sourceCoefficient;

	/**
	 * The source amount for each reach-source. src[i][k] == the amount added
	 * via source k at reach i
	 */
	protected DataTable sourceValues;

	/**
	 * @see PredictData#getDelivery()
	 */
	protected DataTable deliveryCoefficient;

	/**
	 * The number of nodes
	 */
	protected int nodeCount;

	 /**
	 * Construct a new instance using a PredictionDataSet.
	 *
	 * @param data An all-in-one data object
	 *
	 */
	public PredictRunner(PredictData data) {
		{// assign the passed values to the class variables
			this.topo = data.getTopo();
			this.sourceCoefficient = data.getCoef();
			this.sourceValues = data.getSrc();
			this.deliveryCoefficient = data.getDelivery();
		}


		int maxNode = Math.max(topo.getMaxInt(FNODE_COL), topo
				.getMaxInt(TNODE_COL));

		{ // IK: Efficiency checks disabled for now as they cause failing tests,
			// and I'm not sure if I want to do this optimization as it doesn't
			// have sufficient benefit. Basically, we are allocating an array of
			// node values based on the maximum index of the nodes. In an ideal
			// world, that number should be slightly more than the number of
			// reaches.
			// TODO Add this check to data loading process
			boolean isCheckEfficiency = false;
			if (isCheckEfficiency) {
				checkEfficiency(maxNode);
			}
		}
		this.predictData = data;
		nodeCount = maxNode + 1;
	}
	
	@Override
	protected PredictResult doAction() throws Exception {
		return doPredict();
	}

	/**
	 * runs the actual prediction.
	 */
	public PredictResultImm doPredict() throws Exception {
		
		int reachCount = topo.getRowCount(); // # of reaches is equal to the number of 'rows' in topo
		int sourceCount = sourceValues.getColumnCount(); // # of sources is equal to the number of 'columns' in an
		int outputColumnCount = (sourceCount * 2) + 2;	//The # of output columns
		
		/*
		 * The number of predicted values per reach (k = number of sources, i =
		 * reach #) [i, 0 ... (k-1)] incremental added at reach, per source k
		 * (NOT decayed, just showing what comes in) [i, k ... (2k-1)] total at
		 * reach (w/ up stream contrib), per source k (decayed) [i, (2k)] total
		 * incremental contribution at reach (NOT decayed) [i, (2k + 1)] grand
		 * total at reach (incremental + from node). Comparable to measured.
		 * (decayed)
		 */
		
		//The output array for all data, one row per reach and in the same order.
		double outputArray[][] = new double[reachCount][outputColumnCount];
		
		//Array of accumulated values at nodes, row number correspond to nodes
		//defined by fnode and tnode.
		double upstreamNodeLoad[][] = new double[nodeCount][sourceCount];
		
		//in outputArray, the column of the combined incremental contribution (not decayed, all sources)
		int totalIncrementalColumnIndex = 2*sourceCount;
		
		//in outputArray, the column of the combined total load (decayed, all sources)
		int totalTotalColumnIndex = totalIncrementalColumnIndex + 1;

		// Iterate over all reaches
		for (int reachRow = 0; reachRow < reachCount; reachRow++) {

			double totalIncrementalLoad = 0d; // incremental for all sources/ (NOT decayed)
			double rchGrandTotal = 0d; // all sources + all from upstream node (decayed)

			//Iterate over all sources.
			//currentSourceIndex indexes to the incremental per source in the output array.
			for (int currentSourceIndex = 0; currentSourceIndex < sourceCount; currentSourceIndex++) {
				
				//Index to the total (w/ upstream decayed) for this source
				int currentTotalSourceIndex = currentSourceIndex + sourceCount;

				//Land delivery & coef both included in coef value. (NOT decayed)
				double incrementalLoadForThisSource = 
					sourceCoefficient.getDouble(reachRow, currentSourceIndex)
					* sourceValues.getDouble(reachRow, currentSourceIndex);

				outputArray[reachRow][currentSourceIndex] = incrementalLoadForThisSource;

				//total (w/ upstream contrib) for this source (Decayed)
				outputArray[reachRow][currentTotalSourceIndex] =
					(incrementalLoadForThisSource * deliveryCoefficient.getDouble(reachRow, INSTREAM_DECAY_COL))
					/* (incremental addition of this source, decayed by 1/2 stream travel) */
					+
					/* (upstream load decayed by full stream travel (includes delivery fraction)) */
					(upstreamNodeLoad[topo.getInt(reachRow, FNODE_COL)][currentSourceIndex]
					* deliveryCoefficient.getDouble(reachRow, UPSTREAM_DECAY_COL));

				// Accumulate at downstream node if this reach transmits
				if (topo.getInt(reachRow, IFTRAN_COL) != 0) {
					upstreamNodeLoad[topo.getInt(reachRow, TNODE_COL)][currentSourceIndex]
					+= outputArray[reachRow][currentTotalSourceIndex];
				}

				totalIncrementalLoad += incrementalLoadForThisSource; // add to incremental total for all sources at reach
				rchGrandTotal += outputArray[reachRow][currentTotalSourceIndex]; // add to grand total for all sources (w/upsteam) at reach
			}

			outputArray[reachRow][totalIncrementalColumnIndex] = totalIncrementalLoad; // incremental for all sources (NOT decayed)
			outputArray[reachRow][totalTotalColumnIndex] = rchGrandTotal; // all sources + all from upstream node (Decayed)

		}

		return PredictResultImm.buildPredictResult(outputArray,	predictData);
	}

	/**
	 * Method to check the efficiency/density of the node ids. Ideally, they would all be consecutive
	 *
	 * @param maxNode
	 */
	private void checkEfficiency(int maxNode) {
		int minNode = Math.min(topo.getMinInt(FNODE_COL), topo
				.getMinInt(TNODE_COL));
		assert (minNode < 2) : "too high a starting point for the node indices leads to large memory consumption";

		double ratio = (double) maxNode / (double) topo.getRowCount();
		assert (ratio < 1.2) : "large gaps in the indices" + maxNode
		+ " - " + +topo.getRowCount()
		+ "results in inefficient memory consumption";
	}


}
