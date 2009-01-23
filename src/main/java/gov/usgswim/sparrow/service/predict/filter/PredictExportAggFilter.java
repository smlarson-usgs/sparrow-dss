package gov.usgswim.sparrow.service.predict.filter;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.RowFilter;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class PredictExportAggFilter implements RowFilter {
    
    private PredictionContext context;
    private String bbox;
    private Map<Long,Boolean> results;
    
    public PredictExportAggFilter(PredictionContext context, String bbox) {
        this.context = context;
        this.bbox = bbox;
        this.results = getResults();
    }

    public boolean accept(DataTable source, int rowNum) {
        Long id = source.getIdForRow(rowNum);
        return (results.get(id) != null);
    }
    
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
            
            conn = SharedApplication.getInstance().getConnection();
            Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(2000);
    
            rset = statement.executeQuery(query);
            
            Map<Long,Boolean> map = new HashMap<Long,Boolean>();
            while (rset.next()) {
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
