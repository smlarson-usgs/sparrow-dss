package gov.usgswim.sparrow;

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
		int modelId = Integer.parseInt( properties.get(MODEL_ID_KEY).toString() );
		boolean ignoreCache = Boolean.parseBoolean( (String) properties.get(IGNORE_CACHE_KEY) );
		String globalSrcAdj = (String) properties.get(GROSS_SOURCE_ADJUST_KEY);
		String resultMode = (String) properties.get(RESULT_MODE_KEY);
		
		boolean isPercCompareMode = StringUtils.equalsIgnoreCase(RESULT_MODE_PERC_CHG, resultMode);
		boolean isDecPercCompareMode = StringUtils.equalsIgnoreCase(RESULT_MODE_DEC_PERC_CHG, resultMode);
		
		boolean isValueMode = ! (isPercCompareMode || isDecPercCompareMode);	//The default
		boolean isCompareMode = ! isValueMode;		//Convience flag
		
		
		NSDataSet data = null;
		
		long startTime = System.currentTimeMillis();	//Time started
		long incStartTime = 0;	//Start time at each incremental step
		long incEndTime = 0;	//end time at each incremental step
		
		log.info("MVSparrowDataProvider buildDataSet: " + "model_id" + " = " + modelId);
		
		try {
			
			Data2D result = null;		//The prediction result
			Data2D sysInfo = null;	//The system info associated w/ the result (contains the unique id numbers)
			PredictionDataSet nonAdjustedData = null;	//The dataset w/ no source adjustments
			PredictionDataSet adjustedData = null;	//The dataset after source adjustments
			
			//Load the data from the db (or cache)
			incStartTime = System.currentTimeMillis();
			nonAdjustedData = loadData(modelId, ignoreCache);
			
			incEndTime = System.currentTimeMillis();
			log.debug("MVSparrowDataProvider data loaded (" + nonAdjustedData.getSrc().getRowCount() + " rows)  Time Spent: " + (incEndTime - incStartTime) + "ms");
			
			//Do source adjustments
			incStartTime = System.currentTimeMillis();
			adjustedData = adjustSources(nonAdjustedData, globalSrcAdj);
			incEndTime = System.currentTimeMillis();
			log.debug("MVSparrowDataProvider sources adjusted.  Time Spent: " + (incEndTime - incStartTime) + "ms");
			
			//Run the prediction (adjusted source values)
			incStartTime = System.currentTimeMillis();
			PredictSimple adjPredict = new PredictSimple(adjustedData);
			result = adjPredict.doPredict();
			adjPredict = null;
			sysInfo = adjustedData.getSys();
			incEndTime = System.currentTimeMillis();
			log.debug("MVSparrowDataProvider adjusted predict complete.  Time Spent: " + (incEndTime - incStartTime) + "ms");
			
			//Run the prediction on the non adjusted value (if doing comparison)
			if (isCompareMode) {
				incStartTime = System.currentTimeMillis();
				PredictSimple nonAdjPredict = new PredictSimple(nonAdjustedData);
				Double2D nonAdjResult = null;
				nonAdjResult = nonAdjPredict.doPredict();
				nonAdjPredict = null;
				result = new Data2DPercentCompare(nonAdjResult, result, isDecPercCompareMode, true);
				incEndTime = System.currentTimeMillis();
				log.debug("MVSparrowDataProvider non-adjusted predict complete.  Time Spent: " + (incEndTime - incStartTime) + "ms");	
			}
			
			//Copy the result data to an NSDataSet
			incStartTime = System.currentTimeMillis();
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
			
			data = new NSDataSet(nsRows);
			
			incEndTime = System.currentTimeMillis();
			log.debug("MVSparrowDataProvider transfer to NSDataSet complete.  Time Spent: " + (incEndTime - incStartTime) + "ms");
			log.info("MVSparrowDataProvider Returning data (" + rowCount + " rows).  Total time spent: " + (System.currentTimeMillis() - startTime) + "ms");
			
			
			
			//Debug top 10 values
			if (log.isDebugEnabled()) {
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
			
			
		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer...", e);
			return null;
		}
		
		return data;
	}
	
	/**
	 * This method is not syncrhonized, but the risk is basically that the same
	 * data will be loaded twice.
	 * @param modelId
	 * @return
	 * @throws NamingException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	protected PredictionDataSet loadData(int modelId, boolean ignoreCache) throws NamingException, SQLException,
			ClassNotFoundException, Exception {
			
		Integer key = new Integer(modelId);
		Object data = null;	
		
		if (! ignoreCache) {
			data = cachedData.get(key);
			
			if (data != null) {
				log.debug("MVSparrowDataProvider found cached data for model_id " + modelId);
				data = ((DataProviderCacheProxy) data).getData();
				
				if (data != null) {
					log.debug("MVSparrowDataProvider cache getData() returned non-null data for model_id " + modelId);
					return (PredictionDataSet) data;
				} else {
					log.warn("MVSparrowDataProvider cache getData() returned NULL data for model_id " + modelId);
				}
				
				
			}
		}


		//Couldn't return a cached dataset
		{
		
			//Create a proxy placehold to prevent others from trying to create
			DataProviderCacheProxy proxy = new DataProviderCacheProxy(key);
			cachedData.put(key, proxy);
			
			Connection conn = null;
			try {
			
				conn = getConnection();
				data = JDBCUtil.loadMinimalPredictDataSet(conn, modelId);
				proxy.setData(data);
				return (PredictionDataSet) data;
				
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception ee) {
						/* Ignore */
					}
					conn = null;
				}
			}
		}

	}
	
	/**
	 * @todo This assumes the id number is the same as the column position.
	 * @param data
	 * @param globalSrcAdj
	 */
	protected PredictionDataSet adjustSources(PredictionDataSet data, String globalSrcAdj) {
		globalSrcAdj = StringUtils.trimToNull(globalSrcAdj);
		
		log.debug("MVSparrowDataProvider adjustment STring: " + globalSrcAdj);
		
		if (globalSrcAdj != null) {
			String[] sVals = StringUtils.split(globalSrcAdj, ", :;|");
			double[] dVals = new double[sVals.length];
			
			for (int i = 0; i < dVals.length; i++)  {
				dVals[i] = Double.parseDouble(sVals[i]);
			}
			
			Data2DColumnCoefView view = new Data2DColumnCoefView(data.getSrc());
			
			for (int i = 0; i < dVals.length; i+=2)  {
				int col = ((int) dVals[i]) - 1;
				double coef = dVals[i + 1];
				view.setCoef(col, coef);
			}
			
			return new PredictionDataSet(data.getTopo(), data.getCoef(), view, data.getDecay(), data.getSys());
			
		} else {
			
			return data;	//no adjustment
		}
		
		
	}

	/**
	 * Called once when this instance is destroyed
	 */
	public void destroy() {
		jndiDS = null;
	}
	
	public synchronized Connection getConnection() throws NamingException, SQLException,
																												ClassNotFoundException {

		Connection conn = null;

		if (jndiDatasourceName != null) {
			if (jndiDS == null) {
				log.debug("Build new data jdbc datasouce from jndi name '" + jndiDatasourceName + "'");
				InitialContext context = new InitialContext();
				jndiDS = (DataSource)context.lookup(jndiDatasourceName);
			}
	
			conn = jndiDS.getConnection();
			
			if (conn == null) {
				log.error("The jndi datasource '" + jndiDatasourceName + "' return a null connection");
	
			} else if (conn.isClosed()) {
				log.error("The jndi datasource '" + jndiDatasourceName + "' returned a closed connection");
				conn = null;
			}
		} else if (jdbcDriver != null && jdbcUrl != null && jdbcUser != null && jdbcPwd != null) {
			
			Class.forName(jdbcDriver);
			conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPwd);		
			
			if (conn == null) {
				log.error("The jdbc datasource '" + jdbcUrl + "' return a null connection");
			}
		}

		
		return conn;
	}
	
}
