package gov.usgswim.sparrow.util;

import static gov.usgswim.sparrow.PredictData.TOPO_FNODE_COL;
import static gov.usgswim.sparrow.PredictData.TOPO_TNODE_COL;
import gov.usgswim.datatable.DataTable;

/**
 * Utility Methods for PredictData
 * @author ilinkuo
 *
 */
public abstract class PredictDataUtils {
	public static Integer getDownstreamNode(DataTable topo, int reachRow) {
		return topo.getInt(reachRow, TOPO_TNODE_COL);
	}
	
	public static Integer getUpstreamNode(DataTable topo, int reachRow) {
		return topo.getInt(reachRow, TOPO_FNODE_COL);
	}
}
