package gov.usgswim.sparrow.service.predict.filter;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.filter.RowFilter;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * A predicate class for determining whether or not a row should be output by
 * the predict export service.  This filter is applied when the user has chosen
 * to limit their export to the reaches within the viewport, and has aggregated
 * the results up to a HUC.
 */
public class PredictExportAggFilter implements RowFilter {
    
    private PredictionContext context;
    private String bbox;
    private Map<Long,Boolean> results;
    
    public PredictExportAggFilter(PredictionContext context, String bbox) {
        this.context = context;
        this.bbox = bbox;
        this.results = getResults();
    }

    /**
     * Determines whether or not to accept the given row from the given table
     * by checking for the existence of the row's id in the result set.  To
     * elaborate, a set of huc ids is generated by querying for the set of huc
     * geometries that exist within a specified bounding box.  If the row id
     * for {@code rowNum} is contained within that result set, the row is
     * accepted and is not filtered.
     */
    public boolean accept(DataTable source, int rowNum) {
        Long id = source.getIdForRow(rowNum);
        return (results.get(id) != null);
    }
		
		public Integer getEstimatedAcceptCount() {
			if (results != null) {
				return results.size();
			} else {
				return null;
			}
		}
    
    /**
     * Returns a list of generated identifiers for HUCs that fall within the
     * bounding box defined by the specified arguments. A HUC geometry is
     * considered to fall within the bounding box if any part of its geometry
     * falls within the bounding box.
     * 
     * @return A list of (non-natural) identifiers for HUCs that fall within the
     *         defined bounding box.
     */
    private Map<Long,Boolean> getResults() {
        
        Connection conn = null;
        ResultSet rset = null;
        
        try {
            String groupBy = context.getAnalysis().getGroupBy();
            
            String query = ""
                    + "SELECT " + groupBy + "_id AS hucId "
                    + "FROM stream_network." + groupBy + "_lkp "
                    + "WHERE "
                    + "  SDO_FILTER(geom, SDO_GEOMETRY(2003, 8307, NULL,"
                    + "    SDO_ELEM_INFO_ARRAY(1,1003,3),"
                    + "    SDO_ORDINATE_ARRAY(" + bbox + "))"
                    + "  ) = 'TRUE' "
                    ;
            
            conn = SharedApplication.getInstance().getROConnection();
            Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(2000);
    
            rset = statement.executeQuery(query);
            
            Map<Long,Boolean> map = new HashMap<Long,Boolean>();
            while (rset.next()) {
                // Put the HUC id into the map with an irrelevant value of true
                // If it's in the map, it will not be filtered
                map.put(rset.getLong("hucId"), true);
            }
            
            return map;
        } catch (SQLException se) {
            se.printStackTrace();
            throw new RuntimeException(se);
        } finally {
            SharedApplication.closeConnection(conn, rset);
        }
    }
}
