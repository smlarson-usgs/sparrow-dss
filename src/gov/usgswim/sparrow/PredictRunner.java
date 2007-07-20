package gov.usgswim.sparrow;

import gov.usgswim.sparrow.util.JDBCUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import oracle.mapviewer.share.Field;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class PredictRunner {
	protected static Logger log =
		Logger.getLogger(PredictRunner.class); //logging for this class

	//JNDI Datasource for JDBC connection
	DataSource datasource;
	
	
	/**
	 * ResultMode determines what type of calculation is done.
	 */
	public enum ResultMode {
		VALUE("value", "Bulk adjust the source by multiplying by a coef.", true, false),
		PERCENTAGE_CHANGE("perc_chg", "Percentage change from the original model predicted value, as a whole percentage.", false, true),
		DECIMAL_PERCENTAGE_CHANGE("dec_perc_chg", "Percentage change from the original model predicted value, as a decimal percentage.", false, true);
		
		private String _name;
		private String _desc;
		private boolean _isVal;
		private boolean _isComp;
		
		ResultMode(String name, String description, boolean isValue, boolean isComparison) {
			_name = name;
			_desc = description;
			_isVal = isValue;
			_isComp = isComparison;
		}
		
		public String toString() {
			return _name;
		}
		
		public String getName() {
			return _name;
		}
		
		public String getDescription() {
			return _desc;
		}
		
		public boolean isValue() {
			return _isVal;
		}
		
		public boolean isComparison() {
			return _isComp;
		}
		
		/**
		 * Returns the key used to pass a resultMode
		 * @return
		 */
		public static String getKey() {
			return "result_mode";
		}
		
		public static ResultMode find(String name) {
			for(ResultMode mode: ResultMode.values()) {
				if (mode._name.equalsIgnoreCase(name)) return mode;
			}
			return null;
		}
			
	};
	
	
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
	 * Cached PreditionDataSet instances, so that we don't need to load them
	 * each time.
	 */
	protected HashMap cachedData;



	public PredictRunner(DataSource datasource) {
		this.datasource = datasource;
	}
	

	/**
	 * Called for each request.
	 * @param properties
	 * @return
	 */
	public Data2D runPrediction(Hashtable properties) throws Exception {
		int modelId = Integer.parseInt( properties.get(MODEL_ID_KEY).toString() );
		boolean ignoreCache = Boolean.parseBoolean( (String) properties.get(IGNORE_CACHE_KEY) );
		ResultMode resultMode = ResultMode.find((String) properties.get(ResultMode.getKey()) );
		
		
		Data2D data = null;	//The returned data
		
		long startTime = System.currentTimeMillis();	//Time started
		
		log.debug("runPrediction for model_id" + " = " + modelId);
		
		try {
			
			Data2D result = null;		//The prediction result
			PredictionDataSet nonAdjustedData = null;	//The dataset w/ no source adjustments
			PredictionDataSet adjustedData = null;	//The dataset after source adjustments
			
			//Load the data from the db (or cache)
			nonAdjustedData = loadData(modelId, ignoreCache);

			//Do source adjustments
			{
				AdjustmentSetBuilder srcAdjBuilder = new AdjustmentSetBuilder();
				srcAdjBuilder.addGrossSrcAdjustments(properties);
				AdjustmentSet srcAdj = srcAdjBuilder.getImmutable();
				Data2D adjSource = srcAdj.adjust(nonAdjustedData.getSrc(), nonAdjustedData.getSrcIds(), nonAdjustedData.getSys());
				adjustedData = (PredictionDataSet) nonAdjustedData.clone();
				adjustedData.setSrc(adjSource);
				
			}
			
			//Run the prediction (adjusted source values)
			result = doRun(adjustedData);
			

			//Run the prediction on the non adjusted value (if doing comparison) and
			//Set the output data to be the result of the comparison
			if (resultMode.isComparison()) {
				Data2D nonAdjResult = doRun(nonAdjustedData);
				result = new Data2DPercentCompare(nonAdjResult, result, resultMode.equals(ResultMode.DECIMAL_PERCENTAGE_CHANGE));	
			}
			
			log.info("Returning data (" + data.getRowCount() + " rows).  Total time spent: " + (System.currentTimeMillis() - startTime) + "ms");
			
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
			
		long startTime = System.currentTimeMillis();	

		Integer key = new Integer(modelId);
		Object data = null;	
		
		if (! ignoreCache) {
			data = cachedData.get(key);
			
			if (data != null) {
				data = ((DataProviderCacheProxy) data).getData();
				
				if (data != null) {
					log.debug("Returning cached data for model_id " + modelId);
					return (PredictionDataSet) data;
				} else {
					log.warn("Cached data is null for model_id " + modelId + ".  We should block here!!!!");
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
				

				log.debug(
					"Loaded data from db (" +
					((PredictionDataSet)data).getSrc().getRowCount() +
					" rows)  Time Spent: " + (System.currentTimeMillis() - startTime) + "ms");
			
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
	
	protected Data2D doRun(PredictionDataSet data) {
		long startTime = System.currentTimeMillis();
		
		PredictSimple adjPredict = new PredictSimple(data);
		Data2D result = adjPredict.doPredict();
		log.debug("Prediction done.  Time Spent: " + (System.currentTimeMillis() - startTime) + "ms");
		
		return result;
	}
	
	/**
	 * Called once when this instance is destroyed
	 */
	public void destroy() {
		datasource = null;
	}
	
	/**
	 * Returns the connection from the datasource w/ a bit of logging.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {

			Connection conn = datasource.getConnection();
			
			if (conn == null) {
				log.error("The datasource returned a null connection");
	
			} else if (conn.isClosed()) {
				log.error("The datasource returned a closed connection");
				conn = null;
			}

		
			return conn;
	}
	
}
