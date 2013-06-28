package gov.usgswim.sparrow;

/**
 * A simple SPARROW bootstrap implementation.
 * 
 * Note:  It is assumed that the reach order in the topo, coef, and src arrays
 * all match, and that the reach order is such that reach(n) never flows to
 * reach(<n).
 */
public class BootstrapSimple {

	/**
	 * Invariant topographic info about each reach
	 * i = reach index
	 * [i][0]  from node index
	 * [i][1]  too node index
	 * [i][2]  'if transmit' is 1 if the reach transmits to its too-node
	 * 
	 * NOTE:  We assume that the node indexes start at zero and have no skips.
	 * Thus, nodeCount must equal the largest node index + 1
	 */
	protected int[][] topo;
	
	/**
	 * The coef's for each reach-source.
	 * coef[x][i][k] == the coefficient for source k at reach i, for iteration x.
	 */
	protected double[][][] coef;
	
	/**
	 * The source amount for each reach-source.
	 */
	protected double[][] src;
	
	/**
	 * The number of nodes
	 */
	protected int nodeCount;

	/**
	 * Construct a new instance.
	 * 
	 * @param topo
	 * @param coef
	 * @param src
	 * @param nodeCount
	 */
	public BootstrapSimple(int[][] topo, double[][][] coef, double[][] src, int nodeCount) {
	  this.topo = topo; //assign the passed values to the class variables
	  this.coef = coef;
	  this.src = src;
	  this.nodeCount = nodeCount;
	}
}
