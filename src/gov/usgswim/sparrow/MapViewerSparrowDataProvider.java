package gov.usgswim.sparrow;

import gov.usgswim.sparrow.util.JDBCUtil;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Properties;

import java.util.Vector;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import oracle.mapviewer.share.ext.NSDataProvider;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;
import oracle.mapviewer.share.Field;


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
		int modelId = Integer.parseInt( properties.getProperty("model_id") );
		
		Connection conn = null;
		NSDataSet data = null;
		
		try {
			conn = getConnection();
			
			PredictionDataSet ds = JDBCUtil.loadMinimalPredictDataSet(conn, modelId);
			PredictSimple ps = new PredictSimple(ds);
			Double2D result = ps.doPredict();
			
			int rowCount = result.getRowCount();
			int colCount = result.getColCount();
			NSRow[] nsRows = new NSRow[rowCount];
			
			for (int r=0; r < rowCount; r++) {
				Field[] row = new Field[colCount];
				for (int c=0; c < colCount; c++) {
					row[c] = new Field(result.getDouble(r, c));
				}
				NSRow nsRow = new NSRow(row);
				nsRows[r] = nsRow;
			}
			
			log.info("Returning data with " + rowCount + " rows");
			data = new NSDataSet(nsRows);
			
		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer...", e);
			return new NSDataSet(new NSRow[0]);
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
