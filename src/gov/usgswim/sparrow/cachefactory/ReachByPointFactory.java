package gov.usgswim.sparrow.cachefactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest2;
import gov.usgswim.sparrow.service.idbypoint.Reach;



/**
 * This factory class creates a ReachId on demand for an EHCache.
 * 
 * When the cache receives a get(IDByPointRequest) call and it doesn't have a cache
 * entry for that request, the createEntry() method of this class is called
 * and the returned value is cached.
 * 
 * @author eeverman
 *
 */
public class ReachByPointFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(ReachByPointFactory.class); //logging for this class
	
	public Object createEntry(Object request) throws Exception {
		
		IDByPointRequest2 req = (IDByPointRequest2) request;
		
		Integer modelId = req.getModelID();
		Double lng = req.getPoint().x;
		Double lat = req.getPoint().y;
		
		
		
		String query = getText(
				"FindIdentifier",
				new String[] {"ModelId", modelId.toString(), "lng", lng.toString(), "lat", lat.toString()});
		
		Connection conn = SharedApplication.getInstance().getConnection();
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(1);

		ResultSet rs = null;
		Reach reachId = null;
		
		try {

			rs = st.executeQuery(query);
			
			if (rs.next()) {
				
				reachId = new Reach(rs.getInt("identifier"), rs.getString("reach_name"), rs.getInt("dist_in_meters"));

			} else {
				//no rows found - leave as null
			}

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
		

		
		return reachId;
		
	}

}
