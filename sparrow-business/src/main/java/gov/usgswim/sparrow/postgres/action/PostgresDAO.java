package gov.usgswim.sparrow.postgres.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * Performs DB utility functions needed to operate with postgres
 *
 * @author smlarson
 */
public class PostgresDAO {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PostgresDAO.class);
    private static String JNDI_JDBC_NAME;
    private static String SCHEMA_NAME_SPARROW_OVERLAY;
    private static String DB_NAME_MODEL_OUTPUT;
    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    
    public static String getSCHEMA_NAME_SPARROW_OVERLAY() {
        return SCHEMA_NAME_SPARROW_OVERLAY;
    }

    public static String getDB_NAME_MODEL_OUTPUT() {
        return DB_NAME_MODEL_OUTPUT;
    }
    
    public PostgresDAO() {
        if (StringUtils.isBlank(JNDI_JDBC_NAME)) {
            JNDI_JDBC_NAME = "/jdbc/postgres"; 
        }

        if (StringUtils.isBlank(SCHEMA_NAME_SPARROW_OVERLAY)) {
            SCHEMA_NAME_SPARROW_OVERLAY = "sparrow_overlay"; 
        }

        if (StringUtils.isBlank(DB_NAME_MODEL_OUTPUT)) {
            DB_NAME_MODEL_OUTPUT = "sparrow_model_output"; 
        }
        LOGGER.info("Database: %s with schema: %s is using a JNDI lookup of: %s to acquire connections.", new Object[] {DB_NAME_MODEL_OUTPUT, SCHEMA_NAME_SPARROW_OVERLAY, JNDI_JDBC_NAME} );
    }

    /**
     * Retrieves a connection from the database
     *
     * @return
     */
    protected Connection getConnection() {
        Connection con = null;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");  //DataSource ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/postgres" );
            DataSource ds = (DataSource) envCtx.lookup(JNDI_JDBC_NAME);
            con = ds.getConnection();
        } catch (SQLException | NamingException ex) {
            LOGGER.error("Could not create database connection", ex);
        }
        return con;
    }

    /**
     * Properly closing Connections and ResultSets without throwing exceptions,
     * see the section "Here is an example of properly written code to use a db
     * connection obtained from a connection pool"
     * http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html
     *
     * @param conn
     * @param resultSet
     */
    public static void closeConnection(Connection conn, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {;
            }
            resultSet = null;
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {;
            }
            conn = null;
        }
    }

}
