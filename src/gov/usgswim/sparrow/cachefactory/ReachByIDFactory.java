package gov.usgswim.sparrow.cachefactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.service.idbypoint.Reach;



/**
 * This factory finds a Reach based on a ReachID object
 * 
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 * 
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 * 
 * @author eeverman
 *
 */
public class ReachByIDFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(ReachByIDFactory.class); //logging for this class
	
	public Object createEntry(Object request) throws Exception {
		
		ReachID req = (ReachID) request;
		
		String query = getText(
				"FindReach",
				new String[] { "ModelId", Long.toString(req.getModelID()), "Identifier", Integer.toString( req.getReachID() ) });
		
		Connection conn = SharedApplication.getInstance().getConnection();


		ResultSet rs = null;
		Reach reach = null;
		
		try {
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(1);
			
			rs = st.executeQuery(query);
			
			if (rs.next()) {
				
				// reaches retrieved by ID should never have distanceInMeters information
				reach = new Reach(req.getModelID(), rs.getInt("identifier"), rs.getString("reach_name"), null,
						rs.getDouble("MIN_LONG"), rs.getDouble("MIN_LAT"), rs.getDouble("MAX_LONG"), rs.getDouble("MAX_LAT"),
						rs.getString("HUC2"), rs.getString("HUC2NAME"), rs.getString("HUC4"), rs.getString("HUC4NAME"),
						rs.getString("HUC6"), rs.getString("HUC6NAME"), rs.getString("HUC8"), rs.getString("HUC8NAME")
				);

			} else {
				//no rows found - leave as null
			}
		} finally {
			SharedApplication.closeConnection(conn, rs);
		}

		return reach;
		
	}

}
