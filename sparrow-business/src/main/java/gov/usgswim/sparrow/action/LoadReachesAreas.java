/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.IncrementalAreaColumnDataWritable;
import gov.usgswim.sparrow.datatable.ModelReachAreaDataTable;
import gov.usgswim.sparrow.datatable.TotalContributingAreaColumnDataWritable;
import gov.usgswim.sparrow.datatable.TotalUpstreamAreaColumnDataWritable;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
/**
 * Given full identifiers and model id, this action retrieves the areas associated
 * with each reach.
 * @author cschroed
 */
@Data public class LoadReachesAreas extends Action<ModelReachAreaDataTable>{
	private final String QUERY_NAME = "areaAttributesQuery";
	private final String IDENTIFIERS_PARAM_NAME = "reachIds";
	private final String RESULT_ID_NAME = "identifier";
	private final String RESULT_INCREMENTAL_AREA_NAME = "incrementalArea";
	private final String RESULT_CONTRIBUTING_AREA_NAME = "totalContributingArea";
	private final String RESULT_UPSTREAM_AREA_NAME = "totalUpstreamArea";

	private final TerminalReaches terminalReaches;
	private final PredictData predictData;
	private final int rowCount;

	@Override
	public ModelReachAreaDataTable doAction() throws Exception {
		ModelReachAreaDataTable areaTable = null;
		//extract the system ids from the terminal reaches
		List<Long> reachIds = SharedApplication.getInstance().getReachFullIdAsLong(
			terminalReaches.getModelID(), terminalReaches.getReachIdsAsList()
			);
		HashMap<String, Object> params = new HashMap<String, Object>(1);
		params.put(IDENTIFIERS_PARAM_NAME, reachIds);
		PreparedStatement statement = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
		ResultSet queryResults = statement.executeQuery();
		addResultSetForAutoClose(queryResults);
		
		TotalUpstreamAreaColumnDataWritable totalUpstreamArea = new TotalUpstreamAreaColumnDataWritable(rowCount);
		TotalContributingAreaColumnDataWritable totalContributingArea = new TotalContributingAreaColumnDataWritable(rowCount);
		IncrementalAreaColumnDataWritable incrementalArea = new IncrementalAreaColumnDataWritable(rowCount);
		while(queryResults.next()){
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

		areaTable = new ModelReachAreaDataTable(totalUpstreamArea, totalContributingArea, incrementalArea);
		return areaTable;
//		HashMap<String, ReachAreas> methodResults = new HashMap<String, ReachAreas>(reachFullIds.size()); //Maps Reach Ids to Areas
//		Map<String, Object> params = new HashMap<String, Object>(1);
//		params.put(FULL_IDENTIFIER_PARAM_NAME, reachFullIds);
//		params.put(MODEL_ID_PARAM_NAME, modelId);
//		PreparedStatement statement = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
//		ResultSet queryResults = statement.executeQuery();
//		addResultSetForAutoClose(queryResults);
//
//		ReachAreas reachAreas;
//		while(queryResults.next()){
//			reachAreas = new ReachAreas(
//				queryResults.getDouble(totalContributingAreaIndex),
//				queryResults.getDouble(totalUpstreamAreaIndex),
//				queryResults.getDouble(incrementalAreaIndex)
//				);
//			methodResults.put(queryResults.getString(fullIdIndex), reachAreas);
//		}
//		return methodResults;
	}

}
