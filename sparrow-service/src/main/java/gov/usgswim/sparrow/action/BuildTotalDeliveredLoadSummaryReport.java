package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.*;
import gov.usgswim.datatable.impl.DataTableSetSimple;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.view.RenameColumnDataView;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This action assembles a DataTable of Total Delivered Load by source and for
 * all sources.  The returned DataTable has this structure:
 * 
 * Row ID    :  Reach ID (actual id for the row, not a column)
 * column 0  :  Reach Name
 * Column 1  :  EDA Code
 * Column 2  :  Source 0 Total Delivered Load
 * Column 3  :  Source 1 Total Delivered Load
 * ...etc. for all sources
 * Col. Last :  Total Delivered Load for all sources
 * 
 * @author eeverman
 *
 */
public class BuildTotalDeliveredLoadSummaryReport extends Action<DataTableSet> {

	protected AdjustmentGroups adjustmentGroups;
	protected TerminalReaches terminalReaches;
	protected boolean reportYield;
	
	//Loaded or created by the action itself
	Long modelId = null;
	private transient PredictData predictData = null;
	private transient DataTable idInfo = null;
	private transient DataTable terminalReachDrainageArea = null;
	private transient List<ColumnData> expandedTotalDelLoadForAllSources;
	private transient SparrowColumnSpecifier streamFlow = null;
	private transient ColumnData fractionedWatershedArea = null;
	
	
	protected String msg = null;	//statefull message for logging
	
	public BuildTotalDeliveredLoadSummaryReport(AdjustmentGroups adjustmentGroups, TerminalReaches terminalReaches, boolean reportYield) {
		this.adjustmentGroups = adjustmentGroups;
		this.terminalReaches = terminalReaches;
		this.reportYield = reportYield;
	}
	
	public BuildTotalDeliveredLoadSummaryReport(DeliveryReportRequest request) {
		terminalReaches = request.getTerminalReaches();
		adjustmentGroups = request.getAdjustmentGroups();
		this.reportYield = request.isReportYield();
	}
	
	@Override
	protected void initFields() throws Exception {
		SharedApplication sharedApp = SharedApplication.getInstance();
		
		modelId = adjustmentGroups.getModelID();
		predictData = sharedApp.getPredictData(modelId);
		idInfo = sharedApp.getModelReachIdentificationAttributes(modelId);
		terminalReachDrainageArea = sharedApp.getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.NONE, true));
		fractionedWatershedArea = sharedApp.getFractionedWatershedAreaTable(terminalReaches.getId());
		streamFlow = sharedApp.getStreamFlow(modelId);
		
		//Basic predict context, which we need data for all sources
		BasicAnalysis analysis = new BasicAnalysis(
				DataSeriesType.total_delivered_flux, null, null, null);
			
		PredictionContext basicPredictContext = new PredictionContext(
				modelId, adjustmentGroups, analysis, terminalReaches,
				null, NoComparison.NO_COMPARISON);
		
		BuildAnalysisForAllSources action =
				new BuildAnalysisForAllSources(basicPredictContext, 
				BuildAnalysisForAllSources.COLUMN_NAME_FORMAT.SOURCE_NAME_ONLY);
		
		
		expandedTotalDelLoadForAllSources = action.run();
	}
	
	@Override
	public DataTableSet doAction() throws Exception {

		DataTable srcMetadata = predictData.getSrcMetadata();
		//int srcCount = srcMetadata.getRowCount();
		
		BasicAnalysis analysis = new BasicAnalysis(
				DataSeriesType.total_delivered_flux, null, null, null);
			
		ColumnIndex index = predictData.getTopo().toImmutable().getIndex();
		
		//Rename the area db area column
		ColumnAttribsBuilder termReachWatershedAreaColumnAttribs = new ColumnAttribsBuilder();
		termReachWatershedAreaColumnAttribs.setName(terminalReachDrainageArea.getColumn(1).getName() + " (from Model export)");
		termReachWatershedAreaColumnAttribs.setDescription(terminalReachDrainageArea.getColumn(1).getDescription());
		RenameColumnDataView termReachWatershedAreaColumn = new RenameColumnDataView(terminalReachDrainageArea.getColumn(1), termReachWatershedAreaColumnAttribs);
		
				//We can't add immutable columns to a writable table, so we need to construct a new table
		SimpleDataTable infoTable = new SimpleDataTable(
				new ColumnData[] {idInfo.getColumn(0), idInfo.getColumn(1), termReachWatershedAreaColumn, fractionedWatershedArea, streamFlow.getColumnData()},
				"Identification and basic info", 
				"Identification and basic info",
				buildTableProperties(), index);
	
		
		SimpleDataTable loadTable = new SimpleDataTable(
				expandedTotalDelLoadForAllSources.toArray(new ColumnData[]{}),
				"Total Delivered Load by Source", 
				"Total Delivered Load for each source individualy and for all sources",
				buildTableProperties(), index);
		
		String reportName = "Load";
		if (this.reportYield) {
			loadTable = convertToYield(predictData.getModel(), loadTable, fractionedWatershedArea, index);
			reportName = "Yield";
		}
		
		DataTableSet tableSet = new DataTableSetSimple(new DataTable.Immutable[]{infoTable.toImmutable(), loadTable.toImmutable()},
				"Total Delivered " + reportName + " Summary Report",
				"Total Delivered " + reportName + " for each source individualy and for all sources");
		
		if (tableSet.isValid()) {
			return tableSet;
		} else {
			msg = "The resulting table was invalid";
			return null;
		}
		
	}
	
	protected Map<String, String> buildTableProperties() {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.MODEL_ID.toString(), modelId.toString());
		props.put(TableProperties.ROW_LEVEL.toString(), AggregationLevel.REACH.toString());
		props.put(TableProperties.CONSTITUENT.toString(), predictData.getModel().getConstituent());
		return props;
	}
	
	//Action override methods
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	protected void validate() {
		
		if (adjustmentGroups == null || terminalReaches == null) {
			addValidationError("Both the adjustmentGroups and terminalReaches must be non-null.");
			return;	//Don't check beyond a null value
		}

		//
		//
		
		if (terminalReaches.isEmpty()) {
			addValidationError("the terminalReaches must be non-empty to do delivery analysis.");
		}
		
		if (adjustmentGroups.getModelID() != terminalReaches.getModelID()) {
			addValidationError("The model IDs of the adjustmentGroups and the terminalReaches must be the same.");
		}


	}
	
	protected SimpleDataTable convertToYield(SparrowModel model, DataTable loadTable,
			ColumnData areaCol, ColumnIndex index) throws Exception {
		
		ArrayList<ColumnData> yieldCols = new ArrayList<ColumnData>();
		
		for (int col = 0; col < loadTable.getColumnCount(); col++) {
			ColumnData loadColumn = loadTable.getColumn(col);
			
			CalcAnyYield action = new CalcAnyYield(DataSeriesType.total_yield, model, loadColumn, areaCol);
			ColumnData yieldCol = action.run();
			yieldCols.add(yieldCol);
		}
		
		SimpleDataTable yieldTable = new SimpleDataTable(
				yieldCols.toArray(new ColumnData[]{}),
				"Total Delivered Yield by Source", 
				"Total Delivered Yield for each source individualy and for all sources",
				buildTableProperties(), index);
		
		return yieldTable;
	}
	
}
