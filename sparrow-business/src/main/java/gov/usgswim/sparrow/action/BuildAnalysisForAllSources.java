package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnAttribs;
import gov.usgs.cida.datatable.ColumnAttribsBuilder;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.view.RenameColumnDataView;
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

	public static enum COLUMN_NAME_FORMAT {
		DATA_SERIES_ONLY,
		SOURCE_NAME_ONLY,
		DATA_SERIES_AND_SOURCE
	};
	
	PredictionContext context;
	COLUMN_NAME_FORMAT columnNameFormat;

	
	//Self loaded data
	PredictData predictData;
		
	
	public BuildAnalysisForAllSources(PredictionContext context, COLUMN_NAME_FORMAT columnNameFormat) {
		this.context = context;
		this.columnNameFormat = columnNameFormat;
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
		
		if (columnNameFormat.equals(COLUMN_NAME_FORMAT.DATA_SERIES_ONLY)) {
			//Do nothing - the columns are normally named w/ the data series name only
		} else {
			
			String dataSeriesName = context.getAnalysis().getDataSeries().name();
			
			for (int c=0; c<columns.size(); c++) {
				
				String srcName = null;
				
				if (c < columns.size() - 1) {
					//normal column
					srcName = predictData.getModel().getSourceByIndex(c).getDisplayName();
				} else {
					srcName = "All Sources";
				}

				switch (columnNameFormat) {
					case SOURCE_NAME_ONLY: 
					{
						ColumnAttribsBuilder attribs = new ColumnAttribsBuilder();
						attribs.setName(srcName);
						RenameColumnDataView renamed = new RenameColumnDataView(columns.get(c), attribs);
						columns.set(c, renamed);
						break;
					}	
					case DATA_SERIES_AND_SOURCE:
					{
						ColumnAttribsBuilder attribs = new ColumnAttribsBuilder();
						attribs.setName(dataSeriesName + " for " + srcName);
						RenameColumnDataView renamed = new RenameColumnDataView(columns.get(c), attribs);
						columns.set(c, renamed);
						break;
					}
					default:
						
				}
			}
		}
		
		return columns;
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
