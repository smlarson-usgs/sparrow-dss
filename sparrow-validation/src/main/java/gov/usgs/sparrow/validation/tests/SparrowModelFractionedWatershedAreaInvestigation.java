package gov.usgs.sparrow.validation.tests;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.*;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;

/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class SparrowModelFractionedWatershedAreaInvestigation extends SparrowModelValidationBase {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .1D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	
	public TestResult testModel(Long modelId) throws Exception {
		return compareFractionedWatershedAreasToUnfractioned(modelId);
	}
	
	
		/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public TestResult compareFractionedWatershedAreasToUnfractioned(Long modelId) throws Exception {
		
		DataTable incrementalAreasFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.REACH, false));
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		DataTable topo = predictData.getTopo();
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			
			
			//Calculate the fractioned watershed area, skipping the cache
			CalcReachAreaFractionMap areaMapAction = new CalcReachAreaFractionMap(topo, reachId, false);
			ReachRowValueMap areaMap = areaMapAction.run();
		
			CalcFractionedWatershedArea fractionedAreaAction = new CalcFractionedWatershedArea(areaMap, incrementalAreasFromDb);
			Double fractionalWatershedArea = fractionedAreaAction.run();
			
			CalcFractionedWatershedArea unfractionedAreaAction = new CalcFractionedWatershedArea(areaMap, incrementalAreasFromDb, true);
			Double unfractionalWatershedArea = unfractionedAreaAction.run();

			if (! comp(fractionalWatershedArea, unfractionalWatershedArea, allowedFractialVariance)) {
				Boolean shoreReach = topo.getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
				Boolean ifTran = topo.getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
				recordRowError(modelId, reachId, row, fractionalWatershedArea, unfractionalWatershedArea, "frac", "unfrac", shoreReach, ifTran, "Fractioned Watershed area != unfractioned area.");
			}
			

		}
		
		
		return result;
	}
	
	
}

