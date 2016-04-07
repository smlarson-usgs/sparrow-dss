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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

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
     * Tweaked a model 50 output to match the number that the test context has
     * because no existing dbf file was found in the prediction_data.
     *
     * @Ignore as this requires a local db to be stood up etc...more of an
     * integration test
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
    
    @Test
    public void testEnvironment()
    {
     System.out.println("testEnvironment");
      Map<String, String> map =  System.getenv();
      //Set<Entry<String,String>> set = map.entrySet();
      
      for (String key: map.keySet()) 
      {
        LOGGER.info("key: " +key);
        LOGGER.info("value : " + map.get(key));
      }

    }
    
    @Test
    public void testCommand()
    {
        //CreateViewForLayer instance = new CreateViewForLayer();
        //String command = instance.getProcessCommand(basicPredictContext, "sparrow_dss");//49P1716092856.dbf  //50N776208324.dbf  //57N331098404.dbf  //58N593755542.dbf
        String command = " /usr/local/bin/shp2pgsql -n -W ISO-8859-1 /Users/smlarson/sparrow/data/58N593755542.dbf sparrow_overlay.model_58N593755542 | /usr/local/bin/psql -d sparrow_dss -U sparrow_model_output_user -h localhost -p 5432 -w" ; 
        //String commandI = " /usr/local/bin/shp2pgsql -n -W ISO-8859-1 /Users/smlarson/sparrow/data/57N331098404.dbf sparrow_overlay.model_57N331098404 > modelout57N331098404.sql";
        //String commandII = " /usr/local/bin/psql -d sparrow_dss -U sparrow_model_output_user -h localhost -p 5432 -f /Users/smlarson/IdeaProjects/sparrow-dss/sparrow-business/modelout57N331098404.sql"; 
        
//String command = " pwd";
        String response = executeCommand(command, true);
        LOGGER.info("test Command response is: " + response);
    }
    
    public static String executeCommand(String command, boolean waitForResponse) {
        String response = "";

        //ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        pb.redirectErrorStream(true);

        System.out.println("Linux command: " + command);

        try {
            Process shell = pb.start();

            if (waitForResponse) {

                // To capture output from the shell
                InputStream shellIn = shell.getInputStream();

                // Wait for the shell to finish and get the return code
                int shellExitStatus = shell.waitFor();
                System.out.println("Exit status" + shellExitStatus);

                response = convertStreamToStr(shellIn);

                shellIn.close();
            }

        } catch (IOException e) {
            System.out.println("Error occured while executing Linux command. Error Description: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Error occured while executing Linux command. Error Description: "
                    + e.getMessage());
        }

        return response;
    }

    /*
    * To convert the InputStream to String we use the Reader.read(char[]
    * buffer) method. We iterate until the Reader return -1 which means
    * there's no more data to read. We use the StringWriter class to
    * produce the string.
     */
    public static String convertStreamToStr(InputStream is) throws IOException {

        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

}
