package gov.usgswim.sparrow.postgres.action;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.BasicAnalysis;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.NoComparison;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.test.SparrowTestBase;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smlarson
 */
public class CreateViewForLayerTest extends SparrowTestBase {

    public PredictionContext basicPredictContext = null;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreateViewForLayerTest.class);

    public CreateViewForLayerTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        AdjustmentGroups testModelNoAdjustmentsGroup = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);

        basicPredictContext = new PredictionContext(
                SparrowTestBase.TEST_MODEL_ID,
                testModelNoAdjustmentsGroup,
                new BasicAnalysis(DataSeriesType.total, null, null, null),
                new TerminalReaches(SparrowTestBase.TEST_MODEL_ID),
                null,
                NoComparison.NO_COMPARISON);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of createTableFromDbf method, of class CreateViewForLayer.
     */
    @Test
    public void testCreateTableFromDbf() throws Exception {
        System.out.println("createTableFromDbf");
        String myTestdb = "sparrow_dss";
        File dbfFile = null;
        PredictionContext context = basicPredictContext;
        CreateViewForLayer instance = new CreateViewForLayer();
        instance.createTableFromDbf(dbfFile, context, myTestdb);
        // TODO review the generated test code - need to convert into dbf file
    }

    /**
     * Test of createModelOutputViews method, of class CreateViewForLayer.
     * Tweaked a model 50 output to match the number that the test context has because no existing dbf file was found in the prediction_data.
     * @Ignore as this requires a local db to be stood up etc...more of an integration test
     */
    @Ignore
    //@Test
    public void testCreateModelOutputViews() throws Exception {
        System.out.println("createModelOutputViews");
        PredictionContext context = basicPredictContext;
        CreateViewForLayer instance = new CreateViewForLayer();
        String expResult = "flow-50N776208324"; //base name does not include catchment or flow ..  
        Connection connection = testGetDbConnection();
        
        String result = instance.createModelOutputViews(context, connection);
        closeConnection(connection);
        
        assertEquals(expResult, result);

    }

    // TODO dont check in with pwd
    //@Test
    public Connection testGetDbConnection() {
        System.out.println("testGetDbConnection");
        // This will make sure the user, db url and pwd are correct for the db
        Connection connection = null;
        LOGGER.info("Getting connection via Driver Manager....");
        String dbUrl = "jdbc:postgresql://127.0.0.1:5432/sparrow_dss";//local db name...not prod
        String dbuser = PostgresDAO.getDBUser();
        String dbpass = "pwd";
        try {
            Class.forName("org.postgresql.Driver");
            
        } catch (ClassNotFoundException e) {

            LOGGER.info("Where is your PostgreSQL JDBC Driver? "
                    + "Include in your library path via maven!");
        }

        LOGGER.info("Getting connection via Driver Manager....url: " + dbUrl + " user: " + dbuser);
        try {
            connection = DriverManager.getConnection(dbUrl, dbuser, dbpass);
            //return connection;
        } catch (SQLException e) {
            LOGGER.info("POSTGRES Could not get connection.");
        }
        assertNotNull(connection);
        
        return connection;
    }

    public static void closeConnection(Connection conn) {

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {;
            }
            conn = null;
        }
    }    
     
}
