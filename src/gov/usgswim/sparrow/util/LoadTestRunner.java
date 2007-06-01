package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.PredictionDataSet;

import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.ModelImm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.HashSet;

import java.util.Scanner;

import javax.swing.JOptionPane;

import oracle.jdbc.OracleDriver;

import org.apache.log4j.Logger;

public class LoadTestRunner {
	protected static Logger log = Logger.getLogger(LoadTestRunner.class); //logging for this class
    
	public static String DATA_ROOT_DIR = "/data/ch2007_04_24/";
	
	private String _root;
	private long _modelId;
	long _enhNetworkId;
	private boolean _loadAllIterations = true;	//default include all iterations

	public LoadTestRunner() {
	}

	public static void main(String[] args) throws Exception {
		LoadTestRunner loadTestRunner = new LoadTestRunner();
		
		loadTestRunner._root = args[0];
		loadTestRunner._modelId = Long.parseLong(args[1]);
		loadTestRunner._enhNetworkId = Long.parseLong(args[2]);
		if (args.length > 3) {
			loadTestRunner._loadAllIterations = Boolean.parseBoolean(args[3]);
		}
		
		

		
		String message = "Root to load from: " + loadTestRunner._root + "\n";
		message += "Load to model ID: " + loadTestRunner._modelId + "\n";
		message += "Load based on Enhanced Network ID: " + loadTestRunner._enhNetworkId + "\n";
		if (loadTestRunner._loadAllIterations) {
			message += "--Loading all iterations--" + "\n";
		} else {
			message += "Loading only the zero iteration\n";
		}
		
		message += " Is this OK?" + "\n";
		
		
		int n = JOptionPane.showConfirmDialog(null, message,
				"Model Load Configuration", JOptionPane.YES_NO_OPTION);
		
		if (n == JOptionPane.YES_OPTION) {
			System.out.println("Starting... (it may be a while)");
			loadTestRunner.run();
		} else {
			System.out.println("Terminated.");
		}
		
	}
	
	public void run() throws Exception {
	
		PredictionDataSet pd = new PredictionDataSet();
		
		long initStartTime = System.currentTimeMillis();
		long fileReadStart = initStartTime;
		
		if (_root.startsWith("file:")) {
			pd = TabDelimFileUtil.loadPredictDataSet(
				null, _root.substring(5), _modelId, _enhNetworkId, _loadAllIterations, true);
		} else if (_root.startsWith("package:")) {
			pd = TabDelimFileUtil.loadPredictDataSet(
				_root.substring(8), null, _modelId, _enhNetworkId, _loadAllIterations, true);
		} else {
			throw new IllegalArgumentException(
				"A package or directory containing the source files must" +
				"be specified starting with 'file:' or 'package:'"
			);
		}
                
		long fileReadEnd = System.currentTimeMillis();
		log.debug("Load time for text files was: " + (fileReadEnd-fileReadStart)/1000 + " (sec)");

		ModelBuilder mb = new ModelBuilder(_modelId);
		mb.setEnhNetworkId(_enhNetworkId);
		Model model = mb.getImmutable();
		
		pd.setModel( model );
		
		Connection conn = getConnection();
		
		long startTime = System.currentTimeMillis();
		
		try {
		
			conn.setAutoCommit(false);
			int count = JDBCUtil.writePredictDataSet(pd, conn, 250);
			//int count = JDBCUtil.writeModelReaches(pd, conn, 200).size();
			System.out.println("Added " + count + " records to the db.");
			//conn.commit();
			conn.rollback();
			
		} catch (Exception e) {
		
			System.out.println("Exception during load:");
			e.printStackTrace(System.err);
			conn.rollback();
			
		} finally {
		
			long endTime = System.currentTimeMillis();
			int totalSeconds = (int) (endTime - startTime) / 1000;
			int minutes = totalSeconds / 60;
			int seconds = totalSeconds % 60;
			
			System.out.println("Total time for run was: " + minutes + ":" + seconds + " (min:sec)");
			System.out.println("Total reaches loaded: " + pd.getAncil().getRowCount());
			
			try {
				conn.close();
			} catch (Exception ee) {
				ee.printStackTrace();
			}
			
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
