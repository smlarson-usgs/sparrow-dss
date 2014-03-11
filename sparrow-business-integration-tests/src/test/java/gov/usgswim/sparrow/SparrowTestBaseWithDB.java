/**
 * 
 */
package gov.usgswim.sparrow;

import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.action.PredictionContextHandler;
import gov.usgswim.sparrow.action.PredictionContextHandler;
import gov.usgswim.sparrow.cachefactory.PredictDataFactory;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

/**
 * A base test class that sets properties needed for a db connection, cache
 * configuration and other application lifecycle aspects.
 * 
 * Crazy Aspects:
 * JUnit has some nasty features.  There are BeforeClass & AfterClass annotations,
 * however, its not possible to allow these methods to be overridden so there
 * are places where this doesn't work for us (in particular, one aspect of the
 * lifecycle cannot be used for Services so it needs to be overridden by the
 * SparrowServiceTest class).  I tried using a 'firstRun' flag to simualte this
 * feature, however, firstrun is always reset to true because another JUnit
 * 'feature' is to create a new instance for every test.  Thus, all instance
 * variables are always reset.
 * 
 * So...  This class uses static variables as if they were instance variables.
 * This *should* work as long as JUnit is single threaded in running the tests
 * (a requirement that I think is pretty safe).
 * 
 * @author eeverman
 *
 */
public abstract class SparrowTestBaseWithDB extends SparrowTestBase {
	
	/** A connection, shared for this class and autoclosed */
	private static Connection sparrowDBTestConn;

	
	/** A single instance which is destroyed in teardown */
	private static SparrowTestBaseWithDB singleInstanceToTearDown;
	
	
	/**
	 * Override to return true if your test should use the file based predict
	 * data instead of the database.  If set to true, only data for model 50 will
	 * be available.
	 * @return
	 */
	public boolean loadModelDataFromFile() {
		return false;
	}
	
	/**
	 * If this method returns true, db access for PredictionContexts is turned
	 * off, resulting in only the local memory cache being used.  Unless testing
	 * is being done of the PC storage system itself, it should be OK in all
	 * cases to let this be disabled.
	 * @return
	 */
	public boolean disablePredictionContextPersistentStorage() {
		return true;
	}

	@Override
	public void doOneTimeFrameworkSetup() throws Exception {
		
		if (!loadModelDataFromFile()) {
			//Explicitly force a load from the db, not from CSV or serialization file.
			SharedApplication.getInstance().getConfiguration().setProperty(
							PredictDataFactory.ACTION_IMPLEMENTATION_CLASS,
							"gov.usgswim.sparrow.action.LoadModelPredictData");
		}
		
		
		if (disablePredictionContextPersistentStorage()) {
			//turn of db access
			SharedApplication.getInstance().getConfiguration().setProperty(PredictionContextHandler.DISABLE_DB_ACCESS, "true");
		} else {
			//Clear to allow the PredictionContext to be stored to persistent storage
			SharedApplication.getInstance().getConfiguration().setProperty(PredictionContextHandler.DISABLE_DB_ACCESS, "false");
		}
		
		
		doDbSetup();
		singleInstanceToTearDown = this;
	}
	
	@Override
	public void doOneTimeFrameworkTearDown() throws Exception {
		singleInstanceToTearDown.doDbTearDown();
		singleInstanceToTearDown = null;
		SharedApplication.getInstance().reloadConfiguration();
	}
	
	protected void doDbSetup() throws Exception {
		
		//OK to set these props each time
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
			"org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, 
			"org.apache.naming");   


		Context ctx = new InitialContext();
		DataSource testRoDs = null;
		DataSource testRwDs = null;
		
		try {
			testRoDs = (DataSource) ctx.lookup(SharedApplication.READ_ONLY_JNDI_DS_NAME);
			testRwDs = (DataSource) ctx.lookup(SharedApplication.READ_WRITE_JNDI_DS_NAME);
		} catch (Exception e) {
			//Ignore
		}
		
		if (testRoDs == null) {
			OracleDataSource ds = new OracleDataSource(); 
			
			ds.setConnectionCachingEnabled(true);
			ds.setURL("jdbc:oracle:thin:@130.11.165.154:1521:widev");
			ds.setUser("sparrow_dss");
			ds.setPassword("replaced");

			initDataSource(ds, ctx, "sparrow_dss");
		}
		
		if (testRwDs == null) {
			OracleDataSource ds = new OracleDataSource(); 
			
			ds.setConnectionCachingEnabled(true);

			//ALWAYS USE THE TEST DB FOR TRANSACTIONAL SUPPORT.
			//130.11.165.154
			//igsarmewdbdev.er.usgs.gov
			ds.setURL("jdbc:oracle:thin:@130.11.165.154:1521:widev");
			ds.setUser("sparrow_dss");
			ds.setPassword("replaced");

			initDataSource(ds, ctx, "sparrow_dss_trans");
		}
	}
	
	protected void initDataSource(OracleDataSource ds, Context ctx, String localName) throws Exception {

		
		//Set implicite cache properties
        Properties cacheProps = new Properties();
        cacheProps.setProperty("MinLimit", "0");
        cacheProps.setProperty("MaxLimit", "3"); 
        cacheProps.setProperty("InitialLimit", "0");	//# of conns on startup 
        cacheProps.setProperty("ConnectionWaitTimeout", "15");
        cacheProps.setProperty("ValidateConnection", "false");
        ds.setConnectionCacheProperties(cacheProps);
        ds.setImplicitCachingEnabled(true);
        ds.setConnectionCachingEnabled(true);
        
        Object subcontext = null;
        try {
        	subcontext = ctx.lookup("java:comp/env/jdbc");
        } catch (Exception e) {
        	//Ignore - just means it does not yet exist
        }
        if (subcontext == null) {
	        ctx.createSubcontext("java:");
	        ctx.createSubcontext("java:comp");
	        ctx.createSubcontext("java:comp/env");
	        ctx.createSubcontext("java:comp/env/jdbc");
        }
        
        ctx.bind("java:comp/env/jdbc/" + localName, ds);
	}
	
	protected void doDbTearDown() throws Exception {
		
		try {
			if (sparrowDBTestConn != null) {
				sparrowDBTestConn.close();
				sparrowDBTestConn = null;
			}
		} catch (Exception e) {
			log.warn("Exception thrown trying to close the test connection", e);
		}
		
		//It *seems* like closing the connection pool would be the right thing
		//to do, but it seems that the call to close() is ignored.
		//Rather than fight, I'll let the pool stay open for all the tests being
		//run, which means it doesn't have to create a new set of conn's for
		//each test.
//		oracleDataSource.close();
//		oracleDataSource = null;
//		
//		Context ctx = new InitialContext();
//        ctx.destroySubcontext("java:comp/env/jdbc");
//        ctx.destroySubcontext("java:comp/env");
//        ctx.destroySubcontext("java:comp");
//        ctx.destroySubcontext("java:");
        

	}
	
	public static Connection getSingleAutoCloseTestConnection() throws SQLException {
		if (sparrowDBTestConn == null || sparrowDBTestConn.isClosed()) {
			sparrowDBTestConn = SharedApplication.getInstance().getROConnection();
		}
		
		return sparrowDBTestConn;
	}
	
	protected static void setDbProperties() throws IOException {
		SharedApplication.getInstance().getConfiguration().setProperty("dburl", "jdbc:oracle:thin:@130.11.165.154:1521:widev");
		SharedApplication.getInstance().getConfiguration().setProperty("dbuser", "sparrow_dss");
		SharedApplication.getInstance().getConfiguration().setProperty("dbpass", "replaced");
	}
	
	protected static String prompt(String prompt) throws IOException {

		// prompt the user to enter their name
		System.out.print(prompt);

		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String val = null;
		val = br.readLine();
		return val;
	}
	
	
}
