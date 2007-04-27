package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.PredictionDataSet;

import gov.usgswim.sparrow.domain.ModelImp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

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
		pd.setModel( new ModelImp(21) );
		
		Connection conn = getConnection();
		
		try {
			JDBCUtil.writePredictDataSet(pd, conn);
		} finally {
			conn.close();
		}

	}
	
	protected Connection getConnection() throws SQLException {
		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		return DriverManager.getConnection(thinConn,username,password);
	}

}
