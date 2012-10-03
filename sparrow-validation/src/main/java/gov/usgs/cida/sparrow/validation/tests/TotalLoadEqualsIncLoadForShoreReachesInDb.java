package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.BaseTextFileTester;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.*;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.service.SharedApplication;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class TotalLoadEqualsIncLoadForShoreReachesInDb extends SparrowModelValidationBase {
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	public TotalLoadEqualsIncLoadForShoreReachesInDb(Comparator comparator) {
		super(comparator);
	}
	
	public TestResult testModel(Long modelId) throws Exception {

		AdjustmentGroups emptyAdjustmentGroups = new AdjustmentGroups(modelId);
		PredictData dbPredictData = SharedApplication.getInstance().getPredictData(modelId);
		
		
		BasicAnalysis decayedIncAnalysis = new BasicAnalysis(DataSeriesType.decayed_incremental, null, null, null);
		PredictionContext decayedIncContext = new PredictionContext(modelId, emptyAdjustmentGroups, decayedIncAnalysis,
				null, null, NoComparison.NO_COMPARISON);
		SparrowColumnSpecifier decayedIncResult = SharedApplication.getInstance().getAnalysisResult(decayedIncContext);
		
		BasicAnalysis totalAnalysis = new BasicAnalysis(DataSeriesType.total, null, null, null);
		PredictionContext totalContext = new PredictionContext(modelId, emptyAdjustmentGroups, totalAnalysis,
				null, null, NoComparison.NO_COMPARISON);
		SparrowColumnSpecifier totalResult = SharedApplication.getInstance().getAnalysisResult(totalContext);
		
		
		if (dbPredictData != null && decayedIncResult != null && totalResult != null) {
			testComparison(decayedIncResult, totalResult, dbPredictData, modelId);
		} else {
			recordTestException(modelId, null, "Could not load the db model or its predictions.");
		}


		return result;
	}
	

	public void testComparison(SparrowColumnSpecifier decayedIncResult, SparrowColumnSpecifier totalResult, PredictData dbPredictData, long modelId) throws Exception {
		
		TopoData topo = dbPredictData.getTopo();
		
		for (int row = 0; row < topo.getRowCount(); row++) {

			Long id = topo.getIdForRow(row);
			
			boolean rowIsShoreReach = topo.isShoreReach(row);
			boolean rowIsIfTranOn = topo.isIfTran(row);			

			if (rowIsShoreReach) {
				
				double dbCalcedIncValue =
						decayedIncResult.getDouble(row);
				double dbCalcedTotalValue =
						totalResult.getDouble(row);


				if (! comp(dbCalcedIncValue, dbCalcedTotalValue)) {
						this.recordRowError(modelId, id, row, dbCalcedIncValue, dbCalcedTotalValue, 
								"db Inc", "db total", rowIsShoreReach, rowIsIfTranOn,
								"The DSS predicted decayed incremental value is != the DSS total value for this shore reach");
				} else {
						this.recordRowDebug(modelId, id, row, dbCalcedIncValue, dbCalcedTotalValue, 
								"db Inc", "db total", rowIsShoreReach, rowIsIfTranOn,
								"The DSS predicted decayed incremental value == the DSS total value for this shore reach");
					
				}
			
			}
			

		}
	}
	
}

