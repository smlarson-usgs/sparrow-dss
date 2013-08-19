package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationBase;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.service.SharedApplication;

/**
 * Checks that the database model's reaches reaches do not have a diversion w/o
 * upstream reaches.  This would be a situation where two reaches flow away from
 * a common FNODE, but there are no upstream reaches.  Perhaps an artesian well
 * could cause this?
 * 
 * @author eeverman
 */
public class DiversionsShouldHaveUpsteamReaches extends SparrowModelValidationBase {
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	public DiversionsShouldHaveUpsteamReaches(boolean failedTestIsOnlyAWarning) {
		super(null, failedTestIsOnlyAWarning);
	}
	
	public TestResult testModel(Long modelId) throws Exception {
		
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		TopoData topo = predictData.getTopo();
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			
			Long reachId = predictData.getIdForRow(row);
			Integer fnode = topo.getFromNode(row);
			Boolean isShoreReach = topo.isShoreReach(row);
			Boolean ifTran = topo.isIfTran(row);
			
			if (!isShoreReach) {
				
				int[] allReachesAtFromFnode = topo.findFlowReachesLeavingSameNode(row);
				int[] upstreamRowsToNode = topo.findAllowedUpstreamReaches(row);
				
				if (upstreamRowsToNode.length == 0) {
					//Headwater reach
					
					if (allReachesAtFromFnode.length > 1) {
						this.recordRowError(modelId, reachId, row, "1", allReachesAtFromFnode.length, "1", "# reaches leaving node", isShoreReach, ifTran, 
								"Multiple reaches leave this FNODE, but there are no upstream reaches.  Artesian well?  FNODE: " + fnode + " (This is unusual, but not really an error)");
					}
				}	
						
			
			}
		}
		
		
		return result;
	}
	
	
}

