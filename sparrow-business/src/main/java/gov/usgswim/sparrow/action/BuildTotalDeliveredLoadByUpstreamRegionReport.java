package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.*;
import gov.usgs.cida.datatable.impl.DataTableSetSimple;
import gov.usgs.cida.datatable.impl.SimpleDataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.domain.reacharearelation.AreaRelation;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.domain.reacharearelation.ReachAreaRelations;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This action assembles a DataTableSet of Total Delivered Load by source and for
 * all sources.  The returned DataTableSet has this structure:
 * 
 * (No row ID)
 * <h4>Table 0 : Identity Table</h4>
 * Column 0  :  Region Name
 * Column 1  :  Region ID / Code
 * Column 2  :  Region Area, made up of the individual upstream catchment areas (not the total region area).
 * 
 * <h4>Table 1 : Total Delivered Load Summary Report per originating upstream region</h4>
 * Column 0  :  Source 0 Total Delivered Load
 * Column 1  :  Source 1 Total Delivered Load
 * ...etc. for all sources
 * Col. Last :  Total Delivered Load for all sources
 * 
 * @author eeverman
 *
 */
public class BuildTotalDeliveredLoadByUpstreamRegionReport extends Action<DataTableSet> {
	
	//Assigned Values
	protected AdjustmentGroups adjustmentGroups;
	protected TerminalReaches terminalReaches;
	protected AggregationLevel aggLevel;
	protected boolean reportYield;

	
	//Generated / self-loaded values
	protected SparrowModel sparrowModel;
	protected ModelReachAreaRelations areaRelations;
	protected ReachRowValueMap deliveryFractionMap;	//What reaches deliver to the target reaches?
	protected DataTable areaDetail;
	protected DataTable reachCatchmentAreas;
	List<ColumnData> expandedTotalDelLoadForAllSources;
	ColumnData contributingFractionAreaForRegions;
	
	public BuildTotalDeliveredLoadByUpstreamRegionReport(
			AdjustmentGroups adjustmentGroups,
			TerminalReaches terminalReaches,
			AggregationLevel aggLevel,
			boolean reportYield) {
		
		this.adjustmentGroups = adjustmentGroups;
		this.terminalReaches = terminalReaches;
		this.aggLevel = aggLevel;
		this.reportYield = reportYield;
		
	}
	
	public BuildTotalDeliveredLoadByUpstreamRegionReport(DeliveryReportRequest request) {
		
		terminalReaches = request.getTerminalReaches();
		adjustmentGroups = request.getAdjustmentGroups();
		aggLevel = request.getAggLevel();
		this.reportYield = request.isReportYield();
		
	}
	
	/**
	 * Clear designation of init values
	 */
	@Override
	protected void initFields() throws Exception {
		
		Long modelId = adjustmentGroups.getModelID();
		sparrowModel = SharedApplication.getInstance().getPredictData(modelId).getModel();
		
		UnitAreaRequest unitAreaRequest = new UnitAreaRequest(modelId, AggregationLevel.REACH, false);
		reachCatchmentAreas = SharedApplication.getInstance().getCatchmentAreas(unitAreaRequest);
		
		deliveryFractionMap = SharedApplication.getInstance().getDeliveryFractionMap(terminalReaches);
		
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
		
		if (expandedTotalDelLoadForAllSources == null) {
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
		
		if (contributingFractionAreaForRegions == null) {
			CalcContributingFractionedAreaForRegions action = new CalcContributingFractionedAreaForRegions(
					terminalReaches, aggLevel);
			contributingFractionAreaForRegions = action.run();
		}
	}
	
	@Override
	public DataTableSet doAction() throws Exception {

		//
		//Create a writable table to hold individual source and total load values
		SimpleDataTableWritable srcAndTotalTable = new SimpleDataTableWritable();
		srcAndTotalTable.setName("Total Delivered Load per originating upstream region");
		srcAndTotalTable.setDescription("Total Delivered Load per originating upstream region.");
		assignTableProperties(srcAndTotalTable, BaseDataSeriesType.total);
		
		//Add one column for each source and one for the total
		//(these are the column in the expanded TotalDelLoadForAllSources columns)
		for (int colIndex = 0; colIndex < expandedTotalDelLoadForAllSources.size(); colIndex++) {
			
			String colName = expandedTotalDelLoadForAllSources.get(colIndex).getName();
					
			StandardNumberColumnDataWritable col =
							new StandardNumberColumnDataWritable(colName,
							expandedTotalDelLoadForAllSources.get(colIndex).getUnits(),
							Double.class);
			
			//Ensure that the column is populated w/ as many rows as the info table
			//by setting a value for the last row.
			col.setValue(0D, areaDetail.getRowCount() - 1);
			
			srcAndTotalTable.addColumn(col);
		}
		


		populateColumns(srcAndTotalTable, expandedTotalDelLoadForAllSources, areaRelations, areaDetail, reachCatchmentAreas.getColumn(1));
		
		
		//We can't add immutable columns to a writable table, so we need to construct a new table
		SimpleDataTable idTable = new SimpleDataTable(
				new ColumnData[] {areaDetail.getColumn(0), areaDetail.getColumn(1), contributingFractionAreaForRegions},
				"Identification and basic info", 
				"Identification and basic info",
				buildTableProperties(null));
		
		DataTable.Immutable yieldTable = convertToYield(sparrowModel, srcAndTotalTable, contributingFractionAreaForRegions);

		
		
		DataTableSet tableSet = new DataTableSetSimple(new DataTable.Immutable[]{idTable, srcAndTotalTable.toImmutable(), yieldTable},
				"Total Delivered Load Summary Report per upstream region",
				"Total Delivered Load Summary Report per originating upstream region.");
		
		return tableSet;
		
	}
	
	/**
	 * Sets model properties on the passed and/or returns a set of basic properties.
	 * @param table may be just to just get the basic props.
	 * @return 
	 */
	protected void assignTableProperties(DataTableWritable table, BaseDataSeriesType type) {
		Map<String, String> props = buildTableProperties(type);

		if (table != null) {
			for (Map.Entry<String, String> entry : props.entrySet()) {
				table.setProperty(entry.getKey(), entry.getValue());
			}
		}
	}
	
	protected Map<String, String> buildTableProperties(BaseDataSeriesType type) {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.MODEL_ID.toString(), sparrowModel.getId().toString());
		props.put(TableProperties.ROW_LEVEL.toString(), aggLevel.toString());
		props.put(TableProperties.CONSTITUENT.toString(), sparrowModel.getConstituent());
		
		if (type != null) {
			props.put(TableProperties.DATA_TYPE.toString(), type.toString());
		}
		return props;
	}
	
	/**
	 * 
	 * @param regionResultTable Table to fill
	 * @param sourceColumns Total Delivered Load for all source and the total del load
	 * @param areaRelations reach to state (or other region) relation w/ fraction in each region
	 * @param areaDetail List of all regions (ie areaDetail) that this model touches (all regions for areaRelations)
	 * @throws Exception 
	 */
	protected void populateColumns(SimpleDataTableWritable regionResultTable,
					/*SimpleDataTableWritable regionAreaTable,*/
					List<ColumnData> sourceColumns,
					ModelReachAreaRelations areaRelations, DataTable areaDetail, ColumnData catchmentAreas) throws Exception {
		
		
		int reachRowCount = sourceColumns.get(0).getRowCount();
		int colCount = sourceColumns.size();	//number of report columns, equal to the number of sources plus 1.
		
		//Loop thru all the reaches in the Total Delivered Load
		for (int reachRow = 0; reachRow < reachRowCount; reachRow++) {
			
			if (deliveryFractionMap.containsKey(reachRow)) {
				ReachAreaRelations reachRelations = areaRelations.getRelationsForReachRow(reachRow);
				//Double catchmentArea = catchmentAreas.getDouble(reachRow);

				//Loop thru each region that this reach has catchment area in
				for (AreaRelation reachRelation : reachRelations.getRelations()) {
					long regionId = reachRelation.getAreaId();
					double reachFractionInRegion = reachRelation.getFraction();
					int regionRow = areaDetail.getRowForId(regionId);

					//Loop thru each source and the total.  Col 0 is the area.  First source is col 1.
					for (int sourceColIndex = 0; sourceColIndex < colCount; sourceColIndex++) {

						Double existingRegionAggVal = regionResultTable.getDouble(regionRow, sourceColIndex);

						//Do we already have a value for this cell?
						if (existingRegionAggVal == null) existingRegionAggVal = 0d;

						double reachValue =
										sourceColumns.get(sourceColIndex).getDouble(reachRow);
						double reachValuePortionFromRegion = reachValue * reachFractionInRegion;

						regionResultTable.setValue(reachValuePortionFromRegion + existingRegionAggVal, regionRow, sourceColIndex);

					}

				}

			}
		}
	}
	
	protected SimpleDataTable convertToYield(SparrowModel model, DataTable loadTable,
			ColumnData areaCol) throws Exception {
		
		ArrayList<ColumnData> yieldCols = new ArrayList<ColumnData>();
		
		for (int col = 0; col < loadTable.getColumnCount(); col++) {
			ColumnData loadColumn = loadTable.getColumn(col);
			
			CalcAnyYield action = new CalcAnyYield(DataSeriesType.total_yield, model, loadColumn, areaCol);
			ColumnData yieldCol = action.run();
			yieldCols.add(yieldCol);
		}
		
		SimpleDataTable yieldTable = new SimpleDataTable(
				yieldCols.toArray(new ColumnData[]{}),
				"Total Delivered Yield per originating upstream region", 
				"Total Delivered Yield per originating upstream region",
				buildTableProperties(BaseDataSeriesType.yield));
		
		return yieldTable;
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
