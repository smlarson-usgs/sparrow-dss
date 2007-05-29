package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.PredictionDataSet;

import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.ModelImm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.HashSet;

import oracle.jdbc.OracleDriver;

public class LoadTestRunner {
	public static String DATA_ROOT_DIR = "/data/ch2007_04_24/";

	public LoadTestRunner() {
	}

	public static void main(String[] args) throws Exception {
		LoadTestRunner loadTestRunner = new LoadTestRunner();
		
		loadTestRunner.run(args);
		
	}
	
	public void run(String[] args) throws Exception {
		String rootDir = DATA_ROOT_DIR;
		
		if (args != null && args.length > 0) {
			rootDir = args[0];
		}
		
		if (! rootDir.endsWith("/")) rootDir = rootDir + "/";
		
		PredictionDataSet pd = new PredictionDataSet();
		
		pd.setAncil( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "ancil.txt"), true) );
		pd.setCoef( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "coef.txt"), true) );
		pd.setSrc( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "src.txt"), true) );
		pd.setTopo( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "topo.txt"), true) );
		
		ModelBuilder mb = new ModelBuilder(21L);
		mb.setEnhNetworkId(2L);
		Model model = mb.getImmutable();
		
		pd.setModel( model );
		
		Connection conn = getConnection();
		
		try {
			conn.setAutoCommit(false);
			int count = JDBCUtil.writePredictDataSet(pd, conn);
                        System.out.println("Added " + count + " records to the db.");
                        conn.commit();
                } catch (Exception e) {
                    System.out.println("Exception during load:");
                    e.printStackTrace(System.err);
		    conn.rollback();
		} finally {
			try {
				
			} catch (Exception ee) {
				//ignore
			}
			conn.close();
		}

	}
	
	public PredictionDataSet loadModelFromText(String rootDir, long modelId, long enhNetworkId)
				throws FileNotFoundException, IOException {

		if (! rootDir.endsWith("/")) rootDir = rootDir + "/";
		
		PredictionDataSet pd = new PredictionDataSet();
		
		pd.setAncil( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "ancil.txt"), true) );
		pd.setCoef( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "coef.txt"), true) );
		pd.setSrc( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "src.txt"), true) );
		pd.setTopo( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "topo.txt"), true) );
		
		
		ModelBuilder mb = new ModelBuilder(modelId);
		mb.setEnhNetworkId(enhNetworkId);
		Model model = mb.getImmutable();
		
		pd.setModel( model );
		
		return pd;
	}
	
	
	protected Connection getConnection() throws SQLException {
		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		return DriverManager.getConnection(thinConn,username,password);
	}

}
