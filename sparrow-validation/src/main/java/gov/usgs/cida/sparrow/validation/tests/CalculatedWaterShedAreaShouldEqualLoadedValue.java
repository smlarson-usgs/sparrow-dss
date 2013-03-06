package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationBase;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.action.*;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachAreaFractionMapRequest;
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
	

	/** If true, don't correct frac values that do not total to one */
	private boolean forceUncorrectedFracValues = false;
	
	/** If true, IfTran is ignored for calculating upstream reaches */
	private boolean forceIgnoreIfTran = false;
	
	//This flag can be set to true to force the fractional watershed area
	//Action to do pure cumulative area calculations, not fractionalal ones.
	private boolean forceNonFractionedArea = false;
	
	int numberOfReachAreaFractionMapsAllowedInMemory_original;
	int numberOfReachAreaFractionMapsAllowedInMemory_forTest = 100000;
	
	
	Comparator shoreReachComparator;

	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	
	public TestResult testModel(Long modelId) throws Exception {
		return testModelBasedOnFractionedAreas(modelId);
		
		//return testModelBasedOnHuc2Aggregation(modelId);
	}
	
	public CalculatedWaterShedAreaShouldEqualLoadedValue(
			Comparator comparator,
			Comparator shoreReachComparator,
			boolean failedTestIsOnlyAWarning) {
		super(comparator, failedTestIsOnlyAWarning);
		this.shoreReachComparator = shoreReachComparator;
	}
	
	/**
	 * Constructor with options to force non-standard values for calculating the
	 * fractioned watershed areas.
	 * 
	 * Note that setting either flag to false can significantly slow down the validation
	 * for large models.  For instance, the North East NHD model can run for 12 hours
	 * or more.
	 * 
	 * @param allowedVariance
	 * @param forceNonFractionedArea Takes precidence over forceUncorrectedFracValues.
	 * @param forceUncorrectedFracValues 
	 */
	public CalculatedWaterShedAreaShouldEqualLoadedValue(
			Comparator comparator,
			Comparator shoreReachComparator,
			boolean failedTestIsOnlyAWarning,
			boolean forceNonFractionedArea,
			boolean forceIgnoreIfTran,
			boolean forceUncorrectedFracValues) {
		
		super(comparator, failedTestIsOnlyAWarning);
		this.shoreReachComparator = shoreReachComparator;
		this.forceNonFractionedArea = forceNonFractionedArea;
		this.forceIgnoreIfTran = forceIgnoreIfTran;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
		
	}
	
	@Override
	public void beforeEachTest(Long modelId) {
		numberOfReachAreaFractionMapsAllowedInMemory_original = 
				ConfiguredCache.FractionedWatershedArea.getCacheImplementation().getCacheConfiguration().getMaxElementsInMemory();
		
		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().getCacheConfiguration().
				setMaxElementsInMemory(numberOfReachAreaFractionMapsAllowedInMemory_forTest);
		
		//The settings for this test are important, so record them to the log
		this.recordInfo(modelId,
				"Settings:  forceNonFractionedArea: " + forceNonFractionedArea + 
				", forceIgnoreIfTran: " + forceIgnoreIfTran + 
				", forceUncorrectedFracValues:" + forceUncorrectedFracValues ,
				true);
		
		super.beforeEachTest(modelId);
	}
	
	@Override
	public void afterEachTest(Long modelId) {
		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().getCacheConfiguration().
						setMaxElementsInMemory(numberOfReachAreaFractionMapsAllowedInMemory_original);
		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().removeAll();
		
		super.afterEachTest(modelId);
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
		TopoData topo = predictData.getTopo();
		//ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(modelId, AggregationLevel.HUC2));
		
		//All the HUC2s in this model, with the HUC id as the row ID.
		//DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, HucLevel.HUC2));
		
		int rowCompleteCnt = 0;
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			Double dbArea = cumulativeAreasFromDb.getDouble(row, 1);
			Double calculatedFractionalWatershedArea = null;
			ReachID reachUId = new ReachID(modelId, reachId);
			Boolean ifTran = topo.isIfTran(row);

			//Do Fractioned watershed area calc
			recordRowTrace(modelId, reachId, row, "Starting: CalcFractionedWatershedArea");

			FractionedWatershedAreaRequest areaReq = new FractionedWatershedAreaRequest(
					reachUId, forceUncorrectedFracValues, forceIgnoreIfTran, forceNonFractionedArea);
			calculatedFractionalWatershedArea =  SharedApplication.getInstance().getFractionedWatershedArea(areaReq);

			recordRowTrace(modelId, reachId, row, "Completed: CalcFractionedWatershedArea");
				
			//Two different comparisons based on if the reach is a shore reach or not
			if (topo.isShoreReach(row)) {


				Double dbIncArea = incrementalAreasFromDb.getDouble(row, 1);
				if (! shoreReachComparator.comp(dbIncArea, dbArea)) {
					recordRowError(modelId, reachId, row, dbIncArea, dbArea, "inc db", "db", true, ifTran, "DB Watershed area != DB incremenatal area (shore reach)");
				} else if (! shoreReachComparator.comp(calculatedFractionalWatershedArea, dbIncArea)) {
					recordRowError(modelId, reachId, row, calculatedFractionalWatershedArea, dbIncArea, "calc", "db", true, ifTran, "DB Inc area != calculated area (shore reach)");
				}

			} else {
				//Not a shore reach
				
				if (! comp(dbArea, calculatedFractionalWatershedArea)) {
					recordRowError(modelId, reachId, row, calculatedFractionalWatershedArea, dbArea, "calc", "db", false, ifTran, "DB Watershed area != calculated area.");
				} else {
					if (logIsEnabledForDebug()) {
						
						recordRowDebug(modelId, reachId, row, calculatedFractionalWatershedArea, dbArea, "calc", "db", false, ifTran, "OK.  DB Watershed area == calculated area.");
					}
				}

				if (this.logIsEnabledForTrace() && Math.abs((double)(row / 100) - ((double)row / 100d)) < .000001d) {
					//recordTrace(modelId, "Completed " + row + " rows...");
					dumpCacheState(modelId);
				}
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

