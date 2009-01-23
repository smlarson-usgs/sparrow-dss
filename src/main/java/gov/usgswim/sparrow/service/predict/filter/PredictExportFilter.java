package gov.usgswim.sparrow.service.predict.filter;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.RowFilter;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PredictExportFilter implements RowFilter {

    private Connection conn;
    private ResultSet results;

    public PredictExportFilter(Long modelId, String bbox) {
        this.results = getResults(modelId, bbox);
    }

    public boolean accept(DataTable source, int rowNum) {
        try {
            if (!validCursor()) {
                return false;
            }

            Long srcHydseq = source.getLong(rowNum, source.getColumnByName("HYDSEQ"));
            Long srcId = source.getIdForRow(rowNum);

            Long queryHydseq = results.getLong("HYDSEQ");
            Long queryId = results.getLong("IDENTIFIER");
            
            if (queryHydseq > srcHydseq || queryId > srcId) {
                return false;
            } else {
                updateCursor();
                return true;
            }
        } catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }
    
    private boolean validCursor() throws SQLException {
        if (results.isBeforeFirst()) {
            return results.next();
        }
        return !(results.isAfterLast());
    }
    
    private boolean updateCursor() throws SQLException {
        boolean hasNext = results.next();
        if (!hasNext) {
            SharedApplication.closeConnection(conn, results);
        }
        return hasNext;
    }

    /**
     * Returns a list of identifiers for reaches that fall within the bounding
     * box defined by the specified arguments. A reach is considered to fall
     * within the bounding box if any part of its geometry falls within the
     * bounding box.
     * 
     * @return A list of identifiers for reaches that fall within the defined
     *         bounding box.
     */
    private ResultSet getResults(Long modelId, String bounds) {
        try {
            String query = ""
                    + "SELECT A.identifier AS IDENTIFIER, B.hydseq AS HYDSEQ "
                    + "FROM MODEL_GEOM_VW A INNER JOIN MODEL_REACH B ON A.model_reach_id = B.model_reach_id "
                    + "WHERE "
                    + "  A.sparrow_model_id = " + modelId
                    + "  AND SDO_FILTER(reach_geom, SDO_GEOMETRY(2003, 8307, NULL, SDO_ELEM_INFO_ARRAY(1,1003,3), SDO_ORDINATE_ARRAY(" + bounds + "))) = 'TRUE' "
                    + "ORDER BY B.hydseq, A.identifier "
                    ;
    
            conn = SharedApplication.getInstance().getConnection();
            Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(2000);
    
            ResultSet results = statement.executeQuery(query);
            return results;
        } catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }
}
