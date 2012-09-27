package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.BaseTextFileTester;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgs.cida.sparrow.validation.framework.ValidationTestUtils;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class TotalLoadEqualsIncLoadForShoreReachesInDb extends BaseTextFileTester {
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	public TotalLoadEqualsIncLoadForShoreReachesInDb(Comparator comparator) {
		super(comparator);
	}
	
	public TestResult testModel(Long modelId) throws Exception {

		AdjustmentGroups emptyAdjustmentGroups = new AdjustmentGroups(modelId);
		PredictData dbPredictData = SharedApplication.getInstance().getPredictData(modelId);
		PredictResult dbPredictResult = SharedApplication.getInstance().getPredictResult(emptyAdjustmentGroups);

		if (dbPredictData != null && dbPredictResult != null) {
			testComparison(dbPredictResult, dbPredictData, modelId);
		} else {
			recordTestException(modelId, null, "Could not load the db model or its predictions.");
		}


		return result;
	}
	

	public void testComparison(PredictResult dbResult, PredictData dbPredictData, long modelId) throws Exception {
		
		for (int row = 0; row < dbPredictData.getTopo().getRowCount(); row++) {

			Long id = dbResult.getIdForRow(row);
			
			boolean rowIsShoreReach = dbPredictData.getTopo().isShoreReach(row);
			boolean rowIsIfTranOn = dbPredictData.getTopo().isIfTran(row);			

			if (rowIsShoreReach) {
				
				double dbCalcedIncValue =
						dbResult.getDouble(row, dbResult.getIncrementalCol());
				double dbCalcedTotalValue =
						dbResult.getDouble(row, dbResult.getTotalCol());


				if (! comp(dbCalcedIncValue, dbCalcedTotalValue)) {
						this.recordRowError(modelId, id, row, dbCalcedIncValue, dbCalcedTotalValue, 
								"db Inc", "db total", rowIsShoreReach, rowIsIfTranOn,
								"The DB inc value does not match the total value for this shore reach");
				}
			
			}
			

		}
	}
	
}

