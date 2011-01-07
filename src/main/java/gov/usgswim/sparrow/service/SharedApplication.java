package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.ConfiguredCache.AdjustedSource;
import static gov.usgswim.sparrow.service.ConfiguredCache.AdjustmentGroups;
import static gov.usgswim.sparrow.service.ConfiguredCache.AggregateIdLookup;
import static gov.usgswim.sparrow.service.ConfiguredCache.Analyses;
import static gov.usgswim.sparrow.service.ConfiguredCache.AreaOfInterest;
import static gov.usgswim.sparrow.service.ConfiguredCache.CatchmentAreas;
import static gov.usgswim.sparrow.service.ConfiguredCache.ComparisonResult;
import static gov.usgswim.sparrow.service.ConfiguredCache.DataBinning;
import static gov.usgswim.sparrow.service.ConfiguredCache.DeliveryFraction;
import static gov.usgswim.sparrow.service.ConfiguredCache.IdentifyReachByID;
import static gov.usgswim.sparrow.service.ConfiguredCache.IdentifyReachByPoint;
import static gov.usgswim.sparrow.service.ConfiguredCache.LoadModelMetadata;
import static gov.usgswim.sparrow.service.ConfiguredCache.LoadReachAttributes;
import static gov.usgswim.sparrow.service.ConfiguredCache.NSDataSet;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredefinedSessions;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredictContext;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredictData;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredictResult;
import static gov.usgswim.sparrow.service.ConfiguredCache.ReachesByCriteria;
import static gov.usgswim.sparrow.service.ConfiguredCache.StandardErrorEstimateData;
import static gov.usgswim.sparrow.service.ConfiguredCache.StreamFlow;
import static gov.usgswim.sparrow.service.ConfiguredCache.TerminalReaches;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.action.DeletePredefinedSession;
import gov.usgswim.sparrow.action.FilterPredefinedSessions;
import gov.usgswim.sparrow.action.LoadReachesInBBox;
import gov.usgswim.sparrow.action.SavePredefinedSession;
import gov.usgswim.sparrow.cachefactory.AggregateIdLookupKludge;
import gov.usgswim.sparrow.cachefactory.BinningRequest;
import gov.usgswim.sparrow.cachefactory.CatchmentArea;
import gov.usgswim.sparrow.cachefactory.ModelRequestCacheKey;
import gov.usgswim.sparrow.cachefactory.ReachID;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.ModelBBox;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.AdvancedAnalysis;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.LogicalSet;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import oracle.jdbc.driver.OracleDriver;
import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.log4j.Logger;

import com.jamonapi.proxy.MonProxyFactory;

//TODO:  This class contains a lot of unused caches...
public class SharedApplication  {
	protected static Logger log =
		Logger.getLogger(SharedApplication.class);

	private static SharedApplication instance;

	private static final String dsName = "java:comp/env/jdbc/sparrow_dss";
	private DataSource datasource;
	private boolean lookupFailed = false;
	
	//Number of times a connection has been requested
	private int connectionRequestCount = 0;

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

	public static Connection getConnectionFromCommandLineParams() throws SQLException {
		//synchronized (this) {
		{
			DriverManager.registerDriver(new OracleDriver());
		}
		String dbuser = System.getProperty("dbuser");
		String dbpass = System.getProperty("dbpass");
		String url = System.getProperty("dburl");
		Connection connection;
		connection = MonProxyFactory.monitor(DriverManager.getConnection(url, dbuser, dbpass));
		return connection;

	}
	
	// ================
	// INSTANCE METHODS
	// ================

	public Connection getConnection() throws SQLException {
		connectionRequestCount++;
		
		Connection c = findConnection();
		
		if (c != null) {
			if (! c.isClosed()) {
				if (log.isTraceEnabled()) {
					Exception e = new Exception("Exception created only for stacktrace");
					e.fillInStackTrace();
					log.trace("Fetching connection #" + connectionRequestCount, e);
				} else if (log.isDebugEnabled()) {
					log.debug("Fetching connection #" + connectionRequestCount);
				}
			} else {
				SQLException e = new SQLException("The datasource returned a CLOSED CONNECTION for #" + connectionRequestCount);
				e.fillInStackTrace();
				log.error(e);
				throw e;
			}
		} else {
			SQLException e = new SQLException("The datasource returned a NULL CONNECTION for #" + connectionRequestCount);
			e.fillInStackTrace();
			log.error(e);
			throw e;
		}
		
		return c;
	}

	private Connection findConnection() throws SQLException {
		synchronized (this) {
			if (datasource == null && ! lookupFailed) {
				try {
					Context ctx = new InitialContext();
					datasource = (DataSource) ctx.lookup(dsName);
					//					Connection connection = ds.getConnection();

				} catch (Exception e) {
					lookupFailed = true;
				}
			}
			
			if (datasource != null) {
				return MonProxyFactory.monitor(datasource.getConnection());
			}
		}


		//if we fall through from above, fetching from cmd line does
		//not need to be sync'ed
		return getConnectionFromCommandLineParams();

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
	public Integer putPredictionContext(PredictionContext context) {

		try {
			context = context.clone();
		} catch (CloneNotSupportedException e) {
			// Shouldn't happen
			e.printStackTrace();
		}

		CacheManager cm = SparrowCacheManager.getInstance();

		// ===============
		// CAVEAT: The prediction context is placed in a distributed cache
		// across all the nodes in the cluster. All of its "children" also have
		// to be distributed.
		//
		// ===============
		Integer pcHash = context.hashCode();
		cm.getEhcache(PredictContext.name()).put( new Element(pcHash, context) );

		AdjustmentGroups ag = context.getAdjustmentGroups();
		if (ag != null) {
			Integer hash = ag.hashCode();
			cm.getEhcache(AdjustmentGroups.name()).put( new Element(hash, ag) );
		}

		Analysis anal = context.getAnalysis();
		if (anal != null) {
			Integer hash = anal.hashCode();
			cm.getEhcache(Analyses.name()).put( new Element(hash, anal) );
		}

		TerminalReaches tr = context.getTerminalReaches();
		if (tr != null) {
			Integer hash = context.getTerminalReaches().hashCode();
			cm.getEhcache(TerminalReaches.name()).put( new Element(hash, tr) );
		}

		AreaOfInterest aoi = context.getAreaOfInterest();
		if (aoi != null) {
			Integer hash = aoi.hashCode();
			cm.getEhcache(AreaOfInterest.name()).put( new Element(hash, aoi) );
		}

		return pcHash;
	}

	public PredictionContext getPredictionContext(Integer id) {
		return getPredictionContext(id, false);
	}

	public PredictionContext getPredictionContext(Integer id, boolean quiet) {

		Ehcache c = SparrowCacheManager.getInstance().getEhcache(PredictContext.name());
		Element e  = (quiet)?c.getQuiet(id):c.get(id);

		if (e == null) return null;

		PredictionContext pc = (PredictionContext) e.getObjectValue();

		//TODO [IK] Code here now assumes nulls are allowed, so PredictionContext code may need to change to match.
		// Populate transient children if necessary
		{
			AdjustmentGroups ags = pc.getAdjustmentGroups();
			Analysis analysis = pc.getAnalysis();
			TerminalReaches terminalReaches = pc.getTerminalReaches();
			AreaOfInterest aoi = pc.getAreaOfInterest();

			try {
				String retrievalMessage = "PredCtxt " + pc.getId() + " successfully retrieved %s %s : %s" ;
				if (ags == null && pc.getAdjustmentGroupsID() != null) {
					ags = getAdjustmentGroups(pc.getAdjustmentGroupsID());
					log.info(String.format(retrievalMessage, "adjGrp",pc.getAdjustmentGroupsID(),(ags != null)));
				} else if (ags != null){
					touchAdjustmentGroups(ags.getId());	//refresh in cache
					ags = ags.clone();
				}

				if (analysis == null && pc.getAnalysisID() != null) {
					analysis = getAnalysis(pc.getAnalysisID());
					log.info(String.format(retrievalMessage, "analysis ",pc.getAnalysisID(),(analysis != null)));
				} else if (analysis != null){
					touchAnalysis(analysis.getId());	//refresh in cache
					analysis = analysis.clone();
				}

				if (terminalReaches == null && pc.getTerminalReachesID() != null) {
					terminalReaches = getTerminalReaches(pc.getTerminalReachesID());
					log.info(String.format(retrievalMessage, "termReaches",pc.getTerminalReachesID(),(terminalReaches != null)));
				} else if (terminalReaches != null) {
					touchTerminalReaches(terminalReaches.getId());	//refresh in cache
					terminalReaches = terminalReaches.clone();
				}

				if (aoi == null && pc.getAreaOfInterestID() != null) {
					aoi = getAreaOfInterest(pc.getAreaOfInterestID());
					log.info(String.format(retrievalMessage, "areaOfInt",pc.getAreaOfInterestID(),(aoi != null)));
				} else if (aoi != null) {
					touchAreaOfInterest(aoi.getId());	//refresh in cache
					aoi = aoi.clone();
				}

				pc = pc.clone(ags, analysis, terminalReaches, aoi);


			} catch (CloneNotSupportedException e1) {
				log.error("Unexpected Clone not supported - returning null");
				pc = null;
			}
		}
		return pc;
	}


	//AdjustmentGroup Cache
	protected Integer putAdjustmentGroups(AdjustmentGroups adj) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AdjustmentGroups.name());
		int hash = adj.hashCode();
		c.put( new Element(hash, adj) );
		return hash;
	}

	protected boolean touchAdjustmentGroups(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AdjustmentGroups.name());
		Element e  = c.get(id);
		return (e != null);
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
	protected Integer putAnalysis(AdvancedAnalysis analysis) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(Analyses.name());
		int hash = analysis.hashCode();
		c.put( new Element(hash, analysis) );
		return hash;
	}

	protected boolean touchAnalysis(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(Analyses.name());
		Element e  = c.get(id);
		return (e != null);
	}

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
	protected Integer putTerminalReaches(TerminalReaches term) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(TerminalReaches.name());
		int hash = term.hashCode();
		c.put( new Element(hash, term) );
		return hash;
	}

	protected boolean touchTerminalReaches(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(TerminalReaches.name());
		Element e  = c.get(id);
		return (e != null);
	}

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
	protected Integer putAreaOfInterest(AreaOfInterest area) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AreaOfInterest.name());
		int hash = area.hashCode();
		c.put( new Element(hash, area) );
		return hash;
	}

	protected boolean touchAreaOfInterest(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AreaOfInterest.name());
		Element e  = c.get(id);
		return (e != null);
	}

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

	//PredefinedSessions Cache
	public List<IPredefinedSession> loadPredefinedSessions(Long modelId) {
		return loadPredefinedSessions(modelId, false);
	}

	@SuppressWarnings("unchecked")
	public List<IPredefinedSession> loadPredefinedSessions(Long modelId, boolean quiet) {
		return (List<IPredefinedSession>) PredefinedSessions.get(modelId, quiet);
	}
	
	public List<IPredefinedSession> getPredefinedSessions(PredefinedSessionRequest request) throws Exception {
		FilterPredefinedSessions action = new FilterPredefinedSessions(request);
		return action.run();
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
	public NSDataSet getNSDataSet(PredictionContext context) {
		return getNSDataSet(context, false);
	}

	public NSDataSet getNSDataSet(PredictionContext context, boolean quiet) {
		return (NSDataSet) NSDataSet.get(context, quiet);
	}


	//PredictData
	public PredictData getPredictData(Long id) {
		return getPredictData(id, false);
	}

	public PredictData getPredictData(Long id, boolean quiet) {
		return (PredictData) PredictData.get(id, quiet);
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
		Connection conn = getInstance().getConnection();
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

