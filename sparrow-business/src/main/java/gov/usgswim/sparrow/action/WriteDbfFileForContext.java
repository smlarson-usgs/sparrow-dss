package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;
import java.io.File;

/**
 * Creates a dbf file containing an ID column and a data column.
 * 
 * The columns are written in order as follows:
 * <ol>
 * <li>[ID Column of named specified] : Integer number of 10 digits.
 * <li>VALUE : Decimal number of 14 digits total, four of which are decimal places.
 * </ol>
 * 
 * Note that 10 digits is the NHD definition of the COMID:
 * http://nhd.usgs.gov/nhd_faq.html#q119
 * 
 * @author eeverman
 *
 */
public class WriteDbfFileForContext extends Action<File> {

	//User config
	private PredictionContext context;
	
	//Self initialized
	private String idColumnName;
	private ColumnData idColumn;
	private ColumnData dataColumn;
	private File outputFile;
	
	public WriteDbfFileForContext(PredictionContext context) {
		this.context = context;
	}

	@Override
	protected void initFields() throws Exception {
		
		dataColumn = context.getDataColumn().getColumnData();
		DataTable idTable = SharedApplication.getInstance().getModelReachIdentificationAttributes(context.getModelID());
		idColumn = idTable.getColumn(3);
		ModelRequestCacheKey modelReq = new ModelRequestCacheKey(context.getModelID(), false, false, false);
		SparrowModel model = SharedApplication.getInstance().getModelMetadata(modelReq).get(0);
		idColumnName = model.getEnhNetworkIdColumn();
		

		outputFile = new File(getDefaultCacheDirectory(), context.getId().toString() + ".dbf");
		outputFile.createNewFile();
	}
	
	

	@Override
	protected void validate() {
		if (context == null) {
			addValidationError("The context connot be null");
		}
	}

	
	@Override
	public File doAction() throws Exception {

		WriteDbfFile writeAction = new WriteDbfFile(idColumn, dataColumn, outputFile, idColumnName);
		return writeAction.run();
	}

	public File getDefaultCacheDirectory() {
		
		File home = new File(System.getProperty("user.home"));
		File cacheDir = new File(home, "sparrow");
		cacheDir = new File(cacheDir, "data_cache");
		
		return cacheDir;
	}

}
