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
 * Checks to make sure that all instream and total delivery coef's are less than
 * one and greater than zero.
 * 
 * @author eeverman
 */
public class ReachCoefValuesShouldBeLessThanOneAndGreaterThanZero extends SparrowModelValidationBase {
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	public ReachCoefValuesShouldBeLessThanOneAndGreaterThanZero(Comparator comparator, boolean failedTestIsOnlyAWarning) {
		super(comparator, failedTestIsOnlyAWarning);
	}
	
	public TestResult testModel(Long modelId) throws Exception {


		PredictData dbPredictData = SharedApplication.getInstance().getPredictData(modelId);
		
		
		if (dbPredictData != null) {
			doTest(dbPredictData, modelId);
		} else {
			recordTestException(modelId, null, "Could not load the db model");
		}


		return result;
	}
	

	public void doTest(PredictData dbPredictData, long modelId) throws Exception {
		
		TopoData topo = dbPredictData.getTopo();
		DataTable delivery = dbPredictData.getDelivery();
		
		for (int row = 0; row < topo.getRowCount(); row++) {

			Long id = topo.getIdForRow(row);
			
			boolean rowIsShoreReach = topo.isShoreReach(row);
			boolean rowIsIfTranOn = topo.isIfTran(row);			

			double instreamDeliveryCoef = delivery.getDouble(row, PredictData.INSTREAM_DECAY_COL);
			double totalDeliveryCoef = delivery.getDouble(row, PredictData.UPSTREAM_DECAY_COL);
					
			if (instreamDeliveryCoef > 1D && ! comp(1D, instreamDeliveryCoef)) {
					recordRowError(modelId, id, row, instreamDeliveryCoef, 1D, 
							"instream delivery", "1", rowIsShoreReach, rowIsIfTranOn,
							"The instream delivery coef is > 1");	
			} else if (totalDeliveryCoef > 1D && ! comp(1D, totalDeliveryCoef)) {
					recordRowError(modelId, id, row, totalDeliveryCoef, 1D, 
							"total delivery", "1", rowIsShoreReach, rowIsIfTranOn,
							"The total delivery coef is > 1");	
			} else if (instreamDeliveryCoef < 0D && !comp(0D, instreamDeliveryCoef)) {
					recordRowError(modelId, id, row, instreamDeliveryCoef, 0D, 
							"instream delivery", "0", rowIsShoreReach, rowIsIfTranOn,
							"The instream delivery coef is < 0");	
			} else if (totalDeliveryCoef < 0D && !comp(0D, totalDeliveryCoef)) {
					recordRowError(modelId, id, row, totalDeliveryCoef, 0D, 
							"total delivery", "0", rowIsShoreReach, rowIsIfTranOn,
							"The total delivery coef is < 0");	
			} else {
				recordRowDebug(modelId, id, row, instreamDeliveryCoef, 1D, 
							"delivery coefs", "1", rowIsShoreReach, rowIsIfTranOn,
							"The instream and total delivery coefs are <= 1 and >= 0");
			}


		}
	}
	
}

