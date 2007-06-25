package gov.usgswim.sparrow;

import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import oracle.mapviewer.share.ext.NSDataProvider;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;
import oracle.mapviewer.share.Field;

import org.apache.commons.lang.StringUtils;



import org.apache.log4j.Logger;

public class MapViewerSparrowDataProvider implements NSDataProvider {
	protected static Logger log =
		Logger.getLogger(MapViewerSparrowDataProvider.class); //logging for this class
		
	//Initiation parameter key Constants
	public static final String JNDI_DATASOURCE_NAME_KEY = "jndi-datasource-name";
	public static final String JDBC_DRIVER_KEY = "jdbc-driver";
	public static final String JDBC_URL_KEY = "jdbc-url";
	public static final String JDBC_USER_KEY = "jdbc-user";
	public static final String JDBC_PWD_KEY = "jdbc-pwd";
	
	//Request parameter key constants
	/**
	 * The db unique id of the Sparrow model.  Required.
	 */
	public static final String MODEL_ID_KEY = "model_id";
	
	/**
	 * If "true" (case insensative), ignore cached model data and reload this model.  New data placed in cache.
	 * example:  "True"
	 */
	public static final String IGNORE_CACHE_KEY = "ignore_cache";
	
	/**
	 * A string containing a delimited list, in pairs, of the source id and the
	 * decimal percentage to adjust it.  It is an error to provide a string that
	 * contains anything other then numbers and delimiters, or that contains
	 * an odd number of values.
	 * 
	 * Example:  "1,.25,4,2,8,0"
	 * 
	 * In this example:
	 * <ul>
	 * <li>Source #1 has its value multiplied by .25
	 * <li>Source #4 has its value multiplied by 2
	 * <li>Source #8 has its value multiplied by 0 (effectively turning it off)
	 * <li>All other sources not listed as assumed to be unchanged
	 * (that is, they are multiplied by 1).
	 * <ul>
	 */
	public static final String GROSS_SOURCE_ADJUST_KEY = "gross_src_adj";
	
	/**
	 * Determines what data is returned.  Valid values are the
	 * RESULT_MODE_VALUE_XXX constants.
	 */
	public static final String RESULT_MODE_KEY = "result_mode";
	
	/**
	 * A possible value for the RESULT_MODE_KEY parameter.
	 * This mode returns the new calculated value.
	 */
	public static final String RESULT_MODE_VALUE = "value";
	
	/**
	 * A possible value for the RESULT_MODE_KEY parameter.
	 * This mode returns the percentage change from the original model predicted value.
	 * The value is returned a whole percentage (ie, if the value doubled, 100 would be returned)
	 */
	public static final String RESULT_MODE_PERC_CHG = "perc_chg";
	
	/**
	 * A possible value for the RESULT_MODE_KEY parameter.
	 * This mode returns the percentage change from the original model predicted value.
	 * The value is returned a decimal percentage (ie, if the value doubled, 1 would be returned)
	 */
	public static final String RESULT_MODE_DEC_PERC_CHG = "dec_perc_chg";
	
		
	protected String jndiDatasourceName = null;		//The name of a JNDI datasource to use
	
	protected String jdbcDriver = null;
	protected String jdbcUrl = null;
	protected String jdbcUser = null;
	protected String jdbcPwd = null;
	
	protected DataSource jndiDS = null; //A jndi datasource for creating data related db connections.
	
	protected HashMap cachedData;

	
	public MapViewerSparrowDataProvider() {
	}
	


	/**
	 * Called onces at creation time.
	 * @param properties
	 * @return
	 */
	public boolean init(Properties properties) {
		jndiDatasourceName = properties.getProperty(JNDI_DATASOURCE_NAME_KEY);
		jdbcDriver = properties.getProperty(JDBC_DRIVER_KEY);
		jdbcUrl = properties.getProperty(JDBC_URL_KEY);
		jdbcUser = properties.getProperty(JDBC_USER_KEY);
		jdbcPwd = properties.getProperty(JDBC_PWD_KEY);
		
		log.info("MVSparrowDataProvider initiated with " + JNDI_DATASOURCE_NAME_KEY + " = " + jndiDatasourceName);
		log.info("MVSparrowDataProvider initiated with " + JDBC_DRIVER_KEY + " = " + jdbcDriver);
		log.info("MVSparrowDataProvider initiated with " + JDBC_URL_KEY + " = " + jdbcUrl);
		log.info("MVSparrowDataProvider initiated with " + JDBC_USER_KEY + " = " + jdbcUser);
		log.info("MVSparrowDataProvider initiated with " + JDBC_PWD_KEY + " = " + (((jdbcUrl)!= null)?"[non-null]":"<null>"));
		
		cachedData = new HashMap(10, 1);
		
		return true;
	}

	public NSDataSet buildDataSet(java.util.Properties params) {
		Hashtable hash = new Hashtable(13);
		
		Iterator it = params.keySet().iterator();
		
		while (it.hasNext()) {
			Object key = it.next();
			hash.put(key, params.get(key));
		}
		
		return buildDataSet(hash);
		
	}
	/**
	 * Called for each request.
	 * @param properties
	 * @return
	 */
	public NSDataSet buildDataSet(Hashtable properties) {
		Data2D result = null;		//The prediction result
		
		long modelId = Long.parseLong( properties.get(MODEL_ID_KEY).toString() );
		String resultMode = (String) properties.get(RESULT_MODE_KEY);
		
		boolean isPercCompareMode = StringUtils.equalsIgnoreCase(RESULT_MODE_PERC_CHG, resultMode);
		boolean isDecPercCompareMode = StringUtils.equalsIgnoreCase(RESULT_MODE_DEC_PERC_CHG, resultMode);
		
		boolean isValueMode = ! (isPercCompareMode || isDecPercCompareMode);	//The default
		boolean isCompareMode = ! isValueMode;		//Convience flag
		
		NSDataSet nsData = null;
		
		long startTime = System.currentTimeMillis();	//Time started

		try {
			AdjustmentSetBuilder adjBuilder = new AdjustmentSetBuilder();
			adjBuilder.setAdjustments(properties);
			PredictionRequest adjRequest = new PredictionRequest(modelId, adjBuilder.getImmutable());
			Data2D adjResult = SharedApplication.getInstance().getPredictResultCache().compute(adjRequest);
			
			if (isCompareMode) {
				//need to run the base prediction and the adjusted prediction
				AdjustmentSetImm noAdj = new AdjustmentSetImm();
				PredictionRequest noAdjRequest = new PredictionRequest(modelId, noAdj);
				Data2D noAdjResult = SharedApplication.getInstance().getPredictResultCache().compute(noAdjRequest);
	
				result = new Data2DPercentCompare(noAdjResult, adjResult, isDecPercCompareMode, true);
			} else {
				//need to run only the adjusted prediction
				result = adjResult;
			}
			
			Data2D sysInfo = SharedApplication.getInstance().getPredictDatasetCache().compute( modelId ).getSys();
			nsData = copyToNSDataSet(result, sysInfo);
			
			log.debug("MVSparrowDataProvider done for model #" + modelId + " (" + nsData.size() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");
			
			return nsData;
			
		} catch (InterruptedException e) {
			log.error("No way to indicate this error to mapViewer, so throwing a runtime exception.", e.getCause());
			throw new RuntimeException(e);
		}
		

	}
	
	protected NSDataSet copyToNSDataSet(Data2D result, Data2D sysInfo) {

		int rowCount = result.getRowCount();
		int colCount = result.getColCount();
		NSRow[] nsRows = new NSRow[rowCount];
		
		for (int r=0; r < rowCount; r++) {
			Field[] row = new Field[2];	//ID
			row[0] = new Field(sysInfo.getInt(r, 0));
			row[0].setKey(true);
			
			row[1] = new Field(result.getDouble(r, colCount - 1));	//Value
			//row[1].setLabelText(true);
	
			NSRow nsRow = new NSRow(row);
			nsRows[r] = nsRow;
		}
		
		if (log.isDebugEnabled()) {
			debugNSData(nsRows);
		}
		
		return new NSDataSet(nsRows);
	}
	
	protected void debugNSData(NSRow[] nsRows) {
		int maxRow = 10;
		if (maxRow > nsRows.length) maxRow = nsRows.length;
		
		log.debug("MVSparrowDataProvider These are the first ten rows of data: ");
		for (int r = 0; r < maxRow; r++)  {
			StringBuffer sb = new StringBuffer();
			for (int c = 0; c < nsRows[0].size(); c++)  {
				sb.append(nsRows[r].get(c).toString());
				if (nsRows[r].get(c).isKey()) sb.append("[Key] ");
				if (nsRows[r].get(c).isLabelText()) sb.append("[Lab] ");
				if ((c + 1) < nsRows[0].size()) sb.append("| ");
			}
			log.debug(sb.toString());
		}
	}

	/**
	 * Called once when this instance is destroyed
	 */
	public void destroy() {
		//
	}
	

	
}
