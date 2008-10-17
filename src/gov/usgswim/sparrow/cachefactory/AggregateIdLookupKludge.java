package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * This awesome kludge maps the artificially generated HUC id to its natural id
 * for use within the export feature of Sparrow.  This class is cached, with its
 * key being the level of aggregation requested (huc2, huc4, huc6, or huc8).
 * When an export is requested, the serializing class gains a reference to an 
 * object of this class and queries it to find the natural key.
 */
public class AggregateIdLookupKludge {
    private Map<Long, String> idLookupMap;
    
    /**
     * Constructs a new {@code AggregateIdLookupKludge}.
     * 
     * @param aggLevel The level of aggregation requested (huc2, huc4, etc.).
     */
    public AggregateIdLookupKludge(String aggLevel) {
        idLookupMap = new HashMap<Long, String>();
        populateIdLookup(aggLevel);
    }
    
    /**
     * Returns the natural key for the HUC with the given artificial id.
     * 
     * @param id The artificial id for the HUC.
     * @return The natural key for the HUC.
     */
    public String lookupId(long id) {
        return idLookupMap.get(id);
    }
    
    /**
     * Constructs the lookup map for the specified level of aggregation (huc2,
     * huc4, etc.).
     * 
     * @param aggLevel The level of aggregation requested.
     */
    private void populateIdLookup(String aggLevel) {
        // Build query to retrieve huc natural id and artificial id
        String query = ""
            + "SELECT " + aggLevel + ", " + aggLevel + "_id "
            + "FROM stream_network." + aggLevel + "_lkp "
            ;

        ResultSet rs = null;
        Connection conn = null;
        
        try {
            // Run the query
            conn = SharedApplication.getInstance().getConnection();
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery(query);
            
            // Iterate over the results, constructing a lookup table
            while (rs.next()) {
                long artificialId = rs.getLong(aggLevel + "_id");
                String naturalId = rs.getString(aggLevel);

                idLookupMap.put(artificialId, naturalId);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            SharedApplication.closeConnection(conn, rs);
        }
    }
}
