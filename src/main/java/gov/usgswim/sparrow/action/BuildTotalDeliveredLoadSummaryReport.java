package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.HashMapColumnIndex;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
public class BuildTotalDeliveredLoadSummaryReport extends Action<DataTable> {

	protected AdjustmentGroups adjustmentGroups;
	protected TerminalReaches terminalReaches;
	
	//Loaded by the action itself
	Long modelId = null;
	protected transient PredictData predictData = null;
	
	protected String msg = null;	//statefull message for logging
	
	
	/**
	 * Clear designation of init values
	 */
	protected void initRequiredFields() {
		modelId = adjustmentGroups.getModelID();
		predictData = SharedApplication.getInstance().getPredictData(modelId);
	}
	
	@Override
	public DataTable doAction() throws Exception {
		
		initRequiredFields();

		DataTable srcMetadata = predictData.getSrcMetadata();
		int srcCount = srcMetadata.getRowCount();
		ArrayList<ColumnData> columns = new ArrayList<ColumnData>();
		
		//Add one column for each source
		for (int srcIndex = 0; srcIndex < srcCount; srcIndex++) {
			Long srcId = srcMetadata.getIdForRow(srcIndex);
			BasicAnalysis analysis = new BasicAnalysis(
					DataSeriesType.total_delivered_flux, srcId.intValue(),
					null, null);
			
			PredictionContext pc = new PredictionContext(modelId, adjustmentGroups, analysis,
					terminalReaches, null, NoComparison.NO_COMPARISON);
			
			SparrowColumnSpecifier scs = SharedApplication.getInstance().getAnalysisResult(pc);
			columns.add(scs.getColumnData());
		}
		
		//Add one column for the combined (total) sources
		BasicAnalysis analysis = new BasicAnalysis(
				DataSeriesType.total_delivered_flux, null, null, null);
		
		PredictionContext pc = new PredictionContext(modelId, adjustmentGroups, analysis,
				terminalReaches, null, NoComparison.NO_COMPARISON);
		SparrowColumnSpecifier scs = SharedApplication.getInstance().getAnalysisResult(pc);
		columns.add(scs.getColumnData());
		HashMapColumnIndex index = new  HashMapColumnIndex(predictData.getTopo());
		
		SimpleDataTable result = new SimpleDataTable(
				columns.toArray(new ColumnData[]{}),
				"Total Delivered Load Summary Report", 
				"Total Delivered Load for each source individualy and for all sources",
				buildTableProperties(), index);
		
		if (result.isValid()) {
			return result;
		} else {
			msg = "The resulting table was invalid";
			return null;
		}
		
	}
	
	protected Map<String, String> buildTableProperties() {
		HashMap<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.MODEL_ID.toString(), modelId.toString());
		props.put(TableProperties.ROW_LEVEL.toString(), UnitAreaType.HUC_REACH.toString());
		props.put(TableProperties.CONSTITUENT.toString(), predictData.getModel().getConstituent());
		return props;
	}


	//
	//Setter methods
	public void setAdjustmentGroups(AdjustmentGroups adjustmentGroups) {
		this.adjustmentGroups = adjustmentGroups;
	}

	public void setTerminalReaches(TerminalReaches terminalReaches) {
		this.terminalReaches = terminalReaches;
	}
	
	public void setDeliveryReportRequest(DeliveryReportRequest request) {
		terminalReaches = request.getTerminalReaches();
		adjustmentGroups = request.getAdjustmentGroups();
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
	
}
