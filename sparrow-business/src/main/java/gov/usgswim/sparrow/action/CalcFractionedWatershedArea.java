package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;


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
	
	
	/** If true, ignore the fraction and just add up the area.  Mostly for debugging. */
	protected boolean forceNonFractionedArea = false;
	
	/** If true, try to find the immediate upstream reaches in the cache and calc
	 * based on these already computered areas.  Can be much, much faster if
	 * calculating the entire model in hydseq order.
	 */
	protected boolean attemptOptimizedCalc = true;
	
	//Action initialized
	protected TopoData topoData;
	
	
	//Alt config params
	protected ReachID reachId = null;
	
	/**
	 * Direct parameter initialization for testing
	 * @param topoData
	 * @param targetReachId 
	 */
	public CalcFractionedWatershedArea(ReachRowValueMap areaFractionMap, DataTable incrementalReachAreas) {
		this.areaFractionMap = areaFractionMap;
		this.incrementalReachAreas = incrementalReachAreas;
	}
	
	public CalcFractionedWatershedArea(ReachRowValueMap areaFractionMap, DataTable incrementalReachAreas, boolean forceNonFractionedResult) {
		this.areaFractionMap = areaFractionMap;
		this.incrementalReachAreas = incrementalReachAreas;
		this.forceNonFractionedArea = forceNonFractionedResult;
	}
	
	/**
	 * Single param cache-key initialization
	 * @param reachId 
	 */
	public CalcFractionedWatershedArea(ReachID reachId) {
		this.reachId = reachId;
	}
	
	/**
	 * 
	 * @param reachId
	 * @param forceNonFractionedArea If true, use non-fractioned areas
	 */
	public CalcFractionedWatershedArea(ReachID reachId, boolean forceNonFractionedResult, boolean attemptOptimizedCalc) {
		this.reachId = reachId;
		this.forceNonFractionedArea = forceNonFractionedResult;
		this.attemptOptimizedCalc = attemptOptimizedCalc;
	}
	
	public CalcFractionedWatershedArea(ReachID reachId, DataTable incrementalReachAreas, boolean forceNonFractionedResult, boolean attemptOptimizedCalc) {
		this.reachId = reachId;
		this.incrementalReachAreas = incrementalReachAreas;
		this.forceNonFractionedArea = forceNonFractionedResult;
		this.attemptOptimizedCalc = attemptOptimizedCalc;
	}
	
	
	@Override
	public void initFields() {
		if (reachId != null) {
			
			if (incrementalReachAreas == null) {
				incrementalReachAreas = SharedApplication.getInstance().getCatchmentAreas(
						new UnitAreaRequest(reachId.getModelID(), AggregationLevel.REACH, false));
			}
			
			if (topoData == null) {
				topoData = SharedApplication.getInstance().getPredictData(reachId.getModelID()).getTopo();
			}
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
	public Double doAction() throws Exception {
		if (! forceNonFractionedArea) {
			return calcFractionedArea();
		} else {
			return calcUnfractionedArea();
		}
	}
	
	public Double calcFractionedArea() throws Exception {
		Double totalArea = null;
		
		//Can we used an optimized calc?
		if (attemptOptimizedCalc && topoData != null) {
			totalArea = attemptOptimizedFractionedArea();
		}
			
		if (totalArea == null) {

			totalArea = 0d;
			
			//Late init since we may not need it if
			if (areaFractionMap == null) {
				areaFractionMap = SharedApplication.getInstance().getReachAreaFractionMap(reachId, forceNonFractionedArea);
			}

			for (Integer row : areaFractionMap.keySet()) {
				Float frac = areaFractionMap.getFraction(row);
				Double incArea = incrementalReachAreas.getDouble(row, 1);

				if (frac != null && incArea != null) {
					Double addArea = frac * incArea;
					totalArea += addArea;
				}

			}
		}

		return totalArea;
	}
	
	/**
	 * this is really just a debug method
	 * @return
	 * @throws Exception 
	 */
	public Double calcUnfractionedArea() throws Exception {
		Double totalArea = null;
		
		//Can we used an optimized calc?
		if (attemptOptimizedCalc && topoData != null) {
			totalArea = attemptOptimizedFractionedArea();
		}
			
		if (totalArea == null) {
					totalArea = 0d;
			
			//Late init since we may not need it if
			if (areaFractionMap == null) {
				areaFractionMap = SharedApplication.getInstance().getReachAreaFractionMap(reachId, forceNonFractionedArea);
			}
			
			for (Integer row : areaFractionMap.keySet()) {
				Double incArea = incrementalReachAreas.getDouble(row, 1);
				Float frac = areaFractionMap.getFraction(row);

				if (frac != null && incArea != null) {
					totalArea += incArea;
				}
			}
		}
		
		
		return totalArea;
	}
	
	public Double attemptOptimizedFractionedArea() throws Exception {

		int thisRow = topoData.getRowForId(reachId.getReachID());
		double thisIncArea = incrementalReachAreas.getDouble(thisRow, 1);
		int[] upstreamRows = topoData.findAllowedUpstreamReaches(thisRow);
		

		if (upstreamRows.length == 0) {
						
				log.debug("Optimized Fractioned Area Result: Zero upstream reaches, so using incremental area.  " +
					"Model: " + reachId.getModelID() + ", reach row: " + thisRow);
							
				//No upstream reaches, so  just return this reach area
				return thisIncArea;
					
		} else {
			//Found some upstream reaches

			//Total the watershed areas of the reaches immediately above the
			//reach in question.
			double totalUpstreamArea = 0;

			for (int i = 0; i < upstreamRows.length; i++) {

				ReachID upstreamReach = new ReachID(reachId.getModelID(), topoData.getIdForRow(upstreamRows[i]));

				//Quietly get the upstream reach area (returns null if not in cache)
				Double oneUpstreamArea = SharedApplication.getInstance().getFractionedWatershedArea(upstreamReach, true);
				if (oneUpstreamArea == null) {

					log.debug("Optimized Fractioned Area Result: Atleast one upstream area missing in cache.  Cannot use optimized calc.  " +
						"Model: " + reachId.getModelID() + ", reach row: " + thisRow);
					//One of the immediately upstream reaches does not have a watershed
					//area in the cache, so bail on this entire 'optimized' effort.
					return null;

				} else {
					totalUpstreamArea += oneUpstreamArea;
				}

			}

			if (totalUpstreamArea > 0) {
				log.debug("Optimized Fractioned Area Result: Upstream cached areas found, so fractioning areas.  " +
					"Model: " + reachId.getModelID() + ", reach row: " + thisRow);

				double frac = (forceNonFractionedArea)? 1 : topoData.getCorrectedFracForRow(thisRow);
				double fractionedUpstreamArea = frac * totalUpstreamArea;
				double watershedArea = thisIncArea + fractionedUpstreamArea;

				return watershedArea;
			} else {
				log.debug("Optimized Fractioned Area Result: Upstream reaches are non-contributing, so using incremental area.  " +
					"Model: " + reachId.getModelID() + ", reach row: " + thisRow);

				return thisIncArea;
			}



		}
	
	}
}
