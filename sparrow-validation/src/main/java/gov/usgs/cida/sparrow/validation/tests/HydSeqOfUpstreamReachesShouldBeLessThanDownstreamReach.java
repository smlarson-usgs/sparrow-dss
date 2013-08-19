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
 * Ensures that for each reach (except shore reaches), the immediately upstream reaches have hydseq
 * numbers less then the current reach.
 * 
 * @author eeverman
 */
public class HydSeqOfUpstreamReachesShouldBeLessThanDownstreamReach extends SparrowModelValidationBase {
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	/**
	 * The comparator is not actually used.
	 * 
	 * @param comparator
	 * @param failedTestIsOnlyAWarning 
	 */
	public HydSeqOfUpstreamReachesShouldBeLessThanDownstreamReach(Comparator comparator, boolean failedTestIsOnlyAWarning) {
		super(comparator, failedTestIsOnlyAWarning);
	}
		
	/**
	 * The comparator is not acutally used, so isn't required.
	 * 
	 * @param failedTestIsOnlyAWarning 
	 */
	public HydSeqOfUpstreamReachesShouldBeLessThanDownstreamReach(boolean failedTestIsOnlyAWarning) {
		super(null, failedTestIsOnlyAWarning);
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
		
		for (int row = 0; row < topo.getRowCount(); row++) {

			Long id = topo.getIdForRow(row);
			
			boolean rowIsShoreReach = topo.isShoreReach(row);
			boolean rowIsIfTranOn = topo.isIfTran(row);			

			if (! rowIsShoreReach) {
				
				int hydSeq = topo.getHydSeq(row);
				int[] upstream = topo.findAnyUpstreamReaches(row);
				boolean ok = true;
				
				for (int upstreamRow : upstream) {
					int upstreamHydSeq = topo.getHydSeq(upstreamRow);
					
					if (upstreamHydSeq >= hydSeq) {
						this.recordRowError(modelId, id, row, hydSeq, upstreamHydSeq, 
								"hydseq", "upstream hydseq", rowIsShoreReach, rowIsIfTranOn,
								"The upstream hydseq is greater than the hydseq of this reach.  Upstream reach ID: " + topo.getIdForRow(upstreamRow) +
								"  Is this a circular loop of reaches?  Or a real issue??");
						
						ok = false;
					}
					
				}

				if (ok) {
					this.recordRowDebug(modelId, id, row, hydSeq, null, 
							"hydseq", "", rowIsShoreReach, rowIsIfTranOn,
							"The hydseq of all immediately upstream reaches are less than than the hydseq of this reach.  All OK.");
				}
			
			}
			

		}
	}
	
}

