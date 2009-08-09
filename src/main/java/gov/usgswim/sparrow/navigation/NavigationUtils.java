package gov.usgswim.sparrow.navigation;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;

import java.util.HashSet;
import java.util.Set;

public class NavigationUtils {
	static final int DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY = 1024; // Tune this as we optimize

	/*
	 * 1) Understand headreach and shore reach
	 * 2) U
	 *
	-- select downstream reaches of reach 1842204
	select lpad(' ',level-1) || model_reach_id
		from sparrow_dss.model_reach
		start with model_reach_id = 1842204 and sparrow_model_id = 22
		connect by prior tnode = fnode;

	-- select upstream reaches of reach 1842236
	select lpad(' ',level-1) || model_reach_id
		from sparrow_dss.model_reach
		start with model_reach_id = 1842236 and sparrow_model_id = 22
		connect by prior fnode = tnode;

		============
		You dont need the duplicative network filter also.

	select unique level, lpad(' ',level-1) || enh_reach_id, a.*
		from stream_network.ENH_ATTRIB_VW a
		where enh_network_id = 23
		start with identifier=111
		connect by prior fnode = tnode and prior head_reach<1
		order by hydseq desc;

	select unique level, lpad(' ',level-1) || enh_reach_id, a.*
		from stream_network.ENH_ATTRIB_VW a
		where enh_network_id = 23
		start with enh_reach_id=452060
		connect by prior fnode = tnode and prior head_reach<1
		order by hydseq desc;

select ...
from model_attrib_vw
where sparrow_model_id = $modelId
start with model_reach_id=$reachId
connect by prior fnode = tnode and prior hydseq < hydseq and prior iftran = 1

try reachid = 11183 in model 22
	 */

	public static Set<Long> findUpStreamReaches(long modelID, Set<Long> targetReaches, PredictData pd){
/*

select level, lpad(' ',level-1) || model_reach_id, model_attrib_vw.*
from model_attrib_vw
where sparrow_model_id = 22
start with identifier = 11147
connect by prior fnode = tnode
  and prior hydseq > hydseq
  and prior iftran = 1

		*/


		DataTable data = pd.getTopo();
		int maxReach = findMaxReachRow(targetReaches, data);
		// start working backwards from maxReach
		// TODO consider a pure array approach as in doPredict.
		// That approach may have additional advantage in levels info retention

		Set<Long> results = new HashSet<Long>(DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY);
		results.add(data.getIdForRow(maxReach));
		Set<Long> relevantNodes = new HashSet<Long>(DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY);
		Long fnode = data.getLong(maxReach, PredictData.FNODE_COL);
		relevantNodes.add(fnode);
		Set<Long> transmittingNodes = new HashSet<Long>(DEFAULT_INITIAL_UPSTREAM_REACH_CAPACITY);
		int iftran = data.getInt(maxReach, PredictData.IFTRAN_COL);
		if (iftran == 1) {
			transmittingNodes.add(fnode);
		}

		// traverse reaches in reverse hydseq order because we're looking upstream
		for (int row = maxReach; row >= 0; row--) {
			Long tnode = data.getLong(row, PredictData.TNODE_COL);
			iftran = data.getInt(row, PredictData.IFTRAN_COL);
			if (relevantNodes.contains(tnode) && transmittingNodes.contains(tnode)) {
				// found one!
				results.add(data.getIdForRow(row));
				fnode = data.getLong(row, PredictData.FNODE_COL);
				relevantNodes.add(fnode);
				if (iftran == 1) {
					transmittingNodes.add(fnode);
				}
			}
		}

		return results;
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
