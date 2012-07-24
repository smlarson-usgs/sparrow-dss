package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.datatable.view.RelativePercentageView;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.domain.reacharearelation.AreaRelation;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.domain.reacharearelation.ReachAreaRelations;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.*;

/**
 * This action assembles a DataTable of Total Delivered Load by source and for
 * all sources.  The returned DataTable has this structure:
 * 
 * Row ID    :  Reach ID
 * Column 0  :  Source 0 Total Delivered Load
 * Column 1  :  Source 1 Total Delivered Load
 * ...etc. for all sources
 * Col. Last :  Total Delivered Load for all sources
 * 
 * @author eeverman
 *
 */
public class BuildTotalDeliveredLoadByUpstreamRegionReport extends Action<DataTable> {
	
	//Assigned Values
	protected AdjustmentGroups adjustmentGroups;
	protected TerminalReaches terminalReaches;
	protected AggregationLevel aggLevel;
	
	

	
	//Generated / self-loaded values
	protected SparrowModel sparrowModel;
	protected ModelReachAreaRelations areaRelations;
	protected DataTable areaDetail;
	List<ColumnData> expandedTotalDelLoadForAllSources;
	
	public BuildTotalDeliveredLoadByUpstreamRegionReport(
			AdjustmentGroups adjustmentGroups,
			TerminalReaches terminalReaches,
			AggregationLevel aggLevel) {
		
		this.adjustmentGroups = adjustmentGroups;
		this.terminalReaches = terminalReaches;
		this.aggLevel = aggLevel;
		
	}
	
		public BuildTotalDeliveredLoadByUpstreamRegionReport(DeliveryReportRequest request) {
		
		terminalReaches = request.getTerminalReaches();
		adjustmentGroups = request.getAdjustmentGroups();
		aggLevel = request.getAggLevel();
		
	}
	
	/**
	 * Clear designation of init values
	 */
	protected void initRequiredFields() throws Exception {
		
		Long modelId = adjustmentGroups.getModelID();
		sparrowModel = SharedApplication.getInstance().getPredictData(modelId).getModel();
		
		
		ModelAggregationRequest modelReachAreaRelelationsRequest = 
					new ModelAggregationRequest(modelId, aggLevel);
		areaRelations = SharedApplication.getInstance().getModelReachAreaRelations(modelReachAreaRelelationsRequest);
		
		if (aggLevel.equals(AggregationLevel.STATE)) {
			areaDetail = SharedApplication.getInstance().getStatesForModel(modelId);
		} else if (aggLevel.isHuc()) {
			areaDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, aggLevel.getHucLevel()));
		} else if (aggLevel.isEda()) {
			//areaDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, aggLevel.getHucLevel()));
		}
		
		//Basic predict context, which we need data for all sources
		BasicAnalysis analysis = new BasicAnalysis(
				DataSeriesType.incremental_delivered_flux, null, null, null);
			
		PredictionContext basicPredictContext = new PredictionContext(
				modelId, adjustmentGroups, analysis, terminalReaches,
				null, NoComparison.NO_COMPARISON);
		
		BuildAnalysisForAllSources action = 
				new BuildAnalysisForAllSources(basicPredictContext,
				BuildAnalysisForAllSources.COLUMN_NAME_FORMAT.SOURCE_NAME_ONLY);
		
		
		expandedTotalDelLoadForAllSources = action.run();
	}
	
	@Override
	public DataTable doAction() throws Exception {
		
		initRequiredFields();

		
		
		SimpleDataTableWritable srcAndTotalTable = new SimpleDataTableWritable();
		
		//Add one column for each source and one for the total
		//(these are the column in the expanded TotalDelLoadForAllSources columns)
		for (int colIndex = 0; colIndex < expandedTotalDelLoadForAllSources.size(); colIndex++) {
			
			String colName = expandedTotalDelLoadForAllSources.get(colIndex).getName();
					
			StandardNumberColumnDataWritable col =
							new StandardNumberColumnDataWritable(colName,
							expandedTotalDelLoadForAllSources.get(colIndex).getUnits(),
							Double.class);
			
			srcAndTotalTable.addColumn(col);
		}

		populateColumns(srcAndTotalTable, expandedTotalDelLoadForAllSources, areaRelations, areaDetail);
		
		//Add the reach identification columns
		ArrayList<ColumnData> columns = new ArrayList<ColumnData>();
		columns.add(0, areaDetail.getColumn(0));	//region name
		columns.add(1, areaDetail.getColumn(1));	//region code
		columns.addAll(Arrays.asList(srcAndTotalTable.getColumns()));
		
		
		SimpleDataTable result = new SimpleDataTable(
				columns.toArray(new ColumnData[]{}),
				"Total Delivered Load Summary Report per State", 
				"Total Delivered Load for each source individualy and for all States",
				buildTableProperties());
		
		RelativePercentageView view = new RelativePercentageView(
				result,
				null, null,
				result.getColumnCount() - 1,
				false
				);
		
		return view.toImmutable();
		
	}
	
	protected Map<String, String> buildTableProperties() {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.MODEL_ID.toString(), sparrowModel.getId().toString());
		props.put(TableProperties.CONSTITUENT.toString(), sparrowModel.getConstituent());
		return props;
	}
	
	/**
	 * 
	 * @param regionResultTable Table to fill
	 * @param totDelForAllSrcs Total Delivered Load for all source and the total del load
	 * @param areaRelations reach to state (or other region) relation w/ fraction in each region
	 * @param areaDetail List of all regions (ie areaDetail) that this model touches (all regions for areaRelations)
	 * @throws Exception 
	 */
	protected void populateColumns(SimpleDataTableWritable regionResultTable,
					List<ColumnData> totDelForAllSrcs,
					ModelReachAreaRelations areaRelations, DataTable areaDetail) throws Exception {
		
		
		int reachRowCount = totDelForAllSrcs.get(0).getRowCount();
		int colCount = totDelForAllSrcs.size();	//number of report columns, equal to the number of sources plus 1.
		
		//Loop thru all the reaches in the Total Delivered Load
		for (int reachRow = 0; reachRow < reachRowCount; reachRow++) {
			ReachAreaRelations reachRelations = areaRelations.getRelationsForReachRow(reachRow);
			
			//Loop thru each state that this reach has catchment area in
			for (AreaRelation reachRelation : reachRelations.getRelations()) {
				long regionId = reachRelation.getAreaId();
				double reachFractionInRegion = reachRelation.getFraction();
				int regionRow = areaDetail.getRowForId(regionId);
				
				//Loop thru each source and the total
				for (int colIdx = 0; colIdx < colCount; colIdx++) {
					
					Double existingRegionAggVal = regionResultTable.getDouble(regionRow, colIdx);
					
					//Do we already have a value for this cell?
					if (existingRegionAggVal == null) existingRegionAggVal = 0d;
					
					double reachValue =
									totDelForAllSrcs.get(colIdx).getDouble(reachRow);
					double reachValuePortionFromRegion = reachValue * reachFractionInRegion;
					
					regionResultTable.setValue(reachValuePortionFromRegion + existingRegionAggVal, regionRow, colIdx);

				}
				
			}
	
		}
	
		//We cant do this right now because there is no easy way to convert
		//The immutable state name columns into mutable ones so we can add
		//a row name and make the columns the same length.
//		//Add total row for all sources
//		for (int colIdx = 0; colIdx < colCount; colIdx++) {
//			Double colTotal = DataTableUtils.getColumnTotal(regionResultTable, colIdx);
//			regionResultTable.setValue(colTotal, reachRowCount, colIdx);
//		}
		
	}
	
	@Override
	protected void validate() {
		if (adjustmentGroups == null) {
			this.addValidationError("The adjustment groups parameter cannot be null");
			return;
		}
				
		if (terminalReaches == null) {
			this.addValidationError("The terminal reaches parameter cannot be null");
		}
		
		if (aggLevel == null) {
			this.addValidationError("The aggregate level reaches parameter cannot be null");
		}
		
		if (! (aggLevel.isHuc() || aggLevel.isPolitical())) {
			this.addValidationError("The aggregation level (the level at which to load the area relations for) must be either a HUC or a political region.");
		}
	}
	
}
