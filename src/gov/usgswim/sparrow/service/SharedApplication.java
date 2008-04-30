package gov.usgswim.sparrow.service;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictComputable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.domain.ModelImm;
import gov.usgswim.sparrow.util.DataSourceProxy;
import gov.usgswim.sparrow.util.JDBCConnectable;
import gov.usgswim.task.ComputableCache;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import oracle.jdbc.driver.OracleDriver;

public class SharedApplication extends DataSourceProxy implements JDBCConnectable {
	private static SharedApplication instance;
	private String dsName = "jdbc/sparrowDSDS";
	private DataSource datasource;
	private boolean lookupFailed = false;
	private ComputableCache<PredictRequest, PredictResult> predictResultCache;
	private ComputableCache<Long, PredictData> predictDatasetCache;	//Long is the Model ID
	private ComputableCache<IDByPointRequest, DataTable> idByPointCache;
	private ComputableCache<ModelRequest, ModelImm> modelCache;

	public static final String PREDICT_CONTEXT_CACHE = "predictContext";
	
	
	private SharedApplication() {
		super(null);

		predictResultCache = new ComputableCache<PredictRequest, PredictResult>(new PredictComputable(), "Predict Result Cache");
		predictDatasetCache = new ComputableCache<Long, PredictData>(new PredictDatasetComputable(), "Predict Dataset Cache");
		idByPointCache = new ComputableCache<IDByPointRequest, DataTable>(new IDByPointComputable(), "ID by Point Cache");
		modelCache = new ComputableCache<ModelRequest, ModelImm>(new ModelComputable(), "Model Cache");
		
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

	public Connection getConnection(String username, String password)
	throws SQLException {

		return findConnection();
	}

	private Connection findConnection() throws SQLException {
		synchronized (this) {
			if (datasource == null && ! lookupFailed) {
				try {
					InitialContext context = new InitialContext();
					datasource = (DataSource)context.lookup(dsName);
				} catch (Exception e) {
					lookupFailed = true;
				}
			}
		}


		if (datasource != null) {
			return datasource.getConnection();
		} else {
			return getDirectConnection();
		}

	}

	private Connection getDirectConnection() throws SQLException {
		synchronized (this) {
			DriverManager.registerDriver(new OracleDriver());
		}

		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";

		return DriverManager.getConnection(thinConn,username,password);
	}

	public Integer putPredictContext(Serializable context) {
		Cache c = CacheManager.getInstance().getCache(PREDICT_CONTEXT_CACHE);
		int hash = context.hashCode();
		c.put( new Element(hash, context) );
		return hash;
	}
	
	public Serializable getPredictContext(Integer id) {
		return getPredictContext(id, false);
	}
	
	public Serializable getPredictContext(Integer id, boolean quiet) {
		Cache c = CacheManager.getInstance().getCache(PREDICT_CONTEXT_CACHE);
		Element e = null;
		
		e = (quiet)?c.getQuiet(id):c.get(id);

		return (e != null)?((Serializable) e.getObjectValue()):null;
	}
	
	public ComputableCache<PredictRequest, PredictResult> getPredictResultCache() {
		return predictResultCache;
	}

	public ComputableCache<Long, PredictData> getPredictDatasetCache() {
		return predictDatasetCache;
	}

	public ComputableCache<IDByPointRequest, DataTable> getIdByPointCache() {
		return idByPointCache;
	}

	public ComputableCache<ModelRequest, ModelImm> getModelCache() {
		return modelCache;
	}
}

