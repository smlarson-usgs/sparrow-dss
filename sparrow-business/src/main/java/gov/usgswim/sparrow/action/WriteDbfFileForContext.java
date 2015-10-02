package gov.usgswim.sparrow.action;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import gov.usgs.cida.sparrow.service.util.NamingConventions;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
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
	private ReachRowValueMap reachRowValueMap;
	
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
		outputFile = getDbfFile();
		outputFile.createNewFile();
		
		DataSeriesType type = context.getAnalysis().getDataSeries();
		
		//grab the delivery fraction map if this is a delivery data series.
		//This is used to weed out the reaches that are not upstream of the
		//user selected terminal reaches.
		if (type.isDeliveryRequired()) {

			TerminalReaches tReaches = context.getTerminalReaches();

			assert(tReaches != null) : "client should not submit a delivery request without reaches";

			reachRowValueMap = SharedApplication.getInstance().getDeliveryFractionMap(tReaches);

			if (reachRowValueMap == null) {
				throw new Exception("Unable to find or calculate the delivery fraction map");
			}
		} else {
			reachRowValueMap = null;
		}
	}
	
	

	@Override
	protected void validate() {
		if (context == null) {
			addValidationError("The context connot be null");
		}
	}

	
	@Override
	public File doAction() throws Exception {
		WriteDbfFile writeAction = new WriteDbfFile(columnIndex, dataColumn, outputFile, ID_COLUMN_NAME, reachRowValueMap);
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
	
	@Override
	public Long getModelId() {
		if (context != null) {
			return context.getModelID();
		} else {
			return null;
		}
	}

	/**
	 * Provides a file pointer to the DBF file on disk. 
	 * 
	 * The DBF file may or may not exist. Callers of this function should use 
	 * File object's exist() method to test whether it does. This function is used
	 * primarily to allow callers to decide whether to write a new DBF file or 
	 * skip 
	 * 
	 * @return a File pointer to the location of the DBF file on disk
	 */
	public File getDbfFile() {
		return new File(getDataDirectory(), NamingConventions.convertContextIdToXMLSafeName(context.getModelID().intValue(), context.getId()) + ".dbf");
	}

}
