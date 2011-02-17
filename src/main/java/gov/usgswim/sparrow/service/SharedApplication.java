package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.ConfiguredCache.*;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.action.DeletePredefinedSession;
import gov.usgswim.sparrow.action.DeliveryReach;
import gov.usgswim.sparrow.action.FilterPredefinedSessions;
import gov.usgswim.sparrow.action.LoadPredefinedSession;
import gov.usgswim.sparrow.action.LoadReachesInBBox;
import gov.usgswim.sparrow.action.PredictionContextHandler;
import gov.usgswim.sparrow.action.SavePredefinedSession;
import gov.usgswim.sparrow.cachefactory.AggregateIdLookupKludge;
import gov.usgswim.sparrow.cachefactory.NSDataSetFactory;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.ModelBBox;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.LogicalSet;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.request.*;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import oracle.jdbc.driver.OracleDriver;
import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.log4j.Logger;

//TODO:  This class contains a lot of unused caches...
public class SharedApplication  {
	protected static Logger log =
		Logger.getLogger(SharedApplication.class);

	private static SharedApplication instance;

	//Warehouse (read-only) db connection info
	public static final String READ_ONLY_JNDI_DS_NAME = "java:comp/env/jdbc/sparrow_dss";
	private DataSource roDatasource;
	private boolean roLookupFailed = false;
	
	//Transactional db connection info
	public static final String READ_WRITE_JNDI_DS_NAME = "java:comp/env/jdbc/sparrow_dss_trans";
	private DataSource rwDatasource;
	private boolean rwLookupFailed = false;
	
	//Number of times a connection has been requested
	private int roConnectionRequestCount = 0;

	//an ehcache test cache
	public static final String SERIALIZABLE_CACHE = PredictContext.name();


	private SharedApplication() {

	}

	public static synchronized SharedApplication getInstance() {
		if (instance == null) {
			instance = new SharedApplication();
		}

		return instance;
	}

	private static Connection getROConnectionFromCommandLineParams() throws SQLException {
		//synchronized (this) {
		{
			DriverManager.registerDriver(new OracleDriver());
		}
		String dbuser = System.getProperty("dbuser");
		String dbpass = System.getProperty("dbpass");
		String url = System.getProperty("dburl");
		Connection connection;
		connection = DriverManager.getConnection(url, dbuser, dbpass);
		return connection;
	}
	
	private static Connection getRWConnectionFromCommandLineParams() throws SQLException {
		//synchronized (this) {
		{
			DriverManager.registerDriver(new OracleDriver());
		}
		String dbuser = System.getProperty("rw_dbuser");
		String dbpass = System.getProperty("rw_dbpass");
		String url = System.getProperty("rw_dburl");
		Connection connection;
		connection = DriverManager.getConnection(url, dbuser, dbpass);
		return connection;
	}
	
	// ================
	// INSTANCE METHODS
	// ================

	public Connection getROConnection() throws SQLException {
		roConnectionRequestCount++;
		
		Connection c = findROConnection();
		
		if (c != null) {
			if (! c.isClosed()) {
				if (log.isTraceEnabled()) {
					Exception e = new Exception("Exception created only for stacktrace");
					e.fillInStackTrace();
					log.trace("Fetching connection #" + roConnectionRequestCount, e);
				} else if (log.isDebugEnabled()) {
					log.debug("Fetching connection #" + roConnectionRequestCount);
				}
			} else {
				SQLException e = new SQLException("The datasource returned a CLOSED CONNECTION for #" + roConnectionRequestCount);
				e.fillInStackTrace();
				log.error(e);
				throw e;
			}
		} else {
			SQLException e = new SQLException("The datasource returned a NULL CONNECTION for #" + roConnectionRequestCount);
			e.fillInStackTrace();
			log.error(e);
			throw e;
		}
		
		return c;
	}
	
	public Connection getRWConnection() throws SQLException {
		return findRWConnection();
	}
	
	private Connection findRWConnection() throws SQLException {
		synchronized (this) {
			if (rwDatasource == null && ! rwLookupFailed) {
				try {
					Context ctx = new InitialContext();
					rwDatasource = (DataSource) ctx.lookup(READ_WRITE_JNDI_DS_NAME);
					//					Connection connection = ds.getConnection();

				} catch (Exception e) {
					rwLookupFailed = true;
				}
			}
			
			if (rwDatasource != null) {
				return rwDatasource.getConnection();
			}
		}


		//if we fall through from above, fetching from cmd line does
		//not need to be sync'ed
		return getRWConnectionFromCommandLineParams();
	}

	private Connection findROConnection() throws SQLException {
		synchronized (this) {
			if (roDatasource == null && ! roLookupFailed) {
				try {
					Context ctx = new InitialContext();
					roDatasource = (DataSource) ctx.lookup(READ_ONLY_JNDI_DS_NAME);
					//					Connection connection = ds.getConnection();

				} catch (Exception e) {
					roLookupFailed = true;
				}
			}
			
			if (roDatasource != null) {
				return roDatasource.getConnection();
			}
		}


		//if we fall through from above, fetching from cmd line does
		//not need to be sync'ed
		return getROConnectionFromCommandLineParams();
	}



	public void clearAllCaches() {
		SparrowCacheManager.getInstance().clearAll();
	}

	public Integer putSerializable(Serializable context) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(SERIALIZABLE_CACHE);
		Integer hash = context.hashCode();
		c.put( new Element(hash, context) );
		return hash;
	}

	public Serializable getSerializable(Integer id) {
		return getSerializable(id, false);
	}

	public Serializable getSerializable(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(SERIALIZABLE_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((Serializable) e.getObjectValue()):null;
	}

	//PredictContext Cache
	public Integer putPredictionContext(PredictionContext context) throws Exception {

		try {
			context = context.clone();
		} catch (CloneNotSupportedException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
		
		PredictionContextRequest req = new PredictionContextRequest(context);
		PredictionContextHandler action = new PredictionContextHandler(req);
		List<PredictionContext> pcList = action.run();
		
		if (pcList.size() == 1) {
			context = pcList.get(0);
			putPredictionContextInLocalCache(context);
			return context.getId();
		} else {
			throw new Exception("Unable to store PredictionContext to persistant storage.");
		}
	}
	
	private void putPredictionContextInLocalCache(PredictionContext context) {
		ConfiguredCache.PredictContext.put(context.getId(), context);
		
		AdjustmentGroups ag = context.getAdjustmentGroups();
		if (ag != null) {
			ConfiguredCache.AdjustmentGroups.put(ag.getId(), ag);
		}

		Analysis anal = context.getAnalysis();
		if (anal != null) {
			ConfiguredCache.Analyses.put(anal.getId(), anal);
		}

		TerminalReaches tr = context.getTerminalReaches();
		if (tr != null) {
			ConfiguredCache.TerminalReaches.put(tr.getId(), tr);
		}

		AreaOfInterest aoi = context.getAreaOfInterest();
		if (aoi != null) {
			ConfiguredCache.AreaOfInterest.put(aoi.getId(), aoi);
		}
	}
	
	/**
	 * Touches the PredictionContext and all of its children.
	 * 
	 * This is the same as calling get and if not found, calling put on each
	 * item.
	 * @param context
	 */
	private void touchPredictionContextInLocalCache(PredictionContext context) {
		ConfiguredCache.PredictContext.touch(context.getId(), context);
		
		AdjustmentGroups ag = context.getAdjustmentGroups();
		if (ag != null) {
			ConfiguredCache.AdjustmentGroups.touch(ag.getId(), ag);
		}

		Analysis anal = context.getAnalysis();
		if (anal != null) {
			ConfiguredCache.Analyses.touch(anal.getId(), anal);
		}

		TerminalReaches tr = context.getTerminalReaches();
		if (tr != null) {
			ConfiguredCache.TerminalReaches.touch(tr.getId(), tr);
		}

		AreaOfInterest aoi = context.getAreaOfInterest();
		if (aoi != null) {
			ConfiguredCache.AreaOfInterest.touch(aoi.getId(), aoi);
		}
	}

	public PredictionContext getPredictionContext(Integer id) throws Exception {
		return getPredictionContext(id, false);
	}

	/**
	 * If quiet, it will not check the persistent storage.
	 * @param id
	 * @param quiet
	 * @return
	 * @throws Exception 
	 */
	public PredictionContext getPredictionContext(Integer id, boolean quiet) throws Exception {

		PredictionContext context = (PredictionContext)
				ConfiguredCache.PredictContext.get(id, quiet);


		if (context != null) {

			touchPredictionContextInLocalCache(context);
			return context.clone();
			
		} else {
			
			if (quiet) {
				//Bail if null & quite - caller just check if in local cache.
				return null;
			} else {
				PredictionContextRequest req = new PredictionContextRequest(id);
				PredictionContextHandler action = new PredictionContextHandler(req);
				List<PredictionContext> pcList = action.run();
				
				if (pcList.size() == 1) {
					//found it in the persistent storage
					context = pcList.get(0);
					
					//put into the local cache
					touchPredictionContextInLocalCache(context);
					return context.clone();
				} else {
					//Couldn't find in persistent storage
					return null;
				}
			}
			
		}

	}

	public AdjustmentGroups getAdjustmentGroups(Integer id) {
		return getAdjustmentGroups(id, false);
	}

	public AdjustmentGroups getAdjustmentGroups(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AdjustmentGroups.name());
		Element e  = (quiet)?c.getQuiet(id):c.get(id);

		if (e != null) {
			try {
				return ((AdjustmentGroups) e.getObjectValue()).clone();
			} catch (CloneNotSupportedException e1) {
				log.error("Unexpected Clone not supported - returning null");
				return null;
			}
		}
		return null;
	}

	//Analysis Cache
	public Analysis getAnalysis(Integer id) {
		return getAnalysis(id, false);
	}

	public Analysis getAnalysis(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(Analyses.name());
		Element e  = (quiet)?c.getQuiet(id):c.get(id);

		if (e != null) {
			try {
				return ((Analysis) e.getObjectValue()).clone();
			} catch (CloneNotSupportedException e1) {
				log.error("Unexpected Clone not supported - returning null");
				return null;
			}
		}
		return null;
	}

	//TerminalReach Cache
	public TerminalReaches getTerminalReaches(Integer id) {
		return getTerminalReaches(id, false);
	}

	public TerminalReaches getTerminalReaches(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(TerminalReaches.name());
		Element e  = (quiet)?c.getQuiet(id):c.get(id);

		if (e != null) {
			try {
				return ((TerminalReaches) e.getObjectValue()).clone();
			} catch (CloneNotSupportedException e1) {
				log.error("Unexpected Clone not supported - returning null");
				return null;
			}
		}
		return null;
	}

	//AreaOfInterest Cache
	public AreaOfInterest getAreaOfInterest(Integer id) {
		return getAreaOfInterest(id, false);
	}

	public AreaOfInterest getAreaOfInterest(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AreaOfInterest.name());
		Element e  = (quiet)?c.getQuiet(id):c.get(id);

		if (e != null) {
			try {
				return ((AreaOfInterest) e.getObjectValue()).clone();
			} catch (CloneNotSupportedException e1) {
				log.error("Unexpected Clone not supported - returning null");
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Returns a filtered list of PredefinedSessions, pulling from the model-id
	 * based cache if possible.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public List<IPredefinedSession> getPredefinedSessions(PredefinedSessionRequest request) throws Exception {
		FilterPredefinedSessions action = new FilterPredefinedSessions(request);
		return action.run();
	}
	
	/**
	 * An overloaded version that returns a list instead of a single value
	 * is useful in the session servlet.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public List<IPredefinedSession> getPredefinedSessions(PredefinedSessionUniqueRequest request) throws Exception {
		IPredefinedSession session = getPredefinedSession(request, false);
		ArrayList<IPredefinedSession> list = new ArrayList<IPredefinedSession>(1);
		list.add(session);
		return list;
	}
	

	/**
	 * REturns a single PredefinedSession, uniquely ID by either an ID or
	 * code.  It will attempt to fetch from the model ID based cache first, 
	 * then fetch from the db if not quiet.
	 * 
	 * @param request
	 * @param quiet
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public IPredefinedSession getPredefinedSession(PredefinedSessionUniqueRequest request, boolean quiet) throws Exception {
		List keys = PredefinedSessions.getKeysWithExpiryCheck();
		for (Object key : keys) {
			List<IPredefinedSession> sessions =
				(List<IPredefinedSession>) PredefinedSessions.get(key, true);
			
			for (IPredefinedSession session : sessions) {
				if (session.getId().equals(request.getId()) ||
						session.getUniqueCode().equalsIgnoreCase(request.getUniqueCode())) {
					return session;
				}
			}
			
		}
		
		if (! quiet) {
			//Didn't find it, so load it direct from the db
			LoadPredefinedSession action = new LoadPredefinedSession(request);
			IPredefinedSession session = action.run();
			return session;
		} else {
			return null;
		}
	}
	
	
	
	/**
	 * Deletes the passed session from the database, invalidates the cache,
	 * and returns the db version of the session.  The returned session
	 * will have a null ID.
	 * 
	 * An exception is thrown if it cannot be deleted.
	 * 
	 * @param session
	 * @return
	 * @throws Exception 
	 */
	public IPredefinedSession deletePredefinedSession(IPredefinedSession session) throws Exception {
		DeletePredefinedSession action = new DeletePredefinedSession(session);
		IPredefinedSession deleted = action.run();
		
		if (deleted != null) {
			//Removed all cached PS's for the specified model
			PredefinedSessions.remove(session.getModelId());
		}
		
		return deleted;
	}
	
	/**
	 * Creates or Updates the passed session to the database, invalidates the cache,
	 * and returns the db version of the session.  The returned session
	 * will have a non-null ID.
	 * 
	 * An exception is thrown if it cannot be saved.
	 * 
	 * @param session
	 * @return
	 * @throws Exception 
	 */
	public IPredefinedSession savePredefinedSession(IPredefinedSession session) throws Exception {
		SavePredefinedSession action = new SavePredefinedSession(session);
		IPredefinedSession saved = action.run();
		
		if (saved != null) {
			//Removed all cached PS's for the specified model
			PredefinedSessions.remove(session.getModelId());
		}
		
		return saved;
	}
	
	
	//NSDataSet Cache
	public NSDataSet getNSDataSet(PredictionContext context) throws Exception {
		return getNSDataSet(context, false);
	}

	public NSDataSet getNSDataSet(PredictionContext context, boolean quiet) throws Exception {
		//rm all reference to the cache, since it seems completely broken.
		//return (NSDataSet) NSDataSet.get(context, quiet);

		NSDataSetFactory factory = new NSDataSetFactory();
		NSDataSet dataset = (NSDataSet) factory.createEntry(context);
		return dataset;
	}


	//PredictData
	public PredictData getPredictData(Long id) {
		return getPredictData(id, false);
	}

	public PredictData getPredictData(Long id, boolean quiet) {
		return (PredictData) PredictData.get(id, quiet);
	}

	//DeliveryFractionHash
	public HashMap<Integer, DeliveryReach> getDeliveryFractionHash(TerminalReaches targets) {
		return getDeliveryFractionHash(targets, false);
	}

	public HashMap<Integer, DeliveryReach> getDeliveryFractionHash(TerminalReaches targets, boolean quiet) {
		return (HashMap<Integer, DeliveryReach>) DeliveryFractionHash.get(targets, quiet);
	}
	
	//DeliveryFraction
	public ColumnData getDeliveryFraction(TerminalReaches targets) {
		return getDeliveryFraction(targets, false);
	}

	public ColumnData getDeliveryFraction(TerminalReaches targets, boolean quiet) {
		return (ColumnData) DeliveryFraction.get(targets, quiet);
	}

	//Uncertainty Data
	public UncertaintyData getStandardErrorEstimateData(UncertaintyDataRequest req) {
		return getStandardErrorEstimateData(req, false);
	}

	public UncertaintyData getStandardErrorEstimateData(UncertaintyDataRequest req, boolean quiet) {
		return (UncertaintyData) StandardErrorEstimateData.get(req, quiet);
	}

	//PredictResult Cache
	public PredictResult getPredictResult(AdjustmentGroups adjustments) {
		return getPredictResult(adjustments, false);
	}

	public PredictResult getPredictResult(AdjustmentGroups adjustments, boolean quiet) {
		return (PredictResult) PredictResult.get(adjustments, quiet);
	}

	//ComparisonResult Cache
	public DataColumn getComparisonResult(PredictionContext context) {
		return getComparisonResult(context, false);
	}

	public DataColumn getComparisonResult(PredictionContext context, boolean quiet) {
		return (DataColumn) ComparisonResult.get(context, quiet);
	}

	//AnalysisResult Cache
	public DataColumn getAnalysisResult(PredictionContext context) {
		return getAnalysisResult(context, false);
	}

	public DataColumn getAnalysisResult(PredictionContext context, boolean quiet) {
		return (DataColumn) ConfiguredCache.AnalysisResult.get(context, quiet);
	}

	//ReachByPoint Cache
	public ReachInfo getReachByPointResult(ModelPoint req) {
		return getReachByPointResult(req, false);
	}

	public ReachInfo getReachByPointResult(ModelPoint req, boolean quiet) {
		return (ReachInfo) IdentifyReachByPoint.get(req, quiet);
	}

	//ReachByID Cache
	public ReachInfo getReachByIDResult(ReachID req) {
		return getReachByIDResult(req, false);
	}

	public ReachInfo getReachByIDResult(ReachID req, boolean quiet) {
		return (ReachInfo) IdentifyReachByID.get(req, quiet);
	}

	//LoadReachAttributes
	public DataTable getReachAttributes(ReachID req) {
		return getReachAttributes(req, false);
	}

	public DataTable getReachAttributes(ReachID req, boolean quiet) {
		return (DataTable) LoadReachAttributes.get(req, quiet);
	}

	//LoadModelMetadata
	public List<SparrowModel> getModelMetadata(ModelRequestCacheKey req) {
		return getModelMetadata(req, false);
	}

	@SuppressWarnings("unchecked")
	public List<SparrowModel> getModelMetadata(ModelRequestCacheKey req, boolean quiet) {
		return (List<SparrowModel>) LoadModelMetadata.get(req, quiet);
	}

	//Adjusted Source Cache
	public DataTable getAdjustedSource(AdjustmentGroups req) {
		return getAdjustedSource(req, false);
	}

	public DataTable getAdjustedSource(AdjustmentGroups req, boolean quiet) {
		return (DataTable) AdjustedSource.get(req, quiet);
	}

	//Adjusted Source Cache
	public List<Long> getReachesByCriteria(LogicalSet req) {
		return getReachesByCriteria(req, false);
	}

	@SuppressWarnings("unchecked")
	public List<Long> getReachesByCriteria(LogicalSet req, boolean quiet) {
		return (List<Long>) ReachesByCriteria.get(req, quiet);
	}

	//Data Binning Cache
	public BigDecimal[] getDataBinning(BinningRequest req) {
		return getDataBinning(req, false);
	}

	public BigDecimal[] getDataBinning(BinningRequest req, boolean quiet) {
		return (BigDecimal[]) DataBinning.get(req, quiet);
	}

	//Catchment Area Cache
	public DataTable getCatchmentAreas(CatchmentArea req) {
		return getCatchmentAreas(req, false);
	}
	
	public DataTable getCatchmentAreas(CatchmentArea req, boolean quiet) {
		return (DataTable) CatchmentAreas.get(req, quiet);
	}

	//Flux (Stream Flow) Cache
	public DataColumn getStreamFlow(Long req) {
		return getStreamFlow(req, false);
	}
	
	public DataColumn getStreamFlow(Long req, boolean quiet) {
		return (DataColumn) StreamFlow.get(req, quiet);
	}

	//Aggregate Id Lookup Kludge Cache - temporary
	public AggregateIdLookupKludge getAggregateIdLookup(String aggLevel) {
		return getAggregateIdLookup(aggLevel, false);
	}

	public AggregateIdLookupKludge getAggregateIdLookup(String aggLevel, boolean quiet) {
		return (AggregateIdLookupKludge) AggregateIdLookup.get(aggLevel, quiet);
	}
	
	/////////////////////////////////////////////
	// Non-Cached items
	/////////////////////////////////////////////
	public Long[] getReachesInBBox(ModelBBox modelBBox) throws Exception {
		return getReachesInBBox(modelBBox, false);
	}

	public Long[] getReachesInBBox(ModelBBox modelBBox, boolean quiet) throws Exception {
		LoadReachesInBBox action = new LoadReachesInBBox();
		action.setModelBBox(modelBBox);
		Long[] result = action.run();
		return result;
	}
	

	public static DataTableWritable queryToDataTable(String query) throws NamingException, SQLException {
		Connection conn = getInstance().getROConnection();
		ResultSet rset = null;
		DataTableWritable attributes = null;
		try {
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rset = st.executeQuery(query);
			attributes = DataTableConverter.toDataTable(rset);
		} finally {
			closeConnection(conn, rset);
		}
		return attributes;
	}


	/**
	 * Properly closing Connections and ResultSets without throwing exceptions, see the section
	 * "Here is an example of properly written code to use a db connection obtained from a connection pool"
	 * http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html
	 * @param conn
	 * @param rset
	 */
	public static void closeConnection(Connection conn, ResultSet rset) {
		if (rset != null) {
			try { rset.close(); } catch (SQLException e) { ; }
			rset = null;
		}
		if (conn != null) {
			try { conn.close(); } catch (SQLException e) { ; }
			conn = null;
		}
	}


}

