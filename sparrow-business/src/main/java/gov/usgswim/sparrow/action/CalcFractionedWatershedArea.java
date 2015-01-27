package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.AreaType;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachAreaFractionMapRequest;
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
 * There are two basic calculation paths:
 * 1) Primary:  If the model id is specific, it is assumed that this action will
 * load the data is needs by invoking other actions.
 * 2) Otherwise, a direct invocation may construct the action specifying
 * areaFractionMap and incrementalReachAreas.  These two provide all the data
 * needed to do the calculation and will not attempt to load any additional data.
 *
 *
 *
 * @author eeverman
 * @see gov.usgswim.sparrow.action.CalcAreaFractionMap
 *
 */
public class CalcFractionedWatershedArea extends Action<Double> {

	protected ReachRowValueMap areaFractionMap;
	protected DataTable incrementalReachAreas;


	protected FractionedWatershedAreaRequest request;

	/** If true, try to find the immediate upstream reaches in the cache and calc
	 * based on these already computered areas.  Can be much, much faster if
	 * calculating the entire model in hydseq order.
	 */
	protected boolean attemptOptimizedCalc = true;

	//Action initialized
	protected TopoData topoData;

	/**
	 * Options affecting how the areaFractionMap is constructed are removed from
	 * this constructor.  Note that forceNonFractionedResult is still available
	 * because it just assumes that the fraction of each reach in the map is one
	 * (what reaches are included in the map is up to the caller).
	 *
	 *
	 * @param areaFractionMap
	 * @param incrementalReachAreas
	 * @param forceNonFractionedResult
	 */
	public CalcFractionedWatershedArea(
			ReachRowValueMap areaFractionMap,
			DataTable incrementalReachAreas,
			boolean forceNonFractionedResult) {

		this.areaFractionMap = areaFractionMap;
		this.incrementalReachAreas = incrementalReachAreas;

		request = new FractionedWatershedAreaRequest(null, false, false, forceNonFractionedResult);
	}

	/**
	 * A special test constructor that allows the optimization to be turned off
	 * and the area data to be specified.
	 *
	 * @param request
	 * @param incrementalReachAreas
	 * @param attemptOptimizedCalc
	 */
	public CalcFractionedWatershedArea(
			FractionedWatershedAreaRequest request,
			DataTable incrementalReachAreas,
			boolean attemptOptimizedCalc) {

		this.request = request;
		this.incrementalReachAreas = incrementalReachAreas;
		this.attemptOptimizedCalc = attemptOptimizedCalc;
	}

	/**
	 * Single param cache-key initialization
	 * @param reachId
	 */
	public CalcFractionedWatershedArea(FractionedWatershedAreaRequest request) {
		this.request = request;
	}


	@Override
	public void initFields() {
		if (request.getReachId() != null) {

			if (incrementalReachAreas == null) {
				incrementalReachAreas = SharedApplication.getInstance().getCatchmentAreas(
						new UnitAreaRequest(request.getReachId().getModelID(), AreaType.INCREMENTAL));
			}

			if (topoData == null) {
				topoData = SharedApplication.getInstance().getPredictData(request.getReachId().getModelID()).getTopo();
			}
		}

	}

	@Override
	protected void validate() {
		if (request.getReachId() != null) {
			//Everything is OK
		} else if (areaFractionMap == null || incrementalReachAreas == null) {
			this.addValidationError("The areaFractionMap or the incremental Reach Areas is null.");
		}
	}

	@Override
	public Double doAction() throws Exception {
		if (! request.isForceNonFractionedResult()) {
			return calcFractionedArea();
		} else {
			return calcUnfractionedArea();
		}
	}

	public Double calcFractionedArea() throws Exception {
		Double totalArea = null;

		//Can we used an optimized calc?
		//topo data is non-null only if the ReachID was specified - pathway 1.
		if (attemptOptimizedCalc && topoData != null) {
			totalArea = attemptOptimizedFractionedArea();
		}

		if (totalArea == null) {

			totalArea = 0d;

			//Late init since we may not need it if
			if (areaFractionMap == null) {
				areaFractionMap = SharedApplication.getInstance().getReachAreaFractionMap(
						request.buildReachAreaFractionMapRequest());
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
				areaFractionMap = SharedApplication.getInstance().getReachAreaFractionMap(
						request.buildReachAreaFractionMapRequest());
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

		int thisRow = topoData.getRowForId(request.getReachId().getReachID());
		double thisIncArea = incrementalReachAreas.getDouble(thisRow, 1);
		int[] upstreamRows = topoData.findAllowedUpstreamReaches(thisRow, request.isForceIgnoreIfTran());


		if (upstreamRows.length == 0) {

				log.debug("Optimized Fractioned Area Result: Zero upstream reaches, so using incremental area.  " +
					"Model: " + request.getReachId().getModelID() + ", reach row: " + thisRow);

				//No upstream reaches, so  just return this reach area
				return thisIncArea;

		} else {
			//Found some upstream reaches

			//Total the watershed areas of the reaches immediately above the
			//reach in question.
			double totalUpstreamArea = 0;

			for (int i = 0; i < upstreamRows.length; i++) {

				ReachID upstreamReach = new ReachID(request.getReachId().getModelID(), topoData.getIdForRow(upstreamRows[i]));

				//Quietly get the upstream reach area (returns null if not in cache)
				Double oneUpstreamArea = SharedApplication.getInstance().getFractionedWatershedArea(
						request.cloneForReachId(upstreamReach), true);

				if (oneUpstreamArea == null) {

					log.debug("Optimized Fractioned Area Result: At least one upstream area missing in cache.  Cannot use optimized calc.  " +
						"Model: " + request.getReachId().getModelID() + ", reach row: " + thisRow);
					//One of the immediately upstream reaches does not have a watershed
					//area in the cache, so bail on this entire 'optimized' effort.
					return null;

				} else {
					totalUpstreamArea += oneUpstreamArea;
				}

			}

			if (totalUpstreamArea > 0) {
				log.debug("Optimized Fractioned Area Result: Upstream cached areas found, so fractioning areas.  " +
					"Model: " +  request.getReachId().getModelID() + ", reach row: " + thisRow);

				double frac = 1d;
				if (! request.isForceNonFractionedResult()) {
					frac = (request.isForceUncorrectedFracValues())? topoData.getFrac(thisRow) : topoData.getCorrectedFracForRow(thisRow);
				}

				double fractionedUpstreamArea = frac * totalUpstreamArea;
				double watershedArea = thisIncArea + fractionedUpstreamArea;

				return watershedArea;
			} else {
				log.debug("Optimized Fractioned Area Result: Upstream reaches are non-contributing, so using incremental area.  " +
					"Model: " + request.getReachId().getModelID() + ", reach row: " + thisRow);

				return thisIncArea;
			}



		}

	}
	
	@Override
	public Long getModelId() {
		if (request != null && request.getReachId() != null) {
			return request.getReachId().getModelID();
		} else {
			return null;
		}
	}
}
