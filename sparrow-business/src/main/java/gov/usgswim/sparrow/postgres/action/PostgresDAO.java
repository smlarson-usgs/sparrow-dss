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
    private static String JNDI_JDBC_NAME; ///jdbc/postgres
    private static String SCHEMA_NAME_SPARROW_OVERLAY; //sparrow_overlay
    private static String DB_NAME_MODEL_OUTPUT; //sparrow_model_output
    private static String DB_USER;  //sparrow_model_output_user
    private static String DB_PORT; // 5432
    private static String DB_HOST; // DEV: cida-eros-sparrowdev.er.usgs.gov
    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String getSCHEMA_NAME_SPARROW_OVERLAY() {
        return SCHEMA_NAME_SPARROW_OVERLAY;
    }

    public static String getDB_NAME_MODEL_OUTPUT() {
        return DB_NAME_MODEL_OUTPUT;
    }

    public static String getDBUser() {
        return DB_USER;
    }

    public static String getDBHost() {
        return DB_HOST;
    }

    public static String getDBPort() {
        return DB_PORT;
    }
    
    public PostgresDAO() {
        if (StringUtils.isBlank(JNDI_JDBC_NAME)) {
            JNDI_JDBC_NAME = "/jdbc/postgres";
        }

        if (StringUtils.isBlank(SCHEMA_NAME_SPARROW_OVERLAY)) {
            SCHEMA_NAME_SPARROW_OVERLAY = "sparrow_overlay";
        }

        if (StringUtils.isBlank(DB_NAME_MODEL_OUTPUT)) {
            DB_NAME_MODEL_OUTPUT = "sparrow_model_output"; //sparrow_model_output #TODO dynamicProps, sparrow_dss
        }

        if (StringUtils.isBlank(DB_USER)) {
            DB_USER = "sparrow_model_output_user";
        }
        
        if (StringUtils.isBlank(DB_PORT)) {
            DB_PORT = "5432";
        }

        if (StringUtils.isBlank(DB_HOST)) {
            DB_HOST = "cida-eros-sparrowdev.er.usgs.gov"; //cida-eros-sparrowdev.er.usgs.gov #TODO# dynamicProps,localhost
        }        
        LOGGER.info(String.format("Database: %1s with schema: %2s is using a JNDI lookup of: %3s to acquire connections with host: %4s and port: %5s.", DB_NAME_MODEL_OUTPUT, SCHEMA_NAME_SPARROW_OVERLAY, JNDI_JDBC_NAME, DB_HOST, DB_PORT));
    }

    /**
     * Retrieves a connection from the database
     *
     * @return
     */
    protected Connection getConnection() {
        Connection con = null;
        try {
            //Context initCtx = new InitialContext();
            Context ctx = new InitialContext();  
            //  Context envCtx = (Context) initCtx.lookup("java:comp/env");  //DataSource ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/postgres" );
            //  DataSource ds = (DataSource) envCtx.lookup(JNDI_JDBC_NAME);
            if (ctx == null)
                LOGGER.info("JNDI problem. Cannot get InitialContext.");
            else
                LOGGER.info("PostgresDAO: getting datasource from Init Context ");
            
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/postgres");
            LOGGER.info("Postgres env context is:" + ctx.toString() + "  with datasource:" + ds.toString());
            con = ds.getConnection();
        } catch (SQLException | NamingException ex) {
            LOGGER.error("Could not create sparrow postgres database connection", ex);
        }
        return con;
    }

        /**
         * Properly closing Connections and ResultSets without throwing
         * exceptions, see the section "Here is an example of properly written
         * code to use a db connection obtained from a connection pool"
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
