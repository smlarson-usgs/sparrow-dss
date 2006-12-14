package gov.usgswim.sparrow;

import org.apache.commons.lang.StringUtils;

/**
 * A simple SPARROW prediction implementation.
 *
 * Note:  It is assumed that the reach order in the topo, coef, and src arrays
 * all match, and that the reach order is such that reach(n) never flows to
 * reach(<n).
 */
public class PredictSimple {
	/* NOTE : ALL JAVA ARRAYS ARE ALWAYS ZERO BASED */
	
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
	protected int[][] topo;
	
	/**
	 * The coef's for each reach-source.
	 * coef[i][k] == the coefficient for source k at reach i
	 */
	protected double[][] coef;
	
	/**
	 * The source amount for each reach-source.
	 * src[i][k] == the amount added via source k at reach i
	 */
	protected double[][] src;
	
	/**
	 * The stream and resevor decay
	 * 
	 * src[i][0] == the instream decay at reach i.
	 *   This decay is assumed to be at mid-reach and already computed as such.
	 *   That is, it would normally be the sqr root of the instream decay, and
	 *   it is assumed that this value already has the square root taken.
	 * src[i][1] == the upstream decay at reach i.
	 *   This decay is applied to the load coming from the upstream node.
	 */
	protected double[][] decay;
	
	/**
	 * The labels for the source columns
	 */
	protected String[] sourceLabel;
	
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
	public PredictSimple(int[][] topo, double[][] coef, double[][] src, double[][] decay, String[] sourceLabels) {
		this.topo = topo; //assign the passed values to the class variables
		this.coef = coef;
		this.src = src;
		this.decay = decay;
		this.sourceLabel = sourceLabels;
		
		int maxNode = 0;
		
		for (int i = 0; i < topo.length; i++)  {
			if (topo[i][0] > maxNode) maxNode = topo[i][0];
		  if (topo[i][1] > maxNode) maxNode = topo[i][1];
		}
		
		nodeCount = maxNode + 1;
	}
	
	/**
	 * Construct a new instance.
	 * 
	 * @param topo
	 * @param coef
	 * @param src
	 * @param nodeCount
	 */
	public PredictSimple(int[][] topo, double[][] coef, double[][] src, double[][] decay, String[] sourceLabels, int nodeCount) {
		this.topo = topo;	//assign the passed values to the class variables
		this.coef = coef;
		this.src = src;
	  this.decay = decay;
		this.nodeCount = nodeCount;
	  this.sourceLabel = sourceLabels;
	}

	public Double2D doPredict() {
		int reachCount = topo.length;	//# of reachs is equal to the number of 'rows' in topo
		int sourceCount = src[0].length; //# of sources is equal to the number of 'columns' in an arbitrary row (row zero)
		
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
				double rchSrcVal = coef[i][k] * src[i][k];
				
		    rchVal[i][k] = rchSrcVal;	//store to out array
				
				//total at reach (w/ up stream contrib) per source k (Decayed)
				rchVal[i][k + sourceCount] =
					(rchSrcVal * (1d - decay[i][0])) /* Just the decayed source */
						+
					(nodeVal[ topo[i][0] ][k] * (1d - decay[i][1])); /* Just the decayed upstream portion */
				
				//Accumulate at downstream node if this reach transmits
		    if (topo[i][2] != 0) {
					nodeVal[ topo[i][1] ][k] += rchVal[i][k + sourceCount];
				}
				
				rchIncTotal += rchSrcVal;		//add to incremental total for all sources at reach
				rchGrandTotal += rchVal[i][k + sourceCount];	//add to grand total for all sources (w/ upsteam) at reach
		  }
			
			rchVal[i][2 * sourceCount] = rchIncTotal;	//incremental for all sources (NOT decayed)
		  rchVal[i][(2 * sourceCount) + 1] = rchGrandTotal;	//all sources + all from upstream node (Decayed)
			
		}
		
		//Add some display labels
		String[] head = new String[rchValColCount];
		
		for (int i = 0; i < sourceCount; i++)  {
			String s = null;
		  if (sourceLabel != null && sourceLabel.length > i) {
				s = StringUtils.trimToNull(sourceLabel[i]);
			}
			if (s == null) s = "Source " + i;
			
			head[i] = s + " Inc. Addition";
			head[i + sourceCount] = s + " Total (w/ upstream, decayed)";
		}
		
		head[2 * sourceCount] = "Total Inc. (not decayed)";
	  head[(2 * sourceCount) + 1] = "Grand Total (measurable)";
		
	  return new Double2D(rchVal, head);
		
	}
}
