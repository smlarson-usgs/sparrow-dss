package gov.usgswim.sparrow.service;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.cachefactory.AggregateIdLookupKludge;
import gov.usgswim.sparrow.cachefactory.BinningRequest;
import gov.usgswim.sparrow.cachefactory.ReachID;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.AdvancedAnalysis;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.LogicalSet;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
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
	public static final String NS_DATASET_CACHE = "NSDataSet";
	public static final String COMPARISON_RESULT_CACHE = "ComparisonResult";
	public static final String ANALYSIS_RESULT_CACHE = "AnalysisResult";
	public static final String PREDICT_DATA_CACHE = "PredictData";
	public static final String STANDARD_ERROR_ESTIMATE_DATA = "StandardErrorEstimateData";
	public static final String ADJUSTED_SOURCE_CACHE = "AdjustedSource";
	public static final String PREDICT_RESULT_CACHE = "PredictResult";
	public static final String IDENTIFY_REACH_BY_POINT = "IdentifyReachByPoint";
	public static final String IDENTIFY_REACH_BY_ID = "IdentifyReachByID";
	public static final String REACHES_BY_CRITERIA = "ReachesByCriteria";
	public static final String DATA_BINNING = "DataBinning";



	private SharedApplication() {

	}

	public static synchronized SharedApplication getInstance() {
		if (instance == null) {
			instance = new SharedApplication();
		}

		return instance;
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
			return datasource.getConnection();
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
		connection = DriverManager.getConnection(url, dbuser, dbpass);
		return connection;

	}

	public Integer putSerializable(Serializable context) {
		Ehcache c = CacheManager.getInstance().getEhcache(SERIALIZABLE_CACHE);
		Integer hash = context.hashCode();
		c.put( new Element(hash, context) );
		return hash;
	}

	public Serializable getSerializable(Integer id) {
		return getSerializable(id, false);
	}

	public Serializable getSerializable(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(SERIALIZABLE_CACHE);
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

		CacheManager cm = CacheManager.getInstance();

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

		Ehcache c = CacheManager.getInstance().getEhcache(PREDICT_CONTEXT_CACHE);
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

				if (ags == null && pc.getAdjustmentGroupsID() != null) {
					ags = getAdjustmentGroups(pc.getAdjustmentGroupsID());
				} else if (ags != null){
					touchAdjustmentGroups(ags.getId());	//refresh in cache
					ags = ags.clone();
				}

				if (analysis == null && pc.getAnalysisID() != null) {
					analysis = getAnalysis(pc.getAnalysisID());
				} else if (analysis != null){
					touchAnalysis(analysis.getId());	//refresh in cache
					analysis = analysis.clone();
				}

				if (terminalReaches == null && pc.getTerminalReachesID() != null) {
					terminalReaches = getTerminalReaches(pc.getTerminalReachesID());
				} else if (terminalReaches != null) {
					touchTerminalReaches(terminalReaches.getId());	//refresh in cache
					terminalReaches = terminalReaches.clone();
				}

				if (aoi == null && pc.getAreaOfInterestID() != null) {
					aoi = getAreaOfInterest(pc.getAreaOfInterestID());
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
		Ehcache c = CacheManager.getInstance().getEhcache(ADJUSTMENT_GROUPS_CACHE);
		int hash = adj.hashCode();
		c.put( new Element(hash, adj) );
		return hash;
	}

	protected boolean touchAdjustmentGroups(Integer id) {
		Ehcache c = CacheManager.getInstance().getEhcache(ADJUSTMENT_GROUPS_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public AdjustmentGroups getAdjustmentGroups(Integer id) {
		return getAdjustmentGroups(id, false);
	}

	public AdjustmentGroups getAdjustmentGroups(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(ADJUSTMENT_GROUPS_CACHE);
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
		Ehcache c = CacheManager.getInstance().getEhcache(ANALYSES_CACHE);
		int hash = analysis.hashCode();
		c.put( new Element(hash, analysis) );
		return hash;
	}

	protected boolean touchAnalysis(Integer id) {
		Ehcache c = CacheManager.getInstance().getEhcache(ANALYSES_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public Analysis getAnalysis(Integer id) {
		return getAnalysis(id, false);
	}

	public Analysis getAnalysis(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(ANALYSES_CACHE);
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
		Ehcache c = CacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
		int hash = term.hashCode();
		c.put( new Element(hash, term) );
		return hash;
	}

	protected boolean touchTerminalReaches(Integer id) {
		Ehcache c = CacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public TerminalReaches getTerminalReaches(Integer id) {
		return getTerminalReaches(id, false);
	}

	public TerminalReaches getTerminalReaches(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(TERMINAL_REACHES_CACHE);
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
		Ehcache c = CacheManager.getInstance().getEhcache(AREA_OF_INTEREST_CACHE);
		int hash = area.hashCode();
		c.put( new Element(hash, area) );
		return hash;
	}

	protected boolean touchAreaOfInterest(Integer id) {
		Ehcache c = CacheManager.getInstance().getEhcache(AREA_OF_INTEREST_CACHE);
		Element e  = c.get(id);
		return (e != null);
	}

	public AreaOfInterest getAreaOfInterest(Integer id) {
		return getAreaOfInterest(id, false);
	}

	public AreaOfInterest getAreaOfInterest(Integer id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(AREA_OF_INTEREST_CACHE);
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
		Ehcache c = CacheManager.getInstance().getEhcache(NS_DATASET_CACHE);
		Element e  = (quiet)?c.getQuiet(context):c.get(context);
		return (e != null)?((NSDataSet) e.getObjectValue()):null;
	}
	
	
	//PredictData Cache
	public PredictData getPredictData(Long id) {
		return getPredictData(id, false);
	}

	public PredictData getPredictData(Long id, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(PREDICT_DATA_CACHE);
		Element e  = (quiet)?c.getQuiet(id):c.get(id);
		return (e != null)?((PredictData) e.getObjectValue()):null;
	}
	
	//Uncertainty Data
	public UncertaintyData getStandardErrorEstimateData(UncertaintyDataRequest req) {
		return getStandardErrorEstimateData(req, false);
	}
	
	public UncertaintyData getStandardErrorEstimateData(UncertaintyDataRequest req, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(STANDARD_ERROR_ESTIMATE_DATA);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((UncertaintyData) e.getObjectValue()):null;
	}

	//PredictResult Cache
	public PredictResult getPredictResult(PredictionContext context) {
		return getPredictResult(context, false);
	}

	public PredictResult getPredictResult(PredictionContext context, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(PREDICT_RESULT_CACHE);
		Element e  = (quiet)?c.getQuiet(context):c.get(context);
		return (e != null)?((PredictResult) e.getObjectValue()):null;
	}
	
	//ComparisonResult Cache
	public PredictionContext.DataColumn getComparisonResult(PredictionContext context) {
		return getComparisonResult(context, false);
	}

	public PredictionContext.DataColumn getComparisonResult(PredictionContext context, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(COMPARISON_RESULT_CACHE);
		Element e  = (quiet)?c.getQuiet(context):c.get(context);
		return (e != null)?((PredictionContext.DataColumn) e.getObjectValue()):null;
	}

	//AnalysisResult Cache
	public PredictionContext.DataColumn getAnalysisResult(PredictionContext context) {
		return getAnalysisResult(context, false);
	}

	public PredictionContext.DataColumn getAnalysisResult(PredictionContext context, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(ANALYSIS_RESULT_CACHE);
		Element e  = (quiet)?c.getQuiet(context):c.get(context);
		return (e != null)?((PredictionContext.DataColumn) e.getObjectValue()):null;
	}

	//ReachByPoint Cache
	public ReachInfo getReachByPointResult(ModelPoint req) {
		return getReachByPointResult(req, false);
	}

	public ReachInfo getReachByPointResult(ModelPoint req, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(IDENTIFY_REACH_BY_POINT);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((ReachInfo) e.getObjectValue()):null;
	}

	//ReachByID Cache
	public ReachInfo getReachByIDResult(ReachID req) {
		return getReachByIDResult(req, false);
	}

	public ReachInfo getReachByIDResult(ReachID req, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(IDENTIFY_REACH_BY_ID);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((ReachInfo) e.getObjectValue()):null;
	}

	//Adjusted Source Cache
	public DataTable getAdjustedSource(AdjustmentGroups req) {
		return getAdjustedSource(req, false);
	}

	public DataTable getAdjustedSource(AdjustmentGroups req, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(ADJUSTED_SOURCE_CACHE);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((DataTable) e.getObjectValue()):null;
	}

	//Adjusted Source Cache
	public List<Long> getReachesByCriteria(LogicalSet req) {
		return getReachesByCriteria(req, false);
	}

	@SuppressWarnings("unchecked")
	public List<Long> getReachesByCriteria(LogicalSet req, boolean quiet) {
		Ehcache c = CacheManager.getInstance().getEhcache(REACHES_BY_CRITERIA);
		Element e  = (quiet)?c.getQuiet(req):c.get(req);
		return (e != null)?((List<Long>) e.getObjectValue()):null;
	}

    //Data Binning Cache
    public BigDecimal[] getDataBinning(BinningRequest req) {
        return getDataBinning(req, false);
    }

    public BigDecimal[] getDataBinning(BinningRequest req, boolean quiet) {
        Ehcache c = CacheManager.getInstance().getEhcache(DATA_BINNING);
        Element e  = (quiet)?c.getQuiet(req):c.get(req);
        return (e != null)?((BigDecimal[]) e.getObjectValue()):null;
    }

    //Aggregate Id Lookup Kludge Cache - temporary
    public AggregateIdLookupKludge getAggregateIdLookup(String aggLevel) {
        return getAggregateIdLookup(aggLevel, false);
    }

    public AggregateIdLookupKludge getAggregateIdLookup(String aggLevel, boolean quiet) {
        Ehcache c = CacheManager.getInstance().getEhcache("AggregateIdLookup");
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

