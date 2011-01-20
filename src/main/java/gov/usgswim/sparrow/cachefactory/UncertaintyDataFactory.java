package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataImm;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DLUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;


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
public class UncertaintyDataFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(UncertaintyDataFactory.class); //logging for this class

	@Override
	public UncertaintyData createEntry(Object request) throws Exception {

		UncertaintyDataRequest req = (UncertaintyDataRequest) request;
		float data[][] = null;
		

		Connection conn = SharedApplication.getInstance().getROConnection();
		ResultSet rs = null;

		try {
			
			String srcId = "";
			if (req.getSourceId() != null) {
				srcId = req.getSourceId().toString();
			}
			
			String query = getTextWithParamSubstitution(
				req.getUncertaintySeries().name(),
				"ModelId", Long.toString(req.getModelId()), "SourceId", srcId );

			//System.out.println("Query: " + query);
			
			int reachCount = calcReachCount(req.getModelId());
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			data = new float[2][reachCount];
			
			rs = st.executeQuery(query);

			for (int row = 0; row < reachCount; row++) {

				rs.next();
				
				float m = rs.getFloat("MEAN_PLOAD");
				float se = rs.getFloat("SE_PLOAD");
				
				data[0][row] = m;
				data[1][row] = se;
			}
			
		} finally {
			SharedApplication.closeConnection(conn, rs);
		}

		UncertaintyData uData = new UncertaintyDataImm(data);
		return uData;

	}
	
	protected int calcReachCount(long modelId) throws IOException, SQLException {
		String query = getTextWithParamSubstitution(
				"SelectReachCount",
				"ModelId", Long.toString(modelId));

		Connection conn = null;
		Integer rchCount = null;
		try {
			conn = SharedApplication.getInstance().getROConnection();
			rchCount = DLUtils.readAsInteger(conn, query, 1).getInt(0, 0);
		} finally {
			SharedApplication.closeConnection(conn, null);
		}
		
		return rchCount;
	}

}
