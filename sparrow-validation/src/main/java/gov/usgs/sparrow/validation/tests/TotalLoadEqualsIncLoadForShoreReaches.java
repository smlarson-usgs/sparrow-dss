package gov.usgs.sparrow.validation.tests;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgs.sparrow.validation.ValidationTestUtils;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class TotalLoadEqualsIncLoadForShoreReaches extends BaseTextFileTester {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .000001D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	private boolean compareTextValues = true;
	private boolean compareDbValues = false;
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return true; }
	
	
	

	
	public TotalLoadEqualsIncLoadForShoreReaches(Double allowedVariance, 
			boolean compareTextValues, boolean compareDbValues) {
		
		this.allowedFractialVariance = allowedVariance;
		this.compareTextValues = compareTextValues;
		this.compareDbValues = compareDbValues;
		
	}
	
	public TestResult testModel(Long modelId) throws Exception {

		DataTable txtModel = this.runner.loadModelTextFile(modelId);
		
		if (txtModel != null) {
			AdjustmentGroups emptyAdjustmentGroups = new AdjustmentGroups(modelId);
			PredictData dbPredictData = SharedApplication.getInstance().getPredictData(modelId);
			PredictResult dbPredictResult = SharedApplication.getInstance().getPredictResult(emptyAdjustmentGroups);
			
			testComparison(txtModel, dbPredictResult, dbPredictData, modelId, compareTextValues, compareDbValues);
		} else {
			recordTestException(modelId, null, "Could not load the text version of the model from file.");
		}


		return result;
	}
	
	/**
	 * 
	 * @param txt
	 * @param dbResult
	 * @param dbPredictData
	 * @param useDecay
	 * @param modelId
	 * @return The number of comparison errors (zero if no errors)
	 * @throws Exception
	 */
	public void testComparison(DataTable txt, PredictResult dbResult, PredictData dbPredictData, long modelId,
			boolean compareTextValues, boolean compareDbValues) throws Exception {
		

		
		String idColStr = txt.getProperty(ValidationTestUtils.ID_COL_KEY);
		int idCol = Integer.parseInt(idColStr);
		
		for (int r = 0; r < txt.getRowCount(); r++) {

			
			Long id = dbResult.getIdForRow(r);
			
			int txtRow = txt.findFirst(idCol, id);
			
			if (txtRow < 0) {
				this.recordRowError(modelId, id, r, "This id (from the text file) could not be found in the db set of data.");
				continue;
			}
			
			
			boolean rowIsShoreReach = dbPredictData.getTopo().getInt(r, PredictData.TOPO_SHORE_REACH_COL) == 1;
			boolean rowIsIfTranOn = dbPredictData.getTopo().getInt(r, PredictData.TOPO_IFTRAN_COL) == 1;			

			if (rowIsShoreReach) {
				
				//Check the total (all sources together) values
				double txtIncValue =
						txt.getDouble(txtRow, getIncAllCol(txt));
				double txtTotalValue =
						txt.getDouble(txtRow, getTotalAllCol(txt));

				double dbCalcedIncValue =
						dbResult.getDouble(r, dbResult.getIncrementalCol());
				double dbCalcedTotalValue =
						dbResult.getDouble(r, dbResult.getTotalCol());


				if (compareTextValues && ! comp(txtIncValue, txtTotalValue, allowedFractialVariance)) {

						this.recordRowError(modelId, id, r, txtIncValue, txtTotalValue, 
								"text Inc", "text total", rowIsShoreReach, rowIsIfTranOn,
								"The TEXT inc value does not match the total value for this shore reach");

				} else if (compareTextValues && ! comp(dbCalcedIncValue, dbCalcedTotalValue, allowedFractialVariance)) {
						this.recordRowError(modelId, id, r, dbCalcedIncValue, dbCalcedTotalValue, 
								"db Inc", "db total", rowIsShoreReach, rowIsIfTranOn,
								"The DB inc value does not match the total value for this shore reach");
				}
			
			}
			

		}
	}
	
}

