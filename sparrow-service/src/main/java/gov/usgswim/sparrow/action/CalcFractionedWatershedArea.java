package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.PredictData.TOPO_IFTRAN_COL;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Calculates the fractioned watershed area for reaches upstream of a single selected reach.
 * 
 * The returned cumulative upstream area is fractioned at each upstream diversion
 * (ie, a split in the river where not all of the upstream load enters a particular
 * reach).  As an example, consider the delta at the mouth of the Mississippi where the
 * river splits into several smaller reaches that actually enter the Gulf.  For
 * a given delta reach, a fraction - say .1 - of the upstream flow may enter that
 * reach.  When calculating the total upstream area of the delta reach, that same
 * fraction, .1 in this case, should be applied to the upstream incremental areas
 * since not all the load from those upstream area flow to the reach.
 * 
 * Similarly, if there is another split (called a diversion) farther upstream,
 * the fractions would compound.
 * 
 * For most parts of a model, the fraction will be one for all reaches with respect
 * to a given downstream reach.  However, any place the river splits, the FRAC
 * value from the model topo table will be some fraction less than one and reaches
 * upstream of this point will have their areas fractionally counted towards
 * downstream cumulative areas.
 * 
 * @author eeverman
 * @see gov.usgswim.sparrow.action.CalcAreaFractionMap
 *
 */
public class CalcFractionedWatershedArea extends Action<Double> {

	protected ReachRowValueMap areaFractionMap;
	protected DataTable incrementalReachAreas;
	
	protected String msg = null;
	
	//Alt config params
	protected transient ReachID reachId = null;
	
	/**
	 * Direct parameter initialization for testing
	 * @param topoData
	 * @param targetReachId 
	 */
	public CalcFractionedWatershedArea(ReachRowValueMap areaFractionMap, DataTable incrementalReachAreas) {
		this.areaFractionMap = areaFractionMap;
		this.incrementalReachAreas = incrementalReachAreas;
	}
	
	/**
	 * Single param cache-key initialization
	 * @param reachId 
	 */
	public CalcFractionedWatershedArea(ReachID reachId) {
		this.reachId = reachId;
	}
	
	
	public void initRequiredFields() {
		if (reachId != null) {
			//topoData = SharedApplication.getInstance().getPredictData(reachId.getModelID()).getTopo();
			areaFractionMap = SharedApplication.getInstance().getReachAreaFractionMap(reachId);
			incrementalReachAreas = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(reachId.getModelID(), AggregationLevel.REACH, false));
		}
		
	}

	@Override
	protected void validate() {
		if (reachId != null) {
			//Everything is OK
		} else if (areaFractionMap == null || incrementalReachAreas == null) {
			this.addValidationError("The areaFractionMap or the incremental Reach Areas is null.");
		}
	}
	
	
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public Double doAction() throws Exception {
		double totalArea = 0D;
		
		for (Integer row : areaFractionMap.keySet()) {
			Float frac = areaFractionMap.getFraction(row);
			Double incArea = incrementalReachAreas.getDouble(row, 1);
			
			if (frac != null && incArea != null) {
				Double addArea = frac * incArea;
				totalArea += addArea;
			}
			
		}
		
		
		return totalArea;
	}
	
	
}
