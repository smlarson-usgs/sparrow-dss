/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.PredictData;
import static gov.usgswim.sparrow.action.Action.splitListIntoSubLists;
import gov.usgswim.sparrow.datatable.IncrementalAreaColumnDataWritable;
import gov.usgswim.sparrow.datatable.ModelReachAreaDataTable;
import gov.usgswim.sparrow.datatable.TotalContributingAreaColumnDataWritable;
import gov.usgswim.sparrow.datatable.TotalUpstreamAreaColumnDataWritable;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Given full identifiers and model id, this action retrieves the areas associated
 * with each reach.
 * @author cschroed
 */
public class LoadReachesAreas extends Action<ModelReachAreaDataTable>{
	private final String QUERY_NAME = "areaAttributesQuery";
	private final String IDENTIFIERS_PARAM_NAME = "reachIds";
	private final String MODEL_ID_PARAM_NAME = "ModelId";
	private final String RESULT_ID_NAME = "identifier";
	private final String RESULT_INCREMENTAL_AREA_NAME = "incrementalArea";
	private final String RESULT_CONTRIBUTING_AREA_NAME = "totalContributingArea";
	private final String RESULT_UPSTREAM_AREA_NAME = "totalUpstreamArea";

	
	private final PredictData predictData;
	
	
	//These two are either-or, but terminalReaches will create a list of reachIds
	//if not provided.
	private final TerminalReaches terminalReaches;
	private List<Long> reachIds;
	
	//Self-created list of lists of 1K max reach ID
	//This deails w/ the oracle IN clause limitation of 1K params
	List<List<Long>> reachIdSets;
	
	private int numberOfReachesLoaded = 0;	//status tracking and final count check
	
	//Self-populated
	private int rowsInModel;
	
	LoadReachesAreas(TerminalReaches terminalReaches, PredictData predictData) {
		this.terminalReaches = terminalReaches;
		this.predictData = predictData;
	}
	
	LoadReachesAreas(List<Long> reachIds, PredictData predictData) {
		this.terminalReaches = null;	//not needed b/c we have a set of reachIds
		this.reachIds = reachIds;
		this.predictData = predictData;
	}

	@Override
	protected void validate() {
		if (predictData == null) {
			addValidationError("The predict data cannot be null");
		}
		
		if (terminalReaches != null) {
			if (terminalReaches.isEmpty())
				addValidationError("The TerminalReaches collection cannot be empty");
		} else if (reachIds != null) {
			if (reachIds.isEmpty())
				addValidationError("The reachIds list cannot be empty");
		} else {
			addValidationError("Either the TerminalReaches or reachIds must be non-null");
		}
	}

	@Override
	protected void initFields() throws Exception {
		
		if (reachIds == null) {
			reachIds = SharedApplication.getInstance().getReachFullIdAsLong(
				terminalReaches.getModelID(), terminalReaches.getReachIdsAsList()
			);
		}
		
		rowsInModel = predictData.getTopo().getRowCount();
		
		reachIdSets = new ArrayList<List<Long>>();
		splitListIntoSubLists(reachIdSets, reachIds, 1000);
	}
	
	

	@Override
	public ModelReachAreaDataTable doAction() throws Exception {
		ModelReachAreaDataTable areaTable = null;

		TotalUpstreamAreaColumnDataWritable totalUpstreamArea = new TotalUpstreamAreaColumnDataWritable(rowsInModel);
		TotalContributingAreaColumnDataWritable totalContributingArea = new TotalContributingAreaColumnDataWritable(rowsInModel);
		IncrementalAreaColumnDataWritable incrementalArea = new IncrementalAreaColumnDataWritable(rowsInModel);
		
		for (List<Long> idList : reachIdSets) {
			fetchFromDb(predictData.getModel().getId(),
					idList, totalUpstreamArea, totalContributingArea, incrementalArea);
		}

		
		//Verify that the number of returned reaches matches the number of
		//requested reach IDs.
		//NOTE:  We don't check for duplicate IDs.
		if (numberOfReachesLoaded != reachIds.size()) {
			this.setPostMessage(
					"Some reach ids were not found in model " + predictData.getModel().getId() +
					".  Expected to find " + reachIds.size() + " reaches by ID, but only found " + numberOfReachesLoaded);
			areaTable = null;
		} else {
			areaTable = new ModelReachAreaDataTable(totalUpstreamArea, totalContributingArea, incrementalArea);
		}
		
		
		return areaTable;
	}
	
	public void fetchFromDb(
			Long modelId, List<Long> reachIds,
			TotalUpstreamAreaColumnDataWritable totalUpstreamArea,
			TotalContributingAreaColumnDataWritable totalContributingArea,
			IncrementalAreaColumnDataWritable incrementalArea) throws Exception {
		
		ModelReachAreaDataTable areaTable = null;
		//extract the system ids from the terminal reaches

		HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put(MODEL_ID_PARAM_NAME, predictData.getModel().getId());
		params.put(IDENTIFIERS_PARAM_NAME, reachIds);
		
		
		PreparedStatement statement = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
		ResultSet queryResults = statement.executeQuery();
		addResultSetForAutoClose(queryResults);


		while(queryResults.next()){
			
			numberOfReachesLoaded++;
			
			Long reachId = queryResults.getLong(RESULT_ID_NAME);
			int row = predictData.getRowForReachID(reachId);
			totalUpstreamArea.setValue(
				queryResults.getDouble(RESULT_UPSTREAM_AREA_NAME),
				row);
			totalContributingArea.setValue(
				queryResults.getDouble(RESULT_CONTRIBUTING_AREA_NAME),
				row);
			incrementalArea.setValue(
				queryResults.getDouble(RESULT_INCREMENTAL_AREA_NAME),
				row);
		}
		
		//Preemptive, otherwise we wait for the entire action to finish
		queryResults.close();

	}
	


}
