package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;



/**
 * This factory finds a Reach based on a ModelPoint.
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
public class ReachByPointFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(ReachByPointFactory.class); //logging for this class

	@Override
	public ReachInfo createEntry(Object request) throws Exception {

		ModelPoint req = (ModelPoint) request;

		Long modelId = req.getModelID();
		Double lng = req.getPoint().x;
		Double lat = req.getPoint().y;

		String query = getText(
				"FindReach",
				new String[] {"ModelId", modelId.toString(), "lng", lng.toString(), "lat", lat.toString()});

		Connection conn = SharedApplication.getInstance().getConnection();

		ResultSet rs = null;

		try {
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(1);

			rs = st.executeQuery(query);

			Integer reachID = null; Integer distance = null;
			if (rs.next()) {
				reachID = rs.getInt("identifier");
				distance = rs.getInt("dist_in_meters");
			}
			{	// Clean up the connection before going to next step, which might wind up using the same connection
				rs.close();
				rs = null;
			}
			if (reachID != null) {
				ReachInfo reach = SharedApplication.getInstance().getReachByIDResult(new ReachID(req.getModelID(), reachID));
				// add the distance information to the retrieved Reach
				ReachInfo result = reach.cloneWithDistance(distance);
				result.setClickedPoint(lng, lat);
				return result;
			}

		} finally {
			SharedApplication.closeConnection(conn, rs);
		}

		return null;

	}

}
