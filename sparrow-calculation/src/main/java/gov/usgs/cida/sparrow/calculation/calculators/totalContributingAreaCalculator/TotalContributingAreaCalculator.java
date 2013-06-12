package gov.usgs.cida.sparrow.calculation.calculators.totalContributingAreaCalculator;

import gov.usgs.cida.sparrow.calculation.framework.SparrowCalculatorBase;
import gov.usgs.cida.sparrow.calculation.framework.CalculationResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.AreaType;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.Element;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 *
 * @author eeverman
 */
public class TotalContributingAreaCalculator extends SparrowCalculatorBase {


	/** If true, don't correct frac values that do not total to one */
	private boolean forceUncorrectedFracValues = true;

	/** If true, IfTran is ignored for calculating upstream reaches */
	private boolean forceIgnoreIfTran = false;

	//This flag can be set to true to force the fractional watershed area
	//Action to do pure cumulative area calculations, not fractionalal ones.
	private boolean forceNonFractionedArea = false;

	int numberOfReachAreaFractionMapsAllowedInMemory_original;
	int numberOfReachAreaFractionMapsAllowedInMemory_forTest = 100000;
	private Connection connection;

	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }



	public CalculationResult calculate(Long modelId) throws Exception {
		return this.calculateTotalContributingAreaAndPutInDb(modelId);
		//return testModelBasedOnHuc2Aggregation(modelId);
	}

	public TotalContributingAreaCalculator(
			boolean failedTestIsOnlyAWarning) {
		super(failedTestIsOnlyAWarning);
		try {
			this.connection = SharedApplication.getInstance().getRWConnection();
		} catch (SQLException ex) {
			Logger.getLogger(TotalContributingAreaCalculator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @param forceNonFractionedArea Takes precidence over forceUncorrectedFracValues.
	 * @param forceUncorrectedFracValues
	 */
	public TotalContributingAreaCalculator(
			boolean failedTestIsOnlyAWarning,
			boolean forceNonFractionedArea,
			boolean forceIgnoreIfTran,
			boolean forceUncorrectedFracValues) {

		super(failedTestIsOnlyAWarning);
		this.forceNonFractionedArea = forceNonFractionedArea;
		this.forceIgnoreIfTran = forceIgnoreIfTran;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;

	}

	@Override
	public void beforeEachCalc(Long modelId) {
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

		super.beforeEachCalc(modelId);

		try {
			this.connection = SharedApplication.getInstance().getRWConnection();
		} catch (SQLException ex) {
			Logger.getLogger(TotalContributingAreaCalculator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void afterEachCalc(Long modelId) {
		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().getCacheConfiguration().
						setMaxElementsInMemory(numberOfReachAreaFractionMapsAllowedInMemory_original);
		ConfiguredCache.FractionedWatershedArea.getCacheImplementation().removeAll();

		super.afterEachCalc(modelId);
	}

	/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public CalculationResult calculateTotalContributingAreaAndPutInDb(Long modelId) throws Exception {

		recordTrace(modelId, "Starting:  Load model predict data from the db");
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		recordTrace(modelId, "Completed:  Load model predict data from the db");
		TopoData topo = predictData.getTopo();
		//ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(modelId, AggregationLevel.HUC2));

		//All the HUC2s in this model, with the HUC id as the row ID.
		//DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, HucLevel.HUC2));

		int topoRowCount = topo.getRowCount();
		//@todo really use a hashmap, or just an arraylist of {reachId, area} pairs?
		IdAreaPair[] idAreaPairs = new IdAreaPair[topoRowCount];

		for (int row = 0; row < topoRowCount; row++) {
			Long reachId = predictData.getIdForRow(row);
			Double calculatedFractionalWatershedArea = null;
			ReachID reachUId = new ReachID(modelId, reachId);
			//Do Fractioned watershed area calc
			recordRowTrace(modelId, reachId, row, "Starting: CalcFractionedWatershedArea");

			FractionedWatershedAreaRequest areaReq = new FractionedWatershedAreaRequest(
					reachUId, forceUncorrectedFracValues, forceIgnoreIfTran, forceNonFractionedArea);
			calculatedFractionalWatershedArea =  SharedApplication.getInstance().getFractionedWatershedArea(areaReq);
			idAreaPairs[row] = new IdAreaPair(reachId, calculatedFractionalWatershedArea);
			recordRowTrace(modelId, reachId, row, "Completed: CalcFractionedWatershedArea");
		}

		for(IdAreaPair pair : idAreaPairs){
			this.recordInfo(modelId, pair.toString(), false);
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

