package gov.usgswim.sparrow.loader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import oracle.jdbc.driver.OracleDriver;

public class InsertLoadRunner {

	private static final String THIN_LOCAL_CONNECTION = "jdbc:oracle:thin:@localhost:1521:xe";

	public static enum RunMode{
		COMMIT_IF_NO_ERRORS, BATCH_MODE, COMMIT_AND_LOG_ERRORS, ROLLBACK_IF_ERROR
	};

	public static Connection conn = ModelDataLoader.getWIMAPConnection();
//	public static Connection conn = ModelDataLoader.getWIDWConnection();
//	public static Connection conn = InsertLoadRunner.getDevelopmentConnection();
	public static String fileName = null;
	
	static {
//		fileName = "model_metadata_insert_20081002_163018.sql";
//		fileName = "src_metadata_insert_20081002_163018.sql";	
		fileName = "model_reaches_insert_20081002_163018.sql";

//		fileName = "reach_decay_coef_insert_20081003_120811.sql";
//		fileName = "src_reach_coef_insert_20081003_120811.sql";
//		fileName = "src_value_insert_20081003_120812.sql";


		
//		fileName = "model_reaches_insert_20081002_163018.sql";
//		fileName = "model_reaches_insert_20080930_170536.sql";
//		fileName = "reach_decay_coef_insert_20080930_170552.sql";
//		fileName = "src_reach_coef_insert_20080930_170559.sql";
//		fileName = "src_value_insert_20080930_170609.sql";
	}

	public static String baseDirectory = "C:/Documents and Settings/ilinkuo/Desktop/DaleMRB/load" ;
	
	public static void main(String[] args) throws IOException, SQLException {
		File testInsertFile = new File(baseDirectory + "/" + fileName);
		
		try {
			InsertLoadRunner.load(conn, testInsertFile);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			if (conn != null) conn.close();
		}
		
	}
	
	// =============================================
	public static void load(Connection conn, File sqlFile ) throws IOException, SQLException {
		assert(sqlFile.exists()): "no such sql file exists: " + sqlFile.getAbsolutePath();
		assert(sqlFile.isFile()): "sqlFile argument must be a file, not a directory";
		BufferedWriter doLog = getLogFileWriter(sqlFile);
		BufferedWriter errorLog = getErrorFileWriter(sqlFile);


		BufferedReader reader = new BufferedReader(new FileReader(sqlFile));

		{	// configure connection
			conn.setAutoCommit(false);
		}

		String line = null;
		long lineNum = 0;
		startDoLog(doLog, "load");

		while ((line = reader.readLine()) != null){
			lineNum++;
			if (!line.startsWith("INSERT") && !line.startsWith("insert")) {
				doLog.write(line + "\n");
				doLog.flush(); // output the line immediately
				errorLog.write("not an insert");
				errorLog.flush();
			} else {
				Statement stmt = null;
				try {
					stmt = conn.createStatement();
					// an sql file may have been produced with a different
					// consumer in mind, such as sqlplus. In java, ending
					// semicolons are not allowed
					String query = removeFinalSemiColon(line);
					stmt.execute(query);
					doLog.write(lineNum + "\n");
				} catch (SQLException e) {
					doLog.write(line + "\n");
					doLog.flush(); // output the line immediately
					errorLog.write(e.getMessage());
					errorLog.flush();
				} finally {
					if (stmt != null) stmt.close();
				}

				if (lineNum % 1024 == 0) {
					// periodically commit
					try {
						stmt = conn.createStatement();
						stmt.execute("commit");
						doLog.write("committed at line=" + lineNum + "\n");
					} finally  {
						if (stmt != null) stmt.close();
					}

				}
			}
			// trial

		}
		{	// commit the final batch
			Statement stmt = conn.createStatement();
			stmt.execute("commit");
			doLog.write("committed at end: line=" + lineNum);
		}
	}

	private static void startDoLog(BufferedWriter doLog, String methodName) throws IOException {
		SimpleDateFormat formatter =  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String logStart = "=============== " + methodName + " " + formatter.format(new Date()) + " ================\n";
		doLog.write(logStart);
		doLog.flush();		
	}

	private static String removeFinalSemiColon(String line) {
		String result = line.trim();
		if (line.lastIndexOf(';') == line.length() - 1) {
			// cut the last semi-colon
			result = line.substring(0, line.length() - 1);
		}
		return result;
	}

	static BufferedWriter getLogFileWriter(File baseFile) throws IOException {
		File parent = baseFile.getParentFile();
		String logFileName = ModelDataLoader.appendSuffixToFileName(baseFile.getName(), "_log");
		File logErrFile = new File(parent.getAbsolutePath() + "/" + logFileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(logErrFile, true));
		return writer;
	}

	static BufferedWriter getErrorFileWriter(File baseFile) throws IOException {
		File parent = baseFile.getParentFile();
		String logFileName = ModelDataLoader.appendSuffixToFileName(baseFile.getName(), "_err");
		File logErrFile = new File(parent.getAbsolutePath() + "/" + logFileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(logErrFile, false));
		// rewrite the errors each time.
		return writer;
	}

	public static Connection getDevelopmentConnection() {
		try {
			DriverManager.registerDriver(new OracleDriver());		

			String username = "SPARROW_DSS";
			String password = "admin"; // this password doesn't matter as it's just a local password
			String thinConn = THIN_LOCAL_CONNECTION;

			return DriverManager.getConnection(thinConn,username,password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		return null;
	}
}
