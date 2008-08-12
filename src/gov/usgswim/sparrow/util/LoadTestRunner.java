package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import oracle.jdbc.OracleDriver;

import org.apache.log4j.Logger;


public class LoadTestRunner {
	protected static Logger log = Logger.getLogger(LoadTestRunner.class); //logging for this class

	public static String DATA_ROOT_DIR = "/data/ch2007_04_24/";

	// RUN parameters
	private String _root;
	private long _modelId;
	long _enhNetworkId;
	private boolean _loadAllIterations = true;	//default include all iterations
	private boolean _commitChanges = true;		//default to commiting changes to db

	public LoadTestRunner() {
	}

	/**
	 * Command-line interface for loading model data
	 * @param args
	 * 	arg[0] = root directory for data files
	 * 	arg[1] = model id
	 * 	arg[2] = enhanced network id
	 * 	arg[3] = [true|false] load all iterations
	 * 	arg[4] = [true|false] commit changes
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LoadTestRunner loadTestRunner = new LoadTestRunner();

		// Read in command-line arguments
		loadTestRunner._root = args[0];
		loadTestRunner._modelId = Long.parseLong(args[1]);
		loadTestRunner._enhNetworkId = Long.parseLong(args[2]);
		if (args.length > 3) {
			loadTestRunner._loadAllIterations = Boolean.parseBoolean(args[3]);
		}
		if (args.length > 4) {
			loadTestRunner._commitChanges = Boolean.parseBoolean(args[4]);
		}

		// Construct command-line user feedback
		String message = "Root to load from: " + loadTestRunner._root + "\n";
		message += "Load to model ID: " + loadTestRunner._modelId + "\n";
		message += "Load based on Enhanced Network ID: " + loadTestRunner._enhNetworkId + "\n";
		if (loadTestRunner._loadAllIterations) {
			message += "--Loading all iterations--" + "\n";
		} else {
			message += "Loading only the zero iteration\n";
		}
		if (loadTestRunner._commitChanges) {
			message += "--COMMITTING changes to the db--" + "\n";
		} else {
			message += "--ROLLING BACK changes to the db--" + "\n";
		}
		// Confirmation prompt
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

		PredictData pd;

		long initStartTime = System.currentTimeMillis();
		long fileReadStart = initStartTime;

		if (_root.startsWith("file:")) {
			pd = TabDelimFileUtil.loadPredictDataSet(
					null, _root.substring(5), _modelId, _enhNetworkId, !_loadAllIterations, true);
		} else if (_root.startsWith("package:")) {
			pd = TabDelimFileUtil.loadPredictDataSet(
					_root.substring(8), null, _modelId, _enhNetworkId, !_loadAllIterations, true);
		} else {
			throw new IllegalArgumentException(
					"A package or directory containing the source files must" +
					"be specified starting with 'file:' or 'package:'"
			);
		}

		PredictDataBuilder pdb = pd.getBuilder();

		long fileReadEnd = System.currentTimeMillis();
		log.debug("Load time for text files was: " + (fileReadEnd-fileReadStart)/1000 + " (sec)");

		ModelBuilder mb = new ModelBuilder(_modelId);
		mb.setEnhNetworkId(_enhNetworkId);
		Model model = mb.toImmutable();

		pdb.setModel( model );

		Connection conn = getConnection();

		long startTime = System.currentTimeMillis();

		try {

			conn.setAutoCommit(false);
			int count = JDBCUtil.writePredictDataSet(pdb, conn, 200);

			log.debug("Added " + count + " records to the db.");

			if (_commitChanges) {
				log.debug("Committing Changes...");
				long start = System.currentTimeMillis();
				conn.commit();
				log.debug("Commit Complete in " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
			} else {
				log.debug("Rolling back Changes (as requested)...");
				long start = System.currentTimeMillis();
				conn.rollback();
				log.debug("Rollback Complete in " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
			}


		} catch (Exception e) {

			log.error("Exception during load:");
			e.printStackTrace(System.err);
			log.error("Rolling back Changes b/c of the error...");
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
