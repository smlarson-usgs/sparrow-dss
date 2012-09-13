package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.service.SharedApplication;

/**
 * Checks that the database model's FRAC values total to ONE at each fnode.
 * 
 * @author eeverman
 */
public class FracValuesShouldTotalToOne extends SparrowModelValidationBase {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .00000001D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	public FracValuesShouldTotalToOne() {}
	
	public FracValuesShouldTotalToOne(double allowedFractialVariance) {
		this.allowedFractialVariance = allowedFractialVariance;
	}
	
	public TestResult testModel(Long modelId) throws Exception {
		
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		DataTable topo = predictData.getTopo();
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			Integer fnode = topo.getInt(row, PredictData.TOPO_FNODE_COL);
			
			Boolean isShoreReach = topo.getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
			Boolean ifTran = topo.getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
				
			
			if (!isShoreReach) {
				//This is a regular reach
				
				int[] allReachesAtFromFnode = topo.findAll(PredictData.TOPO_FNODE_COL, fnode);

				if (allReachesAtFromFnode.length == 0) {
					this.recordTestException(modelId, null, 
							"Could not find any reaches with this fnode '" + fnode + "' for reach id " + reachId + " at row " + row);
					continue;
				}

				double fracTotal = 0d;

				for (int i = 0; i < allReachesAtFromFnode.length; i++) {
					double thisFrac = topo.getDouble(allReachesAtFromFnode[i], PredictData.TOPO_FRAC_COL);
					fracTotal+= thisFrac;
				}

				if (! comp(1d, fracTotal, allowedFractialVariance)) {

					if (allReachesAtFromFnode.length == 1) {
						this.recordRowError(modelId, reachId, row, "1", fracTotal, "1", "db frac total", isShoreReach, ifTran, 
								"FRAC total != 1 for SINGLE REACH.  FNODE: " + fnode);
					} else {
						this.recordRowError(modelId, reachId, row, "1", fracTotal, "1", "db frac total", isShoreReach, ifTran, 
								"FRAC total != 1 for " + allReachesAtFromFnode.length + " reach diversion.  FNODE: " + fnode);
					}

				}
			
			} else {
				//This is a shore reach - it doesn't really matter what the FRAC is,
				//but it probably should be one.
				double thisFrac = topo.getDouble(row, PredictData.TOPO_FRAC_COL);
				
				if (! comp(1d, thisFrac, allowedFractialVariance)) {
					this.recordRowError(modelId, reachId, row, "1", thisFrac, "1", "db frac total", isShoreReach, ifTran, 
							"FRAC total != 1 for SINGLE SHORE REACH.  FNODE: " + fnode);
				}
			}
		}
		
		
		return result;
	}
	
	
}

