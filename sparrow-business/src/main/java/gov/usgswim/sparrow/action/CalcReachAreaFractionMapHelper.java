package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import org.apache.commons.lang.ArrayUtils;

/**
 * Corrects FRAC values.
 * 
 * @author eeverman
 *
 */
public class CalcReachAreaFractionMapHelper {

	protected DataTable topoData;
	
	/** If true, FRAC values that do not total to 1 will not be corrected. Mostly for debugging. */
	protected boolean forceUncorrectedFracValues = false;
	
	
	/**
	 * Direct parameter initialization for testing
	 * @param topoData
	 * @param forceUncorrectedFracValues If true, do not correct FRAC values that do not total to one.  Always false for prod.
	 */
	public CalcReachAreaFractionMapHelper(DataTable topoData, boolean forceUncorrectedFracValues) {
		this.topoData = topoData;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
	}
	
	/**
	 * Returns true to indicate that upstream area should be considered when calculating
	 * watershed area for this row.
	 * 
	 * @param row
	 * @return 
	 */
	public boolean reachCanHaveUpstreamReaches(int row) {
		boolean isReachAShoreReach = (1 == topoData.getInt(row, PredictData.TOPO_SHORE_REACH_COL));
		return ! isReachAShoreReach;
	}
	
	public boolean reachCanHaveDownstreamReaches(int row) {
		boolean isReachAShoreReach = (1 == topoData.getInt(row, PredictData.TOPO_SHORE_REACH_COL));
		boolean isTran = topoData.getDouble(row, PredictData.TOPO_IFTRAN_COL) > 0;

 		return (! isReachAShoreReach && isTran);
	}
		
	public int[] findUpstreamRows(int row) {
		long fnode = topoData.getLong(row, PredictData.TOPO_FNODE_COL);

		//The index requires that an Integer be used.
		int[] upstream = topoData.findAll(PredictData.TOPO_TNODE_COL, new Integer((int)fnode));

		return upstream;
	}
	
	public long[] findUpstreamIds(long reachId) {
		int row = topoData.getRowForId(reachId);
		int[] upstreamRows = findUpstreamRows(row);
		
		if (upstreamRows.length == 0) {
			
			return ArrayUtils.EMPTY_LONG_ARRAY;
			
		} else {
			
			long ids[] = new long[upstreamRows.length];
			
			for (int i = 0; i < upstreamRows.length; i++) {
				ids[i] = topoData.getIdForRow(upstreamRows[i]);
			}
			
			return ids;
		}
		
	}
	
	public double getCorrectedFracForReachId(long reachId) throws Exception {
		int row = topoData.getRowForId(reachId);
		return getCorrectedFracForReachRow(row);
	}

	/**
	 * Centralized logic to determine the frac value for a single reach.
	 * FRAC values at a node (where reaches connect) should always sum to one,
	 * meaning that all the flow is allocated in some way to downstream nodes.
	 * However, in some cases modelers have modified the FRAC values to not total
	 * to one to simulate non-network outflows, like municipal water utilities.
	 * 
	 * In other cases, the network modifications might just be errors.  The logic
	 * here forces the sum back to one and adjusts the returned frac value to be
	 * corrected for that adjusted total.
	 * 
	 * @param row
	 * @return
	 * @throws Exception 
	 */
	public double getCorrectedFracForReachRow(int row) throws Exception {
		
		//Bypass switch to use uncorrect values - mostly for debug comparison of models.
		if (forceUncorrectedFracValues) {
			return topoData.getDouble(row, PredictData.TOPO_FRAC_COL);
		}
		
		//Find all other reaches that come from this same node
		Integer fnode = topoData.getInt(row, PredictData.TOPO_FNODE_COL);
		int[] allReachesAtFromFnode = topoData.findAll(PredictData.TOPO_FNODE_COL, fnode);

		if (allReachesAtFromFnode.length == 0) {

			throw new Exception("Could not find any reaches with this fnode '"
					+ fnode + "' for reach row " + row);

		} else if (allReachesAtFromFnode.length == 1) {
			//If only a single reach, the FRAC must be 1.

			return 1d;

		} else {
			//Adjust frac per total

			double fracForReqestedReach = topoData.getDouble(row, PredictData.TOPO_FRAC_COL);
			double fracTotal = 0d;

			for (int i = 0; i < allReachesAtFromFnode.length; i++) {
				double thisFrac = topoData.getDouble(allReachesAtFromFnode[i], PredictData.TOPO_FRAC_COL);
				fracTotal+= thisFrac;
			}

			if (Math.abs(fracTotal - 1D) < .001D) {
				//The total FRAC at this node is within one thousanth of one.
				//OK to use the reach frac value as-is.
				return fracForReqestedReach;
			} else {
				//Adjust the frac based on the total frac
				//Two reaches with fracs of .2 and .2 would have a total of .4, thus
				//an adjusted frac of .2 / .4 == .5
				return fracForReqestedReach / fracTotal;
			}

		}


	}
}
