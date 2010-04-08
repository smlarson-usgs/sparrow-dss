package gov.usgswim.sparrow.service;

import edu.emory.mathcs.backport.java.util.Arrays;
import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.cachefactory.AggregateIdLookupKludge;
import gov.usgswim.sparrow.cachefactory.BinningRequest;
import gov.usgswim.sparrow.cachefactory.ModelRequestCacheKey;
import gov.usgswim.sparrow.cachefactory.ReachID;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.AdvancedAnalysis;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.LogicalSet;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import java.awt.geom.Point2D.Double;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import oracle.jdbc.driver.OracleDriver;
import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.log4j.Logger;
import com.jamonapi.proxy.MonProxyFactory;

//TODO:  This class contains a lot of unused caches...
public class SharedApplication  {
	protected static Logger log =
		Logger.getLogger(SharedApplication.class); //logging for this class

	private static SharedApplication instance;
	// private String dsName = "jdbc/sparrowDSDS"; old value
	private static final String dsName = "java:comp/env/jdbc/sparrow_dss";
	private DataSource datasource;
	private boolean lookupFailed = false;

	//an ehcache test cache
	public static final String SERIALIZABLE_CACHE = "PredictContext";

	//ehcache bean cache names
	public static final String PREDICT_CONTEXT_CACHE = "PredictContext";
	public static final String ADJUSTMENT_GROUPS_CACHE = "AdjustmentGroups";
	public static final String ANALYSES_CACHE = "Analyses";
	public static final String TERMINAL_REACHES_CACHE = "TerminalReaches";
	public static final String AREA_OF_INTEREST_CACHE = "AreaOfInterest";


	//ehcache self-populated cache names
	public static final String PREDICT_DATA_CACHE = "PredictData";
	public static final String DELIVERY_FRACTION_CACHE = "DeliveryFraction";
	public static final String NS_DATASET_CACHE = "NSDataSet";
	public static final String COMPARISON_RESULT_CACHE = "ComparisonResult";
	public static final String ANALYSIS_RESULT_CACHE = "AnalysisResult";
	public static final String STANDARD_ERROR_ESTIMATE_DATA = "StandardErrorEstimateData";
	public static final String ADJUSTED_SOURCE_CACHE = "AdjustedSource";
	public static final String PREDICT_RESULT_CACHE = "PredictResult";
	public static final String IDENTIFY_REACH_BY_POINT = "IdentifyReachByPoint";
	public static final String IDENTIFY_REACH_BY_ID = "IdentifyReachByID";
	public static final String LOAD_REACH_ATTRIBUTES = "LoadReachAttributes";
	public static final String REACHES_BY_CRITERIA = "ReachesByCriteria";
	public static final String DATA_BINNING = "DataBinning";
	public static final String AGGREGATED_ID_LOOKUP = "AggregateIdLookup";
	public static final String LOAD_MODEL_METADATA = "LoadModelMetadata";

	// This list is based on ehcache.xml configuration. Updating this list requires updating the ehcache.xml configuration as well.
	// Consider doing this with enums, later
	public static final String[] DISTRIBUTED_CACHES = {PREDICT_CONTEXT_CACHE, ADJUSTMENT_GROUPS_CACHE, ANALYSES_CACHE, TERMINAL_REACHES_CACHE, AREA_OF_INTEREST_CACHE};
	public static final Class<?> DISTRIBUTED_CACHE_KEY_TYPE = Integer.class;
	public static final String[] ALL_CACHES = {PREDICT_CONTEXT_CACHE, ADJUSTMENT_GROUPS_CACHE, ANALYSES_CACHE, TERMINAL_REACHES_CACHE, AREA_OF_INTEREST_CACHE, 
		DELIVERY_FRACTION_CACHE, NS_DATASET_CACHE, COMPARISON_RESULT_CACHE, ANALYSIS_RESULT_CACHE, STANDARD_ERROR_ESTIMATE_DATA,
		ADJUSTED_SOURCE_CACHE, PREDICT_RESULT_CACHE, IDENTIFY_REACH_BY_POINT, IDENTIFY_REACH_BY_ID,
		LOAD_REACH_ATTRIBUTES, REACHES_BY_CRITERIA, DATA_BINNING, LOAD_MODEL_METADATA};


	private SharedApplication() {

	}

	public static synchronized SharedApplication getInstance() {
		if (instance == null) {
			instance = new SharedApplication();
		}

		return instance;
	}

	public static List getCacheKeys(String cacheName) {
		try {
			Ehcache c = SparrowCacheManager.getInstance().getEhcache(cacheName);
			return c.getKeys();
		} catch (Exception e) {
			System.err.println("Could not obtain a list of keys");
		}
		return null;
	}

	/**
	 * Returns HTML text string showing the state of the cache, useful for debugging purposes
	 * @param showDetails
	 * @return
	 * TODO HTML stuff doesn't really belong in this class, but there isn't an ideal place to put it, so I'm leaving it here for now.
	 */
	public static StringBuilder listDistributedCacheStatus(boolean showDetails) {
		StringBuilder result = new StringBuilder();
		// checking 
		DynamicReadOnlyProperties sparrowProps = SparrowCacheManager.getProperties();
		String configKey = "cacheManagerPeerProviderFactory.properties";
		result.append("CONFIG: " + configKey + " = " + sparrowProps.get(configKey) + "\n\n");

		
		String HEADER_FORMAT = "<font color='%s'>%s</font> ( %s objects of %s) %s %s ; transactions %s\n";
		String MEMORY_STATS_FORMAT = "\t  *memory used: %s bytes; memory store: %s; eviction policy: %s; time to live: %s; time to idle: %s;\n";
		String CLUSTER_STATS_FORMAT = "\t  *cluster coherent: %s; node coherent: %s\n";
		String HITS_AND_MISSES_FORMAT = "\t  *total HITS = mem + disk : %s = %s + %s; expired/total MISSES: %s/%s;\n";
		String REMOVALS_FORMAT = "\t  *updates/puts/removed/expired/evicted: %s/%s/%s/%s/%s\n";
		String PERFORMANCE_FORMAT = "\t  *average/min/max: %s/%s/%s\n";

		String ENTRY_FORMAT = "\t%s: %s\n";
		Set<String> distributedCaches = new HashSet<String>();
		distributedCaches.addAll(Arrays.asList(DISTRIBUTED_CACHES));
		for (String cache: ALL_CACHES) {
			try {
				Ehcache c = SparrowCacheManager.getInstance().getEhcache(cache);
				CacheConfiguration config = c.getCacheConfiguration();

				List allKeys = c.getKeys();
				List liveKeys = c.getKeysWithExpiryCheck();

				// output cache header
				String memorySizeInKB = "";
				if (!showDetails) {
					long memSize = c.calculateInMemorySize();
					memorySizeInKB = Long.toString(memSize >>> 10) + " Kb";
				}
				String displayColor = "black";
				if (distributedCaches.contains(cache)) {
					displayColor = "green";
				}
				String cacheHeader = String.format(HEADER_FORMAT, 
						displayColor,
						cache,
						allKeys.size(), 
						config.getMaxElementsInMemory(),
						memorySizeInKB,
						(distributedCaches.contains(cache)) ? "distributed": "", 
								config.getTransactionalMode());
				result.append(cacheHeader);

				if (showDetails) {
					// output memory stats
					String cacheMemoryStats = String.format(MEMORY_STATS_FORMAT,
							c.calculateInMemorySize(), 
							c.getMemoryStoreSize(),
							config.getMemoryStoreEvictionPolicy(),
							config.getTimeToLiveSeconds(),
							config.getTimeToIdleSeconds());
					result.append(cacheMemoryStats);
					// output cluster state
					String clusterState = String.format(CLUSTER_STATS_FORMAT,
							c.calculateInMemorySize(), 
							c.getMemoryStoreSize(),
							config.getMemoryStoreEvictionPolicy(),
							config.getTimeToLiveSeconds(),
							config.getTimeToIdleSeconds());
					result.append(clusterState);
					// output Live stats
					LiveCacheStatistics liveStats = c.getLiveCacheStatistics();
					if (liveStats != null && liveStats.isStatisticsEnabled()) {
						String hitsAndMisses = String.format(HITS_AND_MISSES_FORMAT, 
								liveStats.getCacheHitCount(),
								liveStats.getInMemoryHitCount(),
								liveStats.getOnDiskHitCount(),
								liveStats.getCacheMissCountExpired(),
								liveStats.getCacheMissCount()
						);
						result.append(hitsAndMisses);

						String removalStats = String.format(REMOVALS_FORMAT, 
								liveStats.getUpdateCount(),
								liveStats.getPutCount(),
								liveStats.getRemovedCount(),
								liveStats.getExpiredCount(),
								liveStats.getEvictedCount()
						);
						result.append(removalStats);
						String perfStats = String.format(PERFORMANCE_FORMAT, 
								liveStats.getAverageGetTimeMillis(),
								liveStats.getMinGetTimeMillis(),
								liveStats.getMaxGetTimeMillis()
						);
						result.append(perfStats);

					}
				}


				// output cache contents
				for (Object key: allKeys) {
					result.append(String.format(ENTRY_FORMAT, key2String(key), (liveKeys.contains(key))? "live": "expired"));
				}
			} catch (Exception e) {
				System.err.println("Unable to print distributed cache status");
			}
		}
		return result;
	}

	public static String key2String(Object key) {
		if (key instanceof Number) {
			return key.toString();
		} else if (key instanceof PredictionContext) {
			PredictionContext context = (PredictionContext) key;
			return context.getId().toString();
		} else if (key instanceof TerminalReaches) {
			TerminalReaches reaches = (TerminalReaches) key;
			return reaches.getId().toString();
		} else if (key instanceof UncertaintyDataRequest) {
			UncertaintyDataRequest req = (UncertaintyDataRequest) key;
			return UncertaintyDataRequest.class.getSimpleName() + "_M" + req.getModelId() + "_S" + req.getSourceId();
		} else if (key instanceof AdjustmentGroups) {
			AdjustmentGroups adj = (AdjustmentGroups) key;
			return adj.getId().toString();
		} else if (key instanceof ModelPoint) {
			Double point = ((ModelPoint) key).getPoint();
			return "(" + point.x + ", " + point.y + ")";
		} else if (key instanceof ReachID) {
			ReachID reach = (ReachID) key;
			return "reach_" + reach.getReachID();
		} else if (key instanceof ModelPoint) {
			Double point = ((ModelPoint) key).getPoint();
			return "(" + point.x + ", " + point.y + ")";
		}


		return key.toString();
	}

	public Connection getConnection() throws SQLException {
		return findConnection();
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
		}


		if (datasource != null) {
			return MonProxyFactory.monitor(datasource.getConnection());
		}
		return getConnectionFromCommandLineParams();

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
		cm.getEhcache(PREDICT_CONTEXT_CACHE).put( new Element(pcHash, context) );

		AdjustmentGroups ag = context.getAdjustmentGroups();
		if (ag != null) {
			Integer hash = ag.hashCode();
			cm.getEhcache(ADJUSTMENT_GROUPS_CACHE).put( new Element(hash, ag) );
		}

		Analysis anal = context.getAnalysis();
		if (anal != null) {
			Integer hash = anal.hashCode();
			cm.getEhcache(ANALYSES_CACHE).put( new Element(hash, anal) );
		}

		TerminalReaches tr = context.getTerminalReaches();
		if (tr != null) {
			Integer hash = context.getTerminalReaches().hashCode();
			cm.getEhcache(TERMINAL_REACHES_CACHE).put( new Element(hash, tr) );
		}

		AreaOfInterest aoi = context.getAreaOfInterest();
		if (aoi != null) {
			Integer hash = aoi.hashCode();
			cm.getEhcache(AREA_OF_INTEREST_CACHE).put( new Element(hash, aoi) );
		}

		return pcHash;
	}

	public PredictionContext getPredictionContext(Integer id) {
		return getPredictionContext(id, false);
	}

	public PredictionContext getPredictionContext(Integer id, boolean quiet) {

		Ehcache c = SparrowCacheManager.getInstance().getEhcache(PREDICT_CONTEXT_CACHE);
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
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ADJUSTMENT_GROUPS_CACHE);
		int hash = adj.hashCode();
		c.put( new Element(hash, adj) );
		return hash;
	}

	protected boolean touchAdjustmentGroups(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ADJUSTMENT_GROUPS_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public AdjustmentGroups getAdjustmentGroups(Integer id) {
		return getAdjustmentGroups(id, false);
	}

	public AdjustmentGroups getAdjustmentGroups(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ADJUSTMENT_GROUPS_CACHE);
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
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ANALYSES_CACHE);
		int hash = analysis.hashCode();
		c.put( new Element(hash, analysis) );
		return hash;
	}

	protected boolean touchAnalysis(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ANALYSES_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public Analysis getAnalysis(Integer id) {
		return getAnalysis(id, false);
	}

	public Analysis getAnalysis(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ANALYSES_CACHE);
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
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
		int hash = term.hashCode();
		c.put( new Element(hash, term) );
		return hash;
	}

	protected boolean touchTerminalReaches(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public TerminalReaches getTerminalReaches(Integer id) {
		return getTerminalReaches(id, false);
	}

	public TerminalReaches getTerminalReaches(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
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
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AREA_OF_INTEREST_CACHE);
		int hash = area.hashCode();
		c.put( new Element(hash, area) );
		return hash;
	}

	protected boolean touchAreaOfInterest(Integer id) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AREA_OF_INTEREST_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public AreaOfInterest getAreaOfInterest(Integer id) {
		return getAreaOfInterest(id, false);
	}

	public AreaOfInterest getAreaOfInterest(Integer id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(AREA_OF_INTEREST_CACHE);
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


	//NSDataSet Cache
	public NSDataSet getNSDataSet(PredictionContext context) {
		return getNSDataSet(context, false);
	}

	public NSDataSet getNSDataSet(PredictionContext context, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(NS_DATASET_CACHE);
		Element e  = (quiet)?c.getQuiet(context):c.get(context);
		return (e != null)?((NSDataSet) e.getObjectValue()):null;
	}


	//PredictData
	public PredictData getPredictData(Long id) {
		return getPredictData(id, false);
	}

	public PredictData getPredictData(Long id, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(PREDICT_DATA_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((PredictData) e.getObjectValue()):null;
	}

	//DeliveryFraction
	public ColumnData getDeliveryFraction(TerminalReaches targets) {
		return getDeliveryFraction(targets, false);
	}

	public ColumnData getDeliveryFraction(TerminalReaches targets, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(DELIVERY_FRACTION_CACHE);
		Element e  = (quiet)?c.getQuiet(targets):c.get(targets);
		return (e != null)?((ColumnData) e.getObjectValue()):null;
	}

	//Uncertainty Data
	public UncertaintyData getStandardErrorEstimateData(UncertaintyDataRequest req) {
		return getStandardErrorEstimateData(req, false);
	}

	public UncertaintyData getStandardErrorEstimateData(UncertaintyDataRequest req, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(STANDARD_ERROR_ESTIMATE_DATA);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((UncertaintyData) e.getObjectValue()):null;
	}

	//PredictResult Cache
	public PredictResult getPredictResult(AdjustmentGroups adjustments) {
		return getPredictResult(adjustments, false);
	}

	public PredictResult getPredictResult(AdjustmentGroups adjustments, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(PREDICT_RESULT_CACHE);
		Element e  = (quiet)?c.getQuiet(adjustments):c.get(adjustments);
		return (e != null)?((PredictResult) e.getObjectValue()):null;
	}

	//ComparisonResult Cache
	public DataColumn getComparisonResult(PredictionContext context) {
		return getComparisonResult(context, false);
	}

	public DataColumn getComparisonResult(PredictionContext context, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(COMPARISON_RESULT_CACHE);
		Element e  = (quiet)?c.getQuiet(context):c.get(context);
		return (e != null)?((DataColumn) e.getObjectValue()):null;
	}

	//AnalysisResult Cache
	public DataColumn getAnalysisResult(PredictionContext context) {
		return getAnalysisResult(context, false);
	}

	public DataColumn getAnalysisResult(PredictionContext context, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ANALYSIS_RESULT_CACHE);
		Element e  = (quiet)?c.getQuiet(context):c.get(context);
		return (e != null)?((DataColumn) e.getObjectValue()):null;
	}

	//ReachByPoint Cache
	public ReachInfo getReachByPointResult(ModelPoint req) {
		return getReachByPointResult(req, false);
	}

	public ReachInfo getReachByPointResult(ModelPoint req, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(IDENTIFY_REACH_BY_POINT);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((ReachInfo) e.getObjectValue()):null;
	}

	//ReachByID Cache
	public ReachInfo getReachByIDResult(ReachID req) {
		return getReachByIDResult(req, false);
	}

	public ReachInfo getReachByIDResult(ReachID req, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(IDENTIFY_REACH_BY_ID);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((ReachInfo) e.getObjectValue()):null;
	}

	//LoadReachAttributes
	public DataTable getReachAttributes(ReachID req) {
		return getReachAttributes(req, false);
	}

	public DataTable getReachAttributes(ReachID req, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(LOAD_REACH_ATTRIBUTES);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((DataTable) e.getObjectValue()):null;
	}

	//LoadModelMetadata
	public List<SparrowModel> getModelMetadata(ModelRequestCacheKey req) {
		return getModelMetadata(req, false);
	}

	@SuppressWarnings("unchecked")
	public List<SparrowModel> getModelMetadata(ModelRequestCacheKey req, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(LOAD_MODEL_METADATA);
		System.out.println("DEBUG: class = " + c.getClass());
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((List<SparrowModel>) e.getObjectValue()):null;
	}

	//Adjusted Source Cache
	public DataTable getAdjustedSource(AdjustmentGroups req) {
		return getAdjustedSource(req, false);
	}

	public DataTable getAdjustedSource(AdjustmentGroups req, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(ADJUSTED_SOURCE_CACHE);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((DataTable) e.getObjectValue()):null;
	}

	//Adjusted Source Cache
	public List<Long> getReachesByCriteria(LogicalSet req) {
		return getReachesByCriteria(req, false);
	}

	@SuppressWarnings("unchecked")
	public List<Long> getReachesByCriteria(LogicalSet req, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(REACHES_BY_CRITERIA);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((List<Long>) e.getObjectValue()):null;
	}

	//Data Binning Cache
	public BigDecimal[] getDataBinning(BinningRequest req) {
		return getDataBinning(req, false);
	}

	public BigDecimal[] getDataBinning(BinningRequest req, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(DATA_BINNING);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((BigDecimal[]) e.getObjectValue()):null;
	}

	//Aggregate Id Lookup Kludge Cache - temporary
	public AggregateIdLookupKludge getAggregateIdLookup(String aggLevel) {
		return getAggregateIdLookup(aggLevel, false);
	}

	public AggregateIdLookupKludge getAggregateIdLookup(String aggLevel, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache("AggregateIdLookup");
		Element e  = (quiet) ? c.getQuiet(aggLevel) : c.get(aggLevel);
		return (e != null)?((AggregateIdLookupKludge) e.getObjectValue()):null;
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

