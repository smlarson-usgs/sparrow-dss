package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.BaseTextFileTester;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationRunner.PromptResponse;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.*;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.service.SharedApplication;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class ReachViewer extends SparrowModelValidationBase {
	
	protected Long modelId;
	protected Long reachId;
	protected PredictData predictData;
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	public ReachViewer(Comparator comparator) {
		super(comparator, false);
	}
	
	public TestResult testModel(Long modelId) throws Exception {

		this.modelId = modelId;
		
		while (promptUserValues()) {
			viewReach();
		}

		return new TestResult(modelId, this.getClass().getSimpleName());
	}
	
	protected void viewReach() {
		AdjustmentGroups emptyAdjustmentGroups = new AdjustmentGroups(modelId);
		PredictData dbPredictData = SharedApplication.getInstance().getPredictData(modelId);
		PredictResult dbPredictResult = SharedApplication.getInstance().getPredictResult(emptyAdjustmentGroups);
			
		TopoData topo = dbPredictData.getTopo();
		Integer reachRow = topo.getRowForId(reachId);
		
		if (reachRow == null || reachRow < 0) {
			System.out.println("Could not find the reach id " + reachId + " in model " + modelId);
			return;
		}
		
		int[] upstreamRows = topo.findAnyUpstreamReaches(reachRow);
		int[] downstreamRows = topo.findAnyDownstreamReaches(reachRow);
		
		System.out.println("");
		System.out.println("**********************************");
		System.out.println("** Upstream Reaches **************");
		for (int i : upstreamRows) {
			printReachDetail(dbPredictData, dbPredictResult, i);
		}
		System.out.println("**********************************");
		System.out.println("** THIS Reach  *******************");
		printReachDetail(dbPredictData, dbPredictResult, reachRow);
		
		System.out.println("**********************************");
		System.out.println("** Downstream Reaches ************");
		for (int i : downstreamRows) {
			printReachDetail(dbPredictData, dbPredictResult, i);
		}
		System.out.println("**********************************");
		
		
	}
	
	protected void printReachDetail(PredictData data, PredictResult result, int row) {
		TopoData topo = data.getTopo();
		String border = topo.isShoreReach(row)?"s":"r";
		
		if (topo.isShoreReach(row)) {
			System.out.println("sssssssssssssssss");
		} else {
			System.out.println("rrrrrrrrrrrrrrrrr");
		}
		
		System.out.println(border + " Row      : " + row);
		System.out.println(border + " ID       : " + topo.getIdForRow(row));
		System.out.println(border + " IfT      : " + topo.isIfTran(row));
		System.out.println(border + " FRAC     : " + topo.getFrac(row));
		System.out.println(border + " f/t node : " + topo.getFromNode(row) + " / " + topo.getToNode(row));
		System.out.println(border + " ---------------");
		System.out.println(border + " Delivery Fracs (instream and upstream)");
		System.out.println(border + " inStream : " + data.getDelivery().getDouble(row, PredictData.INSTREAM_DECAY_COL));
		System.out.println(border + " upStream : " + data.getDelivery().getDouble(row, PredictData.UPSTREAM_DECAY_COL));
		System.out.println(border + " ---------------");
		System.out.println(border + " IncLoad  : " + result.getIncremental(row));
		System.out.println(border + " TotLoad  : " + result.getTotal(row));
		
		if (topo.isShoreReach(row)) {
			System.out.println("sssssssssssssssss");
		} else {
			System.out.println("rrrrrrrrrrrrrrrrr");
		}
	}
	

	protected boolean promptUserValues() {
//		if (modelId == null) {
//			PromptResponse resp = promptModelId();
//			if (resp.isQuit()) return false;
//			modelId = resp.parseAsLong();
//		}
		
		PromptResponse resp = promptReachId();
		if (resp.isQuit()) return false;
		reachId = resp.parseAsLong();
		
		return true;
	}
	
	
//	protected PromptResponse promptModelId() {
//		System.out.println("");
//		SparrowModelValidationRunner.PromptResponse response = runner.prompt("Which model ID (or 'quit') : ");
//		if (response.isQuit()) {
//			return response;
//		} else if (response.isEmptyOrNull()) {
//			System.out.println("Sorry, I didn't get that.");
//			return promptReachId();
//		} else {
//			String strVal = response.getNullTrimmedStrResponse();
//			if (response.parseAsLong() != null && response.parseAsLong() > 0) {
//				return response;
//			} else {
//				System.out.println("Sorry, I didn't get that.");
//				return promptReachId();
//			}
//		}
//		
//	}
		
	protected SparrowModelValidationRunner.PromptResponse promptReachId() {
		System.out.println("");
		SparrowModelValidationRunner.PromptResponse response = runner.prompt("Which reach ID (or 'quit') : ");
		if (response.isQuit()) {
			return response;
		} else if (response.isEmptyOrNull()) {
			System.out.println("Sorry, I didn't get that.");
			return promptReachId();
		} else {
			String strVal = response.getNullTrimmedStrResponse();
			if (response.parseAsLong() != null && response.parseAsLong() > 0) {
				return response;
			} else {
				System.out.println("Sorry, I didn't get that.");
				return promptReachId();
			}
		}
		
	}
	
}

