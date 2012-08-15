package gov.usgswim.sparrow.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardLongColumnData;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.action.CalcAnalysis;
import gov.usgswim.sparrow.action.LoadModelMetadata;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.domain.reacharearelation.AreaRelation;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.domain.reacharearelation.ReachAreaRelations;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.extras.DOMConfigurator;

/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class SparrowModelWaterShedAreaValidation extends SparrowModelValidationBase {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .1D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public ModelValidationResult testModel(Long modelId) throws Exception {
		ModelValidationResult result = new ModelValidationResult();
		
		DataTable cumulativeAreas = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.REACH, true));
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(modelId, AggregationLevel.HUC2));
		
		//All the HUC2s in this model, with the HUC id as the row ID.
		DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, HucLevel.HUC2));
		
		for (int row = 0; row < predictData.getTopo().getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			ReachAreaRelations huc2sForReach = reachToHuc2Relation.getRelationsForReachRow(row);
			
			List<AreaRelation> reachHuc2Relations = huc2sForReach.getRelations();
			
			if (reachHuc2Relations.isEmpty()) {
				recordRowError(modelId, reachId, row, "Reach is not in any HUC2s.");
			} else if (reachHuc2Relations.size() > 1) {
				recordRowError(modelId, reachId, row, "Reach is in multiple HUC2s - Expected only 1.");
			} else {
				List<Long> targetReachIDs = new ArrayList<Long>(1);
				targetReachIDs.add(reachId);
				
				
				DataTable totalDelAggReport = SharedApplication.getInstance().getTotalDeliveredLoadByUpstreamRegionReport(
					new DeliveryReportRequest(new AdjustmentGroups(modelId), new TerminalReaches(modelId, targetReachIDs), AggregationLevel.HUC2));
				
				
				Long regionId = reachHuc2Relations.get(0).getAreaId();
				int regionRow = regionDetail.getRowForId(regionId);
				Double aggArea = totalDelAggReport.getDouble(regionRow, 2);
				Double dbArea = cumulativeAreas.getDouble(row, 1);
				
				if (! comp(dbArea, aggArea, allowedFractialVariance)) {
					recordRowError(modelId, reachId, row, "Agg catchment area != db watershed area (agg / db) : " + aggArea + "  /  " + dbArea);
				}
			}
			

		}
		
		
		if (this.getIndividualFailures() > 0) result.modelsFailed = 1;
		result.modelsRun = 1;
		
		
		return result;
	}
	
	
}

