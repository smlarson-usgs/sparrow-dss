package gov.usgswim.sparrow.action;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import gov.usgs.cida.sparrow.service.util.NamingConventions;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import java.io.File;
import java.nio.file.Files;

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

	private static final String ID_COLUMN_NAME = "IDENTIFIER";
	private static final String DATA_EXPORT_DIRECTORY = "data-export-directory";
          private File dataDirectory;
	//User config
	private PredictionContext context;
	
	//Self initialized
	private ColumnIndex columnIndex;
	private ColumnData dataColumn;
	private File outputFile;
	
	public WriteDbfFileForContext(PredictionContext context) {
		this.context = context;
	}
    
         protected WriteDbfFileForContext() {
             // Created for testing
         }

	@Override
	protected void initFields() throws Exception {
		File dataDir = getDataDirectory();
                    
                    if (!dataDir.exists()) {
                        Files.createDirectories(dataDir.toPath());
                    }
        
		dataColumn = context.getDataColumn().getColumnData();
		columnIndex = SharedApplication.getInstance().getPredictData(context.getModelID()).getTopo().getIndex();
		outputFile = new File(dataDir, NamingConventions.convertContextIdToXMLSafeName(context.getId()) + ".dbf");
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
		WriteDbfFile writeAction = new WriteDbfFile(columnIndex, dataColumn, outputFile, ID_COLUMN_NAME);
		return writeAction.run();
	}

	protected File getDataDirectory() {
		File dDir;

		if (this.dataDirectory != null) {
			dDir =  this.dataDirectory;
		} else {
			DynamicReadOnlyProperties props = SharedApplication.getInstance().getConfiguration();
			String fallbackDataDirectory = System.getProperty("user.home") 
					+ File.separatorChar 
					+ "sparrow"
					+ File.separatorChar
					+ "data";
			String sparrowDataDirectory = props.getProperty(DATA_EXPORT_DIRECTORY, fallbackDataDirectory);
			dDir =  new File(sparrowDataDirectory);
			this.dataDirectory = dDir;
		}

		return dDir;
	}

}
