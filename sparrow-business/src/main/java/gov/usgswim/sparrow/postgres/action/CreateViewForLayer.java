package gov.usgswim.sparrow.postgres.action;

import gov.usgs.cida.sparrow.service.util.NamingConventions;
import gov.usgswim.sparrow.action.WriteDbfFileForContext;
import gov.usgswim.sparrow.domain.PredictionContext;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.slf4j.LoggerFactory;

/**
 *
 * Take a corresponding dbf file and join it to its river region to create a
 * view later exposed in GeoServer as a layer.
 *
 * @author smlarson
 */
public class CreateViewForLayer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreateViewForLayer.class);
    protected final PostgresDAO pgDao = new PostgresDAO();
    
    public CreateViewForLayer() {
       
    }

    // uses a process builder to invoke shp2pgsql
    /**
     *
     * @param dbfFile File that represents the model output
     * @param context All the state needed to run a prediction
     * @param mydb null unless you are testing
     * @throws IOException
     */
    public void createTableFromDbf(File dbfFile, PredictionContext context, String mydb) throws IOException, InterruptedException {
        // takes the dbf and via a Java process, invokes shp2pgsql which transforms
        // the dbf into a postgres table, named with the 10 digit hash and the model ID
        String command = getProcessCommand(context, mydb);
        LOGGER.info("createTableFromDBF: ProcessBuilder recieved this command: " + command);
        
        if(isFileReadable(context))
        {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();  //throws IOException ...need to add logging
            int errCode = p.waitFor();
            LOGGER.info("Any process errors? " + (errCode == 0 ? "No" : "YES"));
        }
        else {
           LOGGER.info("Dbf file with this ID is not readable:" + context.getModelID().toString());
        }
    }
    
    //checks the file to make sure its not null and is accessible 
    private boolean isFileReadable(PredictionContext context)
    {
        boolean result = false;
        WriteDbfFileForContext dbfFileWriter = new WriteDbfFileForContext(context);
        File dbfFile = dbfFileWriter.getDbfFile();
        LOGGER.info("Dbf file path: " + dbfFile.getPath());
        
        if (dbfFile.exists())
        {
            result = dbfFile.canRead();
        }
        
        return result;
    }
    /**
     * @param context All the state needed to run a prediction
     * @param mydb null unless you are testing
     * // example: ("/bin/sh", "-n", "-c", "shp2pgsql dbfName.dbf | psql -d mydb -U sparrow_model_output_user"); //will take a List<String> too
     */
    private String getProcessCommand(PredictionContext context, String mydb)
    {
        if (mydb == null)
         mydb = PostgresDAO.getDB_NAME_MODEL_OUTPUT();
        String dbUser = PostgresDAO.getDBUser();
        String dbHost = PostgresDAO.getDBHost();
        String dbPort = PostgresDAO.getDBPort();
        String dbfName = getDbfNameWithModelNbr(context);  //50n776208324 on test context
        String dbfPath = "/Users/smlarson/sparrow/data/" ;  //for testing without a context lookup
        
        StringBuilder sb = new StringBuilder(); // add quotes too \"
        sb.append("\"/bin/sh\"");  
        sb.append(", ");
        sb.append("\"-n\"");
        sb.append(", ");
        sb.append("\"-c\"");
        sb.append(" ");
        sb.append("\"shp2pgsql ");
        sb.append(dbfPath);
        sb.append(dbfName); // location of dbf file
        sb.append(".dbf ");
        sb.append("| psql -d ");
        sb.append(mydb); 
        sb.append(" -U ");
        sb.append(dbUser);
                sb.append(" -h ");
        sb.append(dbHost);
        sb.append(" -p ");
        sb.append(dbPort);
        sb.append("\""); 
          // also psql -d gisdatabase –U username –h hostname –p port -f parcels.sql <will force a prompt for the pwd> -f is filename 
          // page with the diff options you can pass to shp http://www.bostongis.com/pgsql2shp_shp2pgsql_quickguide.bqg  -u user, -P pwd, -n only import dbf file -T tablespace, -X tablespace for index
          // -k keep case ??,         
        return sb.toString();
    }
    
    // Typically two views are created for each dbf: catchment and a flow (aka reach)
    /**
     * This method assumed you have already created the dbf table in postgres.
     * @param context All the state needed to run a prediction
     * @param connection A Postgres db connection. If null, will lookup from JNDI. For testing purposes.
     * @return base view name (without the catchment or flow) ie 22n1220785281
     * @throws java.sql.SQLException
     */
    public String createModelOutputViews(PredictionContext context, Connection connection) throws SQLException {
        
        String baseViewName = null;
        LOGGER.info("createModelOutputViews: The context has this modelId:" + context.getModelID() + " and this ID: " + context.getId());

        createView(context, connection, false);
        baseViewName = createView(context, connection, true);

        return baseViewName;
    }

    // this will give the int part with the modelNbr+P||N prefix too
    private String getDbfNameWithModelNbr(PredictionContext context)
    {
        int modelId = context.getModelID().intValue();
        int dbfHash = context.getId(); 
        
        return NamingConventions.convertContextIdToXMLSafeName(modelId, dbfHash); //gets everything but the .dbf 
    }
    
    // this will give the int part with the modelNbr+P||N prefix too
    private String getDbfTableName(String dbfNameWithModelNbr)
    {
        StringBuilder sb= new StringBuilder();
        sb.append("model_");
        sb.append(dbfNameWithModelNbr);
        
        return sb.toString(); 
    }
    
    // The view layer name will be exposed in geoserver as the layer name which 
    // is currently the modelNbr+N or P+dbf9digits. Until the view is
    // exposed as a layer in the WMS, the concept of catchment or flow must be captured with the name.
    // flow-22n1220785281 with either flow- or catchment- acting as a prefix to the dbfId.
    // Later, the flow- will be removed during the pub of the layer to keep with the former naming conventions.
    private String createView(PredictionContext context, Connection connection, boolean isFlow) throws SQLException {

        LOGGER.info("___________________________________________");
        int twoDigits = context.getModelID().intValue(); //our models are one digit short of long
        LOGGER.info("The two digit ModelId is:" + twoDigits);
            
        String viewLayerName = "catchment-" + getDbfNameWithModelNbr(context);
        String rNetwork = MODEL_REGION.from(twoDigits).riverNet + "_catch"; //catch, flow or huc8 options 
        String regGeomType = "net.geom::geometry(MultiPolygon, 4326) AS geom "; //catch
        
        if (isFlow)
        {
            regGeomType = "net.geom::geometry(MultiLineString, 4326) AS geom "; //flow geom type MultiLine
            viewLayerName = "flow-" + getDbfNameWithModelNbr(context);
            rNetwork = MODEL_REGION.from(twoDigits).riverNet + "_flow";
        }
        else
        {
          viewLayerName = "catchment-" + getDbfNameWithModelNbr(context);
          rNetwork = MODEL_REGION.from(twoDigits).riverNet + "_catch"; //catch, flow or huc8 options 
          regGeomType = "net.geom::geometry(MultiPolygon, 4326) AS geom "; //catch  
        }
        
        LOGGER.info("The view layer name is:" + viewLayerName);
        LOGGER.info("The dbf will join to region: " + rNetwork);
        LOGGER.info("The dbf view has geomType " + regGeomType);
        
        String dbfTableName = getDbfTableName(getDbfNameWithModelNbr(context)); //model_52N12312323
        LOGGER.info("The dbf table that will join: " + dbfTableName);
        
        String sql = getViewSql(viewLayerName, dbfTableName, rNetwork, regGeomType);
        LOGGER.info("CreateView sql:" + sql);
        LOGGER.info("___________________________________________");
        
        
        if (connection == null)
            connection = pgDao.getConnection();  // performs JNDI lookup that requires a container is running etc
        final PreparedStatement st = connection.prepareStatement(sql);

        try {
            st.execute();  //this returns a boolean; false if the rs is an update count as expected   st.execute(sql); 
        } finally {
            LOGGER.info("Closing connection CreateViewForLayer.createView.");
            st.close();
        }

        return viewLayerName;
    }

    private String getViewSql(String viewLayerName, String dbfTableName, String riverNetworkTableName, String regGeomType) {
        StringBuilder sql = new StringBuilder();
              
        sql.append("CREATE OR REPLACE VIEW ");
        sql.append(PostgresDAO.getSCHEMA_NAME_SPARROW_OVERLAY());
        sql.append(".");
        sql.append("\"");
        sql.append(viewLayerName);
        sql.append("\"");
        sql.append(" AS ");
        sql.append("SELECT dbf.identifier AS \"IDENTIFIER\", ");
        sql.append("dbf.value as \"VALUE\", "); //capitalized to match styles already created
        sql.append("net.gid, ");
        sql.append("net.source, ");
        sql.append(regGeomType);
      //  sql.append("net.geom::geometry(MultiPolygon, 4326) AS geom ");  // <-- line will need to change for flows
        sql.append("FROM ");
        sql.append(PostgresDAO.getSCHEMA_NAME_SPARROW_OVERLAY());
        sql.append(".");
        sql.append(dbfTableName);  //exp model_22n1220785281
        sql.append(" dbf, ");
        sql.append(PostgresDAO.getSCHEMA_NAME_SPARROW_OVERLAY());
        sql.append(".");
        sql.append(riverNetworkTableName);  //exp national_e2rf1_flow
        sql.append(" net WHERE dbf.identifier = net.identifier; ");  //does this need a commit?

        String result = sql.toString();
        LOGGER.info("Attempting to create view of dbf with shape file : " + result);

        return result;
    }
    
    //22,23,24,25, 30,35,36,37,38,41,42,43,44
    public enum MODEL_REGION {
        model22(22, "national_e2rf1"),
        model23(23, "national_e2rf1"),
        model24(24, "national_e2rf1"),
        model25(25, "national_mrb_e2rf1"),
        model30(30, "national_e2rf1"),
        model35(35, "mrb05_mrbe2rf1"),
        model36(36, "mrb05_mrbe2rf1"),
        model37(37, "marb_mrbe2rf1"),
        model38(38, "marb_mrbe2rf1"),
        model41(41, "mrb03_mrbe2rf1"),
        model42(42, "mrb03_mrbe2rf1"),
        model43(43, "mrb07_mrbe2rf1"),
        model44(44, "mrb07_mrbe2rf1"),
        model49(49, "mrb02_mrbe2rf1"),
        model50(50, "mrb02_mrbe2rf1"),
        model51(51, "mrb01_nhd"),
        model52(52, "mrb01_nhd"),
        model53(53, "national_e2rf1"),
        model54(54, "chesa_nhd"),
        model55(55, "chesa_nhd"),
        model57(57, "mrb04_mrbe2rf1"),
        model58(58, "mrb04_mrbe2rf1"),
        unknown(-1, "unknown");

        private final int value;
        public final String riverNet;

        private MODEL_REGION(int value, String riverNet) {
            this.value = value;
            this.riverNet = riverNet;
        }

        public static MODEL_REGION from(int modelId) {
            for (MODEL_REGION model : MODEL_REGION.values()) {
                if (model.value == modelId) {
                    return model;
                }

            }
            return unknown;
        }  // reference it like this to get the string back- MODEL_NBR.from(22).name(); 

    }

}
