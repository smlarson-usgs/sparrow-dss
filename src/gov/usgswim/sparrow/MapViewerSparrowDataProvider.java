package gov.usgswim.sparrow;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Properties;

import java.util.Vector;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import oracle.mapviewer.share.ext.NSDataProvider;
import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.log4j.Logger;

public class MapViewerSparrowDataProvider implements NSDataProvider {
	protected static Logger log =
		Logger.getLogger(MapViewerSparrowDataProvider.class); //logging for this class
		
	public static final String DATASOURCE_NAME_KEY = "datasource-name";
		
	protected String datasourceName = null;		//The name of a JNDI datasource to use
	protected DataSource datasource = null; //A datasource for creating data related db connections.
	
	public MapViewerSparrowDataProvider() {
	}

	/**
	 * Called onces at creation time.
	 * @param properties
	 * @return
	 */
	public boolean init(Properties properties) {
	
		datasourceName = properties.getProperty(DATASOURCE_NAME_KEY);
		
		
		return true;
	}

	/**
	 * Called for each request.
	 * @param properties
	 * @return
	 */
	public NSDataSet buildDataSet(Properties properties) {
		Vector data = new Vector(1000);
		
		Connection conn = null;


		try {
			conn = getConnection();
			
			
			
			
		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer...", e);
			return new NSDataSet(data);
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
		return null;
	}

	/**
	 * Called once when this instance is destroyed
	 */
	public void destroy() {
	}
	
	public synchronized Connection getConnection() throws NamingException, SQLException {

		Connection conn = null;

		if (datasource == null) {
			log.debug("Build new data jdbc datasouce from jndi name '" + datasourceName + "'");
			InitialContext context = new InitialContext();
			datasource = (DataSource)context.lookup(datasourceName);
		}

		conn = datasource.getConnection();
		
		if (conn == null) {
			log.error("The jndi datasource '" + datasourceName + "' return a null connection");

		} else if (conn.isClosed()) {
			log.error("The jndi datasource '" + datasourceName + "' returned a closed connection");
			conn = null;
		}
		
		return conn;
	}
}
