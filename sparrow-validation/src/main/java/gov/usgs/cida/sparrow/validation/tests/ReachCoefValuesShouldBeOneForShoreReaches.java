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
public class ReachCoefValuesShouldBeOneForShoreReaches extends SparrowModelValidationBase {
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	public ReachCoefValuesShouldBeOneForShoreReaches(Comparator comparator, boolean failedTestIsOnlyAWarning) {
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

			if (rowIsShoreReach) {
				
				double instreamDeliveryCoef = delivery.getDouble(row, PredictData.INSTREAM_DECAY_COL);
				double totalDeliveryCoef = delivery.getDouble(row, PredictData.UPSTREAM_DECAY_COL);

				if (! comp(instreamDeliveryCoef, 1D)) {
						this.recordRowError(modelId, id, row, instreamDeliveryCoef, 1D, 
								"instream delivery", "1", rowIsShoreReach, rowIsIfTranOn,
								"The instream delivery coef != 1 for this shore reach.  Not an error, but very uncommon. (total may be off as well)");
						
				} else if (! comp(totalDeliveryCoef, 1D)) {
						this.recordRowError(modelId, id, row, totalDeliveryCoef, 1D, 
								"total delivery", "1", rowIsShoreReach, rowIsIfTranOn,
								"The total delivery coef != 1 for this shore reach.  Not an error, but very uncommon.");
				} else {
						this.recordRowDebug(modelId, id, row, instreamDeliveryCoef, 1D, 
								"delivery coefs", "1", rowIsShoreReach, rowIsIfTranOn,
								"The instream and total delivery coef == 1, which is the common case for a shore reach.)");
				}
			
			}
			

		}
	}
	
}

