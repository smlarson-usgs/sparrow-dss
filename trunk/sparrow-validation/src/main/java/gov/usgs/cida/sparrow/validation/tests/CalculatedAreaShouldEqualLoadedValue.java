package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationBase;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgswim.sparrow.AreaType;
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
 * @author cschroed
 */
public class CalculatedAreaShouldEqualLoadedValue extends SparrowModelValidationBase {

	private final AreaType areaType;
	//This flag can be set to true to force the fractional watershed area
	//Action to do pure cumulative area calculations, not fractionalal ones.
	private final boolean forceNonFractionedArea = false;

	/** If true, IfTran is ignored for calculating upstream reaches */
	private final boolean forceIgnoreIfTran;


	/** If true, don't correct frac values that do not total to one */
	private final boolean forceUncorrectedFracValues = true;

	int numberOfReachAreaFractionMapsAllowedInMemory_original;
	int numberOfReachAreaFractionMapsAllowedInMemory_forTest = 100000;


	Comparator shoreReachComparator;


	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }



	public TestResult testModel(Long modelId) throws Exception {
		return testModelBasedOnFractionedAreas(modelId);
	}

	public CalculatedAreaShouldEqualLoadedValue(
			Comparator comparator,
			Comparator shoreReachComparator,
			boolean failedTestIsOnlyAWarning,
			AreaType areaType) {
		super(comparator, failedTestIsOnlyAWarning);
		this.shoreReachComparator = shoreReachComparator;
		this.areaType=areaType;
		switch(areaType){
			case TOTAL_CONTRIBUTING:
				this.forceIgnoreIfTran = false;
				break;
			case TOTAL_UPSTREAM:
				this.forceIgnoreIfTran = true;
				break;
			default:
				throw new IllegalArgumentException("Area Type not supported: " + areaType.toString());
		}
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

		recordTrace(modelId, "Starting:  Load " + this.areaType.getName() + " areas from the db");
		DataTable areaLoadedFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, this.areaType));
		recordTrace(modelId, "Completed:  Load " + this.areaType.getName() + " areas from the db");
		recordTrace(modelId, "Starting:  Load incremental areas from the db");
		DataTable incrementalAreasFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AreaType.INCREMENTAL));
		recordTrace(modelId, "Completed:  Load incremental areas from the db");
		recordTrace(modelId, "Starting:  Load model predict data from the db");
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		recordTrace(modelId, "Completed:  Load model predict data from the db");
		TopoData topo = predictData.getTopo();
		//ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(modelId, AggregationLevel.HUC2));

		//All the HUC2s in this model, with the HUC id as the row ID.
		//DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, HucLevel.HUC2));

		int rowCompleteCnt = 0;
		final String errorMsg = "DB "+ areaType.getName() + "area != DB area";
		final String successMsg = "OK - DB "+ areaType.getName() + "area == DB area";
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			Double dbArea = areaLoadedFromDb.getDouble(row, 1);
			ReachID reachUId = new ReachID(modelId, reachId);
			Boolean ifTran = topo.isIfTran(row);

			//Do Fractioned watershed area calc
			recordRowTrace(modelId, reachId, row, "Starting: CalcFractionedWatershedArea");

			FractionedWatershedAreaRequest areaReq = new FractionedWatershedAreaRequest(
					reachUId, forceUncorrectedFracValues, forceIgnoreIfTran, forceNonFractionedArea);
			Double calculatedFractionalWatershedArea =  SharedApplication.getInstance().getFractionedWatershedArea(areaReq);

			recordRowTrace(modelId, reachId, row, "Completed: CalcFractionedWatershedArea");

			//Two different comparisons based on if the reach is a shore reach or not
			if (topo.isShoreReach(row)) {

				Double dbIncArea = incrementalAreasFromDb.getDouble(row, 1);
				if (! shoreReachComparator.comp(dbIncArea, dbArea)) {
					recordRowError(modelId, reachId, row, dbIncArea, dbArea, "inc db", "db", true, ifTran, errorMsg + " (shore reach)");
				} else if (! shoreReachComparator.comp(calculatedFractionalWatershedArea, dbIncArea)) {
					recordRowError(modelId, reachId, row, calculatedFractionalWatershedArea, dbIncArea, "calc", "db", true, ifTran, errorMsg + " (shore reach)");
				}

			} else {
				//Not a shore reach

				if (! comp(dbArea, calculatedFractionalWatershedArea)) {
					recordRowError(modelId, reachId, row, calculatedFractionalWatershedArea, dbArea, "calc", "db", false, ifTran, errorMsg);
				} else {
					if (logIsEnabledForDebug()) {

						recordRowDebug(modelId, reachId, row, calculatedFractionalWatershedArea, dbArea, "calc", "db", false, ifTran, successMsg);
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

