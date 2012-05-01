package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.BasicAnalysis;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.List;

/**
 * This action assembles a List of columns with all of the individual source
 * breakdowns based on the passed PredictionContext.
 * 
 * The list of returned columns is as follows:
 * 
 * column 0  :  The predict result for the specified context for source 1
 * Column 1  :  The predict result for the specified context for source 2
 * Column 2  :  etc...
 * Column last  :  The predict result for the specified context for all sources
 * 
 * Not that not all DataSeries support sources, which will result in a validation
 * error for this action.  Also, the specified Analysis must have a null source
 * specified, not a specific source.  The source could be ignored, however,
 * since this data may be cached with the context as a key, we want to avoid
 * multiple keys for the same set of data.
 * 
 * @author eeverman
 *
 */
public class BuildAnalysisForAllSources extends Action<List<ColumnData>> {

	PredictionContext context;


	
	//Self loaded data
	PredictData predictData;

	
	public BuildAnalysisForAllSources() {
	}
		
	
	public BuildAnalysisForAllSources(PredictionContext context) {
		this.context = context;
	}
	
	
	
	/**
	 * Clear designation of init values
	 */
	protected void initRequiredFields() {
		Long modelId = context.getModelID();
		predictData = SharedApplication.getInstance().getPredictData(modelId);
	}
	
	@Override
	public ArrayList<ColumnData> doAction() throws Exception {
		
		initRequiredFields();

		DataTable srcMetadata = predictData.getSrcMetadata();
		int srcCount = srcMetadata.getRowCount();
		ArrayList<ColumnData> columns = new ArrayList<ColumnData>();
		
		//Add one column for each source
		for (int srcIndex = 0; srcIndex < srcCount; srcIndex++) {
			Long srcId = srcMetadata.getIdForRow(srcIndex);
			BasicAnalysis analysis = new BasicAnalysis(
					context.getAnalysis().getDataSeries(), srcId.intValue(),
					context.getAnalysis().getGroupBy(), context.getAnalysis().getAggFunction());
			
			PredictionContext pc = new PredictionContext(context.getModelID(),
							context.getAdjustmentGroups(), analysis,
							context.getTerminalReaches(), context.getAreaOfInterest(), context.getComparison());
			
			SparrowColumnSpecifier scs = SharedApplication.getInstance().getAnalysisResult(pc);
			columns.add(scs.getColumnData());
		}
		
		//Add one column for the combined (total) sources
		BasicAnalysis analysis = new BasicAnalysis(
					context.getAnalysis().getDataSeries(), null, null, null);
		
		PredictionContext pc = new PredictionContext(context.getModelID(),
					context.getAdjustmentGroups(), analysis,
					context.getTerminalReaches(), context.getAreaOfInterest(), context.getComparison());
		SparrowColumnSpecifier scs = SharedApplication.getInstance().getAnalysisResult(pc);
		columns.add(scs.getColumnData());
		
		return columns;
	}




	//
	//Setter methods
	public void setContext(PredictionContext context) {
		this.context = context;
	}
	
	
	//Action override methods

	
	@Override
	protected void validate() {
		
		if (context == null) {
			this.addValidationError("The context cannot be null");
			return;
		}
		
		if (context.getAnalysis() == null) {
			this.addValidationError("The context must have a non-null Analysis.");
			return;
		}
		
		if (context.getAnalysis().getDataSeries() == null) {
			this.addValidationError("The context must have an Analysis with a non-null DataSeries.");
			return;
		}
		
		if (! context.getAnalysis().getDataSeries().isSourceAllowed()) {
			this.addValidationError(
							"The DataSeries '" + context.getAnalysis().getDataSeries() +
							"' does not support individual source reporting.");
		}
		
		if (! (context.getAnalysis().getSource() == null)) {
			this.addValidationError(
							"The context must have an Analysis with a 'null' source specified.");
		}


	}
	
}
