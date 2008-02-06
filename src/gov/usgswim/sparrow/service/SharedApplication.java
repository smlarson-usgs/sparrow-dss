package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.PredictComputable;
import gov.usgswim.task.ComputableCache;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.domain.ModelImm;
import gov.usgswim.sparrow.util.DataSourceProxy;
import gov.usgswim.sparrow.util.JDBCConnectable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;

import javax.sql.DataSource;

import oracle.jdbc.driver.OracleDriver;


public class SharedApplication extends DataSourceProxy implements JDBCConnectable {
	private static SharedApplication instance;
	private String dsName = "jdbc/sparrowDSDS";
	private DataSource datasource;
	private boolean lookupFailed = false;
	private ComputableCache<PredictRequest, PredictResult> predictResultCache;
	private ComputableCache<Long, PredictData> predictDatasetCache;	//Long is the Model ID
	private ComputableCache<IDByPointRequest, Int2DImm> idByPointCache;
	private ComputableCache<ModelRequest, ModelImm> modelCache;
	
	
	private SharedApplication() {
		super(null);
		
		predictResultCache = new ComputableCache<PredictRequest, PredictResult>(new PredictComputable());
		predictDatasetCache = new ComputableCache<Long, PredictData>(new PredictDatasetComputable());
		idByPointCache = new ComputableCache<IDByPointRequest, Int2DImm>(new IDByPointComputable());
		modelCache = new ComputableCache<ModelRequest, ModelImm>(new ModelComputable());
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

	public ComputableCache<PredictRequest, PredictResult> getPredictResultCache() {
		return predictResultCache;
	}

	public ComputableCache<Long, PredictData> getPredictDatasetCache() {
		return predictDatasetCache;
	}
	
	public ComputableCache<IDByPointRequest, Int2DImm> getIdByPointCache() {
		return idByPointCache;
	}

	public ComputableCache<ModelRequest, ModelImm> getModelCache() {
		return modelCache;
	}
}
