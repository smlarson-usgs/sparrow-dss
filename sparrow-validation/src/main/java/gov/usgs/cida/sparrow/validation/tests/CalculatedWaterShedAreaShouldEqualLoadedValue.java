package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.*;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.List;
import net.sf.ehcache.Element;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class CalculatedWaterShedAreaShouldEqualLoadedValue extends SparrowModelValidationBase {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .1D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	//This flag can be set to true to force the fractional watershed area
	//Action to do pure cumulative area calculations, not fractionalal ones.
	private boolean forceNonFractionedArea = false;
	
	/** If true, FRAC values that do not total to 1 will not be corrected. Mostly for debugging. */
	protected boolean forceUncorrectedFracValues = false;
	
	private int numberOfReachAreaFractionMapsAllowedInMemory_original;
	protected int numberOfReachAreaFractionMapsAllowedInMemory_forTest = 5000;

	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	
	public TestResult testModel(Long modelId) throws Exception {
		return testModelBasedOnFractionedAreas(modelId);
		
		//return testModelBasedOnHuc2Aggregation(modelId);
	}
	
	public CalculatedWaterShedAreaShouldEqualLoadedValue() {

	}
	
	@Override
	public void beforeEachTest(Long modelId) {
		numberOfReachAreaFractionMapsAllowedInMemory_original = 
				ConfiguredCache.ReachAreaFractionMap.getCacheImplementation().getCacheConfiguration().getMaxElementsInMemory();
		
		ConfiguredCache.ReachAreaFractionMap.getCacheImplementation().getCacheConfiguration().
				setMaxElementsInMemory(numberOfReachAreaFractionMapsAllowedInMemory_forTest);
		
		super.beforeEachTest(modelId);
	}
	
	@Override
	public void afterEachTest(Long modelId) {
		ConfiguredCache.ReachAreaFractionMap.getCacheImplementation().getCacheConfiguration().
						setMaxElementsInMemory(numberOfReachAreaFractionMapsAllowedInMemory_original);
		ConfiguredCache.ReachAreaFractionMap.getCacheImplementation().removeAll();
		
		super.afterEachTest(modelId);
	}

	/**
	 * 
	 * @param forceAllAreaFractionsToOne Set true to do non-fractional area calcs
	 */
	public CalculatedWaterShedAreaShouldEqualLoadedValue(Double allowedVariance) {
		this.allowedFractialVariance = allowedFractialVariance;
	}
	
	/**
	 * 
	 * @param allowedVariance
	 * @param forceNonFractionedArea Takes precidence over forceUncorrectedFracValues.
	 * @param forceUncorrectedFracValues 
	 */
	public CalculatedWaterShedAreaShouldEqualLoadedValue(Double allowedVariance,
			boolean forceNonFractionedArea, boolean forceUncorrectedFracValues) {
		
		this.allowedFractialVariance = allowedVariance;
		this.forceNonFractionedArea = forceNonFractionedArea;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
	}
	
	
		/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public TestResult testModelBasedOnFractionedAreas(Long modelId) throws Exception {
		
		recordTrace(modelId, "Starting:  Load cumulative areas from the db");
		DataTable cumulativeAreasFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.REACH, true));
		recordTrace(modelId, "Completed:  Load cumulative areas from the db");
		recordTrace(modelId, "Starting:  Load incremental areas from the db");
		DataTable incrementalAreasFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.REACH, false));
		recordTrace(modelId, "Completed:  Load incremental areas from the db");
		recordTrace(modelId, "Starting:  Load model predict data from the db");
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		recordTrace(modelId, "Completed:  Load model predict data from the db");
		DataTable topo = predictData.getTopo();
		//ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(modelId, AggregationLevel.HUC2));
		
		//All the HUC2s in this model, with the HUC id as the row ID.
		//DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, HucLevel.HUC2));
		
		int rowCompleteCnt = 0;
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			Double dbArea = cumulativeAreasFromDb.getDouble(row, 1);
			
			recordRowTrace(modelId, reachId, row, "Starting: CalcReachAreaFractionMap");
			//Calculate the fractioned watershed area, skipping the cache
			//CalcReachAreaFractionMap areaMapAction = new CalcReachAreaFractionMap(topo, reachId, forceUncorrectedFracValues);
			ReachRowValueMap areaMap = SharedApplication.getInstance().getReachAreaFractionMap(new ReachID(modelId, reachId));
			recordRowTrace(modelId, reachId, row, "Completed: CalcReachAreaFractionMap");
			
			recordRowTrace(modelId, reachId, row, "Starting: CalcFractionedWatershedArea");
			CalcFractionedWatershedArea areaAction = new CalcFractionedWatershedArea(areaMap, incrementalAreasFromDb, forceNonFractionedArea, forceUncorrectedFracValues);
			Double calculatedFractionalWatershedArea = areaAction.run();
			recordRowTrace(modelId, reachId, row, "Completed: CalcFractionedWatershedArea");
			
			if (! comp(dbArea, calculatedFractionalWatershedArea, allowedFractialVariance)) {
				Boolean shoreReach = topo.getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
				Boolean ifTran = topo.getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
				recordRowError(modelId, reachId, row, calculatedFractionalWatershedArea, dbArea, "calc", "db", shoreReach, ifTran, "DB Watershed area != calculated area.");
			} else {
				if (logIsEnabledForDebug()) {
					Boolean shoreReach = topo.getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
					Boolean ifTran = topo.getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
					recordRowDebug(modelId, reachId, row, calculatedFractionalWatershedArea, dbArea, "calc", "db", shoreReach, ifTran, "OK.  DB Watershed area == calculated area.");
				}
			}
			
			if (this.logIsEnabledForTrace() && Math.abs((double)(row / 100) - ((double)row / 100d)) < .000001d) {
				//recordTrace(modelId, "Completed " + row + " rows...");
				dumpCacheState(modelId);
			}
			
		}
		
		
		return result;
	}
	
	
	protected void dumpCacheState(Long modelId) {
		for (ConfiguredCache cache : ConfiguredCache.values()) {
			List keys = cache.getKeys();
			
			int size = keys.size();
			int expired = 0;
			
			for (Object o : keys) {
				Element e = cache.getElementQuiet(o);
				if (e.isExpired()) expired++;
			}
			
			if (size != 0) {
				recordTrace(modelId, cache.name() + ": " + size + " Items, " + expired + " are expired.");
			}
		}
	}
	
	
}

