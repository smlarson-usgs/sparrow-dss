package gov.usgswim.sparrow.navigation;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;

import java.util.HashSet;
import java.util.Set;

public class NavigationUtils {
	static final int DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY = 1024; // Tune this as we optimize


	public static Set<Long> findUpStreamReaches(Set<Long> targetReaches, DataTable topo){

		int maxReach = findMaxReachRow(targetReaches, topo);
		// start working backwards from maxReach
		// TODO consider a pure array approach as in doPredict.
		// That approach may have additional advantage in levels info retention

		Set<Long> results = new HashSet<Long>(DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY);
		results.add(topo.getIdForRow(maxReach));
		Set<Long> relevantNodes = new HashSet<Long>(DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY);
		Long fnode = topo.getLong(maxReach, PredictData.TOPO_FNODE_COL);
		relevantNodes.add(fnode);
		Set<Long> transmittingNodes = new HashSet<Long>(DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY);
		int iftran = topo.getInt(maxReach, PredictData.TOPO_IFTRAN_COL);
		if (iftran == 1) {
			transmittingNodes.add(fnode);
		}

		// traverse reaches in reverse hydseq order because we're looking upstream
		for (int row = maxReach; row >= 0; row--) {
			Long tnode = topo.getLong(row, PredictData.TOPO_TNODE_COL);
			iftran = topo.getInt(row, PredictData.TOPO_IFTRAN_COL);
			if (relevantNodes.contains(tnode) && transmittingNodes.contains(tnode)) {
				// found one!
				results.add(topo.getIdForRow(row));
				fnode = topo.getLong(row, PredictData.TOPO_FNODE_COL);
				relevantNodes.add(fnode);
				if (iftran == 1) {
					transmittingNodes.add(fnode);
				}
			}
		}

		return results;
	}

	public static Set<Long> findUpStreamReaches(Set<Long> targetReaches, PredictData pd){
		return findUpStreamReaches(targetReaches, pd.getTopo());
	}

	public static int findMaxReachRow(Set<Long> targetReaches, DataTable topo) {
		int maxReach = 0;
		for (Long reachId: targetReaches) {
			maxReach = Math.max(maxReach, topo.getRowForId(reachId));
		}
		return maxReach;
	}
	/*
	public static Set<Integer> findDownStreamReaches(int modelID, Set<Integer> targetReaches){
		return null;
	}

	public static Set<Integer> findUpStreamReaches(int modelID, int targetReach){
		return null;
	}

	public static Set<Integer> findDownStreamReaches(int modelID, int targetReach){
		return null;
	}
	*/
}
