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

public class LoadTestRunner {
	public static String DATA_ROOT_DIR = "/data/ch2007_04_24/";
	
	private String _root;
	private long _modelId;
	long _enhNetworkId;
	private int _lastIncludedIteration = -1;	//default include all iterations

	public LoadTestRunner() {
	}

	public static void main(String[] args) throws Exception {
		LoadTestRunner loadTestRunner = new LoadTestRunner();
		
		loadTestRunner._root = args[0];
		loadTestRunner._modelId = Long.parseLong(args[1]);
		loadTestRunner._enhNetworkId = Long.parseLong(args[2]);
		if (args.length > 3) {
			loadTestRunner._lastIncludedIteration = Integer.parseInt(args[3]);
		}
		
		

		
		String message = "Root to load from: " + loadTestRunner._root + "\n";
		message += "Load to model ID: " + loadTestRunner._modelId + "\n";
		message += "Load based on Enhanced Network ID: " + loadTestRunner._enhNetworkId + "\n";
		if (loadTestRunner._lastIncludedIteration < 0) {
			message += "--Loading all iterations--" + "\n";
		} else {
			message += "Loading iterations 0 thru " + loadTestRunner._lastIncludedIteration + "\n";
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
		
		if (_root.startsWith("file:")) {
			pd = loadModelFromText(
				null, _root.substring(5), _modelId, _enhNetworkId, _lastIncludedIteration);
		} else if (_root.startsWith("package:")) {
			pd = loadModelFromText(
				_root.substring(8), null, _modelId, _enhNetworkId, _lastIncludedIteration);
		} else {
			throw new IllegalArgumentException(
				"A package or directory containing the source files must" +
				"be specified starting with 'file:' or 'package:'"
			);
		}

		ModelBuilder mb = new ModelBuilder(_modelId);
		mb.setEnhNetworkId(_enhNetworkId);
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
	
	/**
	 * Loads a PredictionDataSet from text files based on either a classpath package
	 * or a filesystem directory.
	 * 
	 * A lastIncludedIteration value can be passed so that only a portion of the
	 * iterations are visible.  This value is the last iteration included, so passing
	 * a value of zero will include only the zero iteration.  Passing -1 will
	 * include all iterations.  The iteration passed must exist.
	 * 
	 * @param rootPackage
	 * @param rootDir
	 * @param modelId
	 * @param enhNetworkId
	 * @param iterations
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PredictionDataSet loadModelFromText(
				String rootPackage, String rootDir, long modelId,
				long enhNetworkId, int lastIncludedIteration) throws FileNotFoundException, IOException {

		
		
		PredictionDataSet pd = new PredictionDataSet();
		
		if (rootPackage != null) {
			if (! rootPackage.endsWith("/")) rootPackage = rootPackage + "/";
			
			pd.setAncil( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "ancil.txt"), true) );
			pd.setCoef( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "coef.txt"), true) );
			pd.setSrc( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "src.txt"), true) );
			pd.setTopo( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "topo.txt"), true) );
			
		} else if (rootDir != null){
			File root = new File(rootDir);
			
			pd.setAncil( TabDelimFileUtil.readAsDouble(new File(root, "ancil.txt"), true) );
			pd.setCoef( TabDelimFileUtil.readAsDouble(new File(root, "coef.txt"), true) );
			pd.setSrc( TabDelimFileUtil.readAsDouble(new File(root, "src.txt"), true) );
			pd.setTopo( TabDelimFileUtil.readAsDouble(new File(root, "topo.txt"), true) );
			
			root = null;
		} else {
			throw new IllegalArgumentException("Must specify a rootPackage or rootDir.");
		}
		
		if (lastIncludedIteration > -1) {
			//Find the last row containing this iteration
			int lastRow = pd.getCoef().orderedSearchLast(lastIncludedIteration, 0);
			
			if (lastRow > -1) {
				pd.setCoef( new Data2DView(pd.getCoef(), 0, lastRow + 1, 0, pd.getCoef().getColCount()) );
			} else {
				throw new IllegalArgumentException("Must specify a rootPackage or rootDir.");
			}
			
			
		}
		
		
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
