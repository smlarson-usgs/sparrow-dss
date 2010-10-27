package gov.usgswim.sparrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardLongColumnData;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 */
public class ComparePredictionToText {
	
	/**
	 * The required comparison accuracy (expected - actual)/(max(expected, actual))
	 * This value is slightly relaxed for values less than 1.
	 */
	final double REQUIRED_COMPARISON_FRACTION = .001d;	//comp fraction
	
	protected static Logger log =
		Logger.getLogger(ComparePredictionToText.class); //logging for this class
	
	public final static String ID_COL_KEY = "id_col";	//Table property of the key column
	
	//final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = 5;
	//final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = 6;
	//The 'print all non-matching values' option:
	final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = Integer.MAX_VALUE;
	final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = Integer.MAX_VALUE;
	
	final static String QUIT = "quit";
	
	String singleModelPath;
	String activeModelDirectory;
	String dbPwd;
	
	int firstModelId = -1;
	int lastModelId = -1;
	
	//Application lifecycle listener handles startup / shutdown
	static LifecycleListener lifecycle = new LifecycleListener();
	


	public static void main(String[] args) throws Exception {
		//org.junit.runner.JUnitCore.runClasses(ComparePredictionToText.class);
		
		ComparePredictionToText runner = new ComparePredictionToText();
		runner.oneTimeUserInput();
		
		if (runner.singleModelPath != null && runner.dbPwd != null) {
			//run one model
			System.out.println("Running one model...");
			
			runner.oneTimeConfig();
			runner.runTheTests();
			
		} else if (runner.activeModelDirectory != null && runner.firstModelId > 0 &&
				runner.lastModelId > 0 && runner.dbPwd != null) {
			//run a set of models
			System.out.println("Running a directory of models...");
			
			runner.oneTimeConfig();
			runner.runTheTests();
			
		} else {
			//we are quitting
			System.out.println("I'm out of here...  Quit was entered or there are missing values.");
		}
		
		
	}
	

	public void oneTimeUserInput() throws Exception {
		
		promptIntro();
		promptPathOrDir();
		promptPwd();
	}
	
	public void oneTimeConfig() {
		
		//Tell JNDI config to not expect JNDI props
		System.setProperty(
				"gov.usgs.cida.config.DynamicReadOnlyProperties.EXPECT_NON_JNDI_ENVIRONMENT",
				"true");
		
		//Production Properties
		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.152:1521:widw");
		System.setProperty("dbuser", "sparrow_dss");
		System.setProperty("dbpass", dbPwd);
		
		//Test DB Properties - igsarmewdbdev.er.usgs.gov
//		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.154:1521:widev");
//		System.setProperty("dbuser", "sparrow_dss");
		
		//Turns on detailed logging
		log.setLevel(Level.DEBUG);
		
		//Generically turn on logging for Actions
		//log.getLogger(Action.class).setLevel(Level.DEBUG);
		
		//Turn off logging for the lifecycle
		Logger.getLogger(LifecycleListener.class).setLevel(Level.ERROR);
		
		lifecycle.contextInitialized(null, true);
	}
	
	
	public void promptIntro() {
		System.out.println("- - Welcome to the new and improved model validator - -");
		System.out.println("The validator works in two modes:");
		System.out.println("1) Test a single model by entering the complete path to a single model (ending with .txt), or");
		System.out.println("2) Test several models from a directory by entering a directoy (ending with '/' or '\\')");
		System.out.println("If you enter a directory, you will be prompted for a start and end model number.");
		System.out.println("Enter 'quit' for any response to stop.");
		System.out.println("");
	}
	
	public void promptPathOrDir() {
		String pathOrDir  = prompt("Enter a direcotry containing models or the complete path to a single model:");
		
		pathOrDir = pathOrDir.trim();
		if (QUIT.equalsIgnoreCase(pathOrDir)) return;

		if (pathOrDir.endsWith(".txt")) {
			//we have a single path
			singleModelPath = pathOrDir;
		} else if (pathOrDir.endsWith(File.separator)) {
			activeModelDirectory = pathOrDir;
			promptFirstLastModel();
		} else {
			System.out.println("Unrecognized response - please try again.");
			promptPathOrDir();
		}
	}
	
	public void promptFirstLastModel() {
		try {
			String firstIdStr  = prompt("Enter the ID of the first model to test:");
			if (QUIT.equalsIgnoreCase(firstIdStr)) return;
			firstModelId = Integer.parseInt(firstIdStr);
			
			String lastIdStr  = prompt("Enter the ID of the last model to test:");
			if (QUIT.equalsIgnoreCase(lastIdStr)) return;
			lastModelId = Integer.parseInt(lastIdStr);
		} catch (Exception e) {
			System.out.println("I really need numbers for this part.  Lets try that again.");
			promptFirstLastModel();
		}
	}
	
	public void promptPwd() {
		String pwd = prompt("Enter the db password:");
		if (QUIT.equalsIgnoreCase(pwd)) return;
		dbPwd = pwd;
	}
	
	public void beforeEachTest() {
		SharedApplication.getInstance().clearAllCaches();
	}
	
	public void runTheTests() throws Exception {
		
		int failCount = 0;
		
		try {
			Connection conn = SharedApplication.getInstance().getConnection();
			assertTrue(!conn.isClosed());
			conn.close();
		} catch (Exception e) {
			throw new Exception("Oops, a bad pwd, or lack of network access to the db?", e);
		}

		if (singleModelPath != null) {
			File file = new File(singleModelPath);
			if (file.exists()) {
				
				String idString = singleModelPath.substring(0, singleModelPath.lastIndexOf('.'));
				idString = idString.substring(idString.lastIndexOf(File.separatorChar) + 1);
				long id = Long.parseLong(idString);
				
				boolean pass = true;
				pass = pass & testSingleModelDataQuality(id);
				pass = pass & testSingleMmodel(file.toURL(), id);
				if (!pass) failCount++;
			} else {
				throw new Exception("The specified model path does not exist!");
			}
			
		} else {
			
			int modelCount = 0;
			for (long id = firstModelId; id <= lastModelId; id++) {
				
				String filePath = activeModelDirectory + id + ".txt";
				File file = new File(filePath);
				
				if (file.exists()) {
					modelCount++;
					
					boolean pass = true;
					pass = pass & testSingleModelDataQuality(id);
					pass = pass & testSingleMmodel(file.toURL(), id);
					
					if (!pass) failCount++;
				}
			}
			
			if (modelCount == 0) {
				throw new Exception("The specified directory does not contain any models within the first & last model IDs!");
			}
			
		}
		

		if (failCount == 0) {
			log.debug("+ + + + + EVERYTHING LOOKS GREAT! + + + + +");
		} else {
			log.error("- - - - - AT LEAST ONE MODEL FAILED VALIDATION.  PLEASE REVIEW THE LOG MESSAGES TO FIND THE VALIDATION ERRORS. + + + + +");
		}
		assertEquals(0, failCount);
	}
	
	
	/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public boolean testSingleModelDataQuality(Long modelId) throws Exception {
		Connection conn = SharedApplication.getInstance().getConnection();
		
		boolean passed = true;
		try {
			passed = passed & testSingleModelDataQuality(modelId, "CheckNullEnhReach", conn);
			passed = passed & testSingleModelDataQuality(modelId, "CheckNullCalculationAttribs", conn);
			passed = passed & testSingleModelDataQuality(modelId, "CheckNullHucAttribs", conn);
			passed = passed & testSingleModelDataQuality(modelId, "CheckForMissingAttributeRows", conn);
			passed = passed & testSingleModelDataQuality(modelId, "CheckForNullTerminations", conn);
			passed = passed & testSingleModelDataQuality(modelId, "CheckForNullGeometry", conn);
			passed = passed & testSingleModelDataQuality(modelId, "CheckForDuplicateEnhIdsWithNoModelReachGeom", conn);
			
		} finally {
			SharedApplication.closeConnection(conn, null);
		}
		
		if (passed) {
			log.debug("++++++++ Model #" + modelId + " PASSED all data validation tests ++++++++");
		} else {
			//The fail message is printed in the deligated method, so no need to reprint here.
		}
		return passed;
	}
	
	/**
	 * Runs a single QA check.
	 * @param modelId
	 * @param queryName
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public boolean testSingleModelDataQuality(Long modelId, String queryName, Connection conn) throws Exception {

		String[] params = new String[] {"MODEL_ID", modelId.toString()};
		String sql = Action.getTextWithParamSubstitution(queryName, this.getClass(), params);
		
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = null;
		
		try {
			rs = st.executeQuery(sql);
				
			if (rs.next()) {
				log.debug("-------- Model #" + modelId + " FAILED the data validation test '" + queryName + "' - See the properties file for the exact validation query --------");
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			log.error("-------- Model #" + modelId + " FAILED the data validation test '" + queryName + "' with a SQL Error --------", e);
			return false;
		} finally {
			rs.close();
			st.close();
		}
	}
	

	public boolean testSingleMmodel(URL predictFile, Long modelId) throws Exception {
		beforeEachTest();
		boolean pass = true;
		
			
		pass = false;	//assume we fail until we pass
		
		DataTable t = loadTextPredict(predictFile);
		
		if (t != null) {
			AdjustmentGroups ags = new AdjustmentGroups(modelId);
			PredictResult prs = SharedApplication.getInstance().getPredictResult(ags);
			PredictData pd = SharedApplication.getInstance().getPredictData(modelId);
			
			log.setLevel(Level.FATAL);	//Turn off logging
			int noDecayFailures = testComparison(t, prs, pd, false, modelId);
			int decayFailures = testComparison(t, prs, pd, true, modelId);
			log.setLevel(Level.DEBUG);	//Turn back on
			
			if (decayFailures < noDecayFailures) {
				if (decayFailures == 0) {
					log.debug("++++++++ Model #" + modelId + " PASSED using DECAYED incremental values +++++++++");
					pass = true;
				} else {
					log.debug("-------- Model #" + modelId + " FAILED using DECAYED incremental values.  Details: --------");
					testComparison(t, prs, pd, true, modelId);
				}
			} else if (noDecayFailures < decayFailures) {
				if (noDecayFailures == 0) {
					log.debug("++++++++ Model #" + modelId + " PASSED using NON-DECAYED incremental values +++++++++");
					pass = true;
				} else {
					log.debug("-------- Model #" + modelId + " FAILED using NON-DECAYED incremental values.  Details: --------");
					testComparison(t, prs, pd, false, modelId);
				}
			} else if (noDecayFailures == decayFailures) {
				//Equal failures mean there is a row count or other type of error
				log.debug("-------- Model #" + modelId + " FAILED.  MAJOR ISSUE - SEE DETAILS: --------");
				testComparison(t, prs, pd, false, modelId);
			}
		} else {
			log.debug("-------- Model #" + modelId + " FAILED.  MAJOR ISSUE - SEE DETAILS (above) --------");
		}
		
		return pass;

	}

	
	public DataTable loadTextPredict(URL url) throws Exception {
		InputStream is = url.openStream();
		DataTable dt = null;
		
		try {
			dt = readAsDouble(is, true);
		} catch (Exception e) {
			throw new Exception("Error reading: " + url.toString(), e);
		}
		
		return dt;
	}
	
	
	public static Integer findIdColumn(String[] headings) {
		int idCol = -1;
		
		
		idCol = ArrayUtils.indexOf(headings, "local_id");
		if (idCol > -1) {
			return idCol;
		}
		

		idCol = ArrayUtils.indexOf(headings, "mrb_id");
		if (idCol > -1) {
			return idCol;
		}
		

		idCol = ArrayUtils.indexOf(headings, "waterid");
		if (idCol > -1) {
			return idCol;
		}
		
		idCol = ArrayUtils.indexOf(headings, "reach");
		if (idCol > -1) {
			return idCol;
		}
		
		return -1;
	}

	public static DataTable readAsDouble(InputStream source, boolean hasHeadings)
		throws Exception  {

		int indexCol;
		InputStreamReader isr = new InputStreamReader(source);
		BufferedReader br = new BufferedReader(isr);

		try {

			int[] remappedColumns = null;	//indexes to map the columns to
			int mappedColumnCount = 0;	//Number of columns in the output

			String[] headings = (hasHeadings)? TabDelimFileUtil.readHeadings(br): null;
			indexCol = findIdColumn(headings);

			if (indexCol < 0) {
				throw new Exception("Could not find an ID Column");
			}
			
			List<double[]> rows = readDataBodyAsDouble(br, remappedColumns, mappedColumnCount);

			//copy the array list to a double[][] array

			DataTableWritable builder = new SimpleDataTableWritable();
			// Configure the columns
			int numColumns = (headings != null)? headings.length: rows.get(0).length;
			numColumns = (mappedColumnCount > 0)? mappedColumnCount: numColumns;
			for (int i=0; i< numColumns; i++) {
				// no units in this test
				
				String heading = (headings != null)? headings[i]: null;
				ColumnDataWritable column = new StandardNumberColumnDataWritable<Double>(heading, null);
				builder.addColumn(column);
			}

			// Add the data
			boolean debug = false;
			for (int i=0; i<rows.size(); i++) {
				double[] row = rows.get(i);
				int offset = 0;

				for (int j = 0; j<numColumns; j++) {
					builder.setValue(Double.valueOf(row[j + offset]), i, j);
				}
			}
			
			//builder.buildIndex(indexCol);
			builder.setProperty(ID_COL_KEY, Integer.toString(indexCol));
			
			//return builder;
			ColumnData[] cols = new ColumnData[builder.getColumns().length];
			
			System.arraycopy(builder.getColumns(), 0, cols, 0, builder.getColumns().length);
			
			ColumnData oldCol = cols[indexCol];
			StandardLongColumnData newCol = new StandardLongColumnData(oldCol, true, 0L);
			cols[indexCol] = newCol;


			//SimpleDataTable(ColumnData[] columns, String name, String description, Map<String, String> properties, long[] rowIds)
			SimpleDataTable table = new SimpleDataTable(cols, builder.getName(), builder.getDescription(), builder.getProperties(), null);
			
			return table;

		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				//At this point we ignore.
			}
			br = null;
		}
	}
	

	static List<double[]> readDataBodyAsDouble(BufferedReader br, int[] remappedColumns, int mappedColumnCount) throws NumberFormatException, IOException {
		List<double[]> rows = new ArrayList<double[]>(500);
		int colCount = 0; //Number of columns (minus one) - must match in each row
		int curRow = 0;	//Current row number - 0 based;
		String s = null;					//Single line from file
		
		while ((s=br.readLine())!=null) {
			StrTokenizer strTk = new StrTokenizer(s);
			strTk.setIgnoreEmptyTokens(false);
			strTk.setDelimiterChar('\t');
			String src[] = strTk.getTokenArray();
			//String src[] = s.split("\t");

			if ( isNonEmptyLine(src) ) {

				if (colCount == 0) {
					colCount = src.length; // initialize column count
					if (remappedColumns == null) {
						//assign default mapping now
						remappedColumns = new int[colCount];
						for (int i = 0; i < colCount; i++)  {
							remappedColumns[i] = i;
						}

						mappedColumnCount = colCount;
					}
				} else {
					if (src.length != colCount) {
						for (int i = 0; i < src.length; i++)  {
							System.out.println(i + ": " + src[i]);
						}

						throw new IllegalStateException("Parse Error: Row " + curRow + " has " + src.length + " columns, previous columns had " + colCount + " columns.");
					}
				}

				double[] row = new double[mappedColumnCount];

				for (int i = 0; i < src.length; i++)  {
					if (remappedColumns[i] != -1) {
						if (src[i].length() > 0) {
							
							//Simple hack to ignore text columns in the predict.txt file
							
							try {
								row[remappedColumns[i]] = Double.parseDouble( src[i] );
							} catch (Exception e) {
								row[remappedColumns[i]] = Double.NaN;
							}
						} else {
							row[remappedColumns[i]] = 0;
						}
					}
				}

				rows.add(row);
			} else {
				//ignore empty lines
			}
			
			curRow++;
		}
		
		log.debug("Found " + curRow + " rows in predict.txt file.");
		return rows;
	}
	

	public static boolean isNonEmptyLine(String[] src) {
		return src.length > 0 && !(src.length == 1 && ("".equals(src[0])));
	}

	/**
	 * 
	 * @param txt
	 * @param pred
	 * @param predData
	 * @param useDecay
	 * @param modelId
	 * @return The number of comparison errors (zero if no errors)
	 * @throws Exception
	 */
	public int testComparison(DataTable txt, PredictResult pred, PredictData predData, boolean useDecay, long modelId) throws Exception {
		
		if (txt.getRowCount() != pred.getRowCount()) {
			log.error("Model " + modelId + ": Expected " + txt.getRowCount() + " rows, found " + pred.getRowCount());
			return Math.max(txt.getRowCount(),  pred.getRowCount());
		}
		
		String idColStr = txt.getProperty(ID_COL_KEY);
		int idCol = Integer.parseInt(idColStr);
		
		int maxSrc = pred.getSourceCount();	//max source #
		
		int incValueFail = 0;
		int incRowFail = 0;
		int totalValueFail = 0;
		int totalRowFail = 0;
		
		int anyValueFail = 0;
		int anyRowFail = 0;
		
		int goodIncRows = 0;
		int goodTotalRows = 0;
		
		
		for (int r = 0; r < txt.getRowCount(); r++) {
			
			//Instream decay, if it needs to be applied (1 otherwise)
			double instreamDecay = 1;
			if (useDecay) {
				instreamDecay =
					predData.getDelivery().getDouble(r, PredictData.INSTREAM_DECAY_COL);
			}
			
			Long id = pred.getIdForRow(r);
			
			int txtRow = txt.findFirst(idCol, id);
			
			if (txtRow < 0) {
				log.error("Couldn't find the ID " + id);
				return Math.max(txt.getRowCount(),  pred.getRowCount());
			}
			
			boolean rowMatches = true;	//assume this row matches
			boolean incMatches = true;
			boolean totalMatches = true;
			
			//Compare Incremental Values (c is column in std data)
			for (int s = 1; s <= maxSrc; s++) {
			
				double txtIncValue =
						txt.getDouble(txtRow, getIncCol(s, txt, predData));
				double txtTotalValue =
						txt.getDouble(txtRow, getTotalCol(s, txt, predData));

				
				double predIncValue =
					pred.getDouble(r, pred.getIncrementalColForSrc((long) s)) * instreamDecay;
				double predTotalValue =
					pred.getDouble(r, pred.getTotalColForSrc((long) s));
				
				//assertEquals(txtIncValue, predIncValue, 0.0001d);
				//assertEquals(txtTotalValue, predTotalValue, 0.0001d);
				
				
				if (comp(txtIncValue, predIncValue)) {
					//Its good!
				} else {
					rowMatches = false;
					incMatches = false;
					incValueFail++;
					anyValueFail++;
				}
				
				if (comp(txtTotalValue, predTotalValue)) {
					//Its good!
				} else {
					rowMatches = false;
					totalMatches = false;
					totalValueFail++;
					anyValueFail++;
				}
			}
//			
//			{
//				//Check the inc total column
//				double stdVal = stdData.getDouble(r, 11);
//				double predictVal = result.getDouble(predictDataRow, 10);
//				if (Math.abs(stdVal - predictVal) < 0.0001d) {
//					//Its good!
//				} else {
//					rowMatches = false;
//					incMatches = false;
//					incValueFail++;
//					anyValueFail++;
//				}
//			}
//			
//			
//			{
//				//Check the total column
//				double stdVal = stdData.getDouble(r, 12);
//				double predictVal = result.getDouble(predictDataRow, 11);
//				if (Math.abs(stdVal - predictVal) < 0.0001d) {
//					//Its good!
//				} else {
//					rowMatches = false;
//					totalMatches = false;
//					totalValueFail++;
//					anyValueFail++;
//				}
//			}
			
			if (! rowMatches) anyRowFail++;

			
			if (incMatches) {
				goodIncRows++;
				
				//Print the first 10 good INC rows
				if (goodIncRows < 2) {
					//printGoodIncRow(stdData, r, result, predictDataRow);
				}
			} else {
				incRowFail++;
				
				if (incRowFail <= NUMBER_OF_BAD_INCREMENTALS_TO_PRINT) {
					printBadIncRow(txt, txtRow, pred, r, instreamDecay, predData);
				}
			}
			
			if (totalMatches) {
				goodTotalRows++;
				
				//Print the first 10 good INC rows
				if (goodTotalRows < 2) {
					//printGoodTotalRow(stdData, r, result, predictDataRow);
				}
			} else {
				totalRowFail++;
				if (totalRowFail <= NUMBER_OF_BAD_TOTALS_TO_PRINT) {
					printBadTotalRow(txt, txtRow, pred, r, predData);
				}
			}
			
		}
		
		log.debug("Comparison Results for model " + modelId +  " (" + txt.getRowCount() + " rows)");
		log.debug("Non Matching Incremental Values: " + incValueFail +  " (" + incRowFail + " unique rows)");
		log.debug("Non Matching Total Values: " + totalValueFail +  " (" + totalRowFail + " unique rows)");
		log.debug("Non Matching Values (all): " + anyValueFail + " (" + anyRowFail + " rows)");
		
		return anyValueFail;

	}
	
	
	public int getIncCol(int srcNum, DataTable txt, PredictData predictData) throws Exception {
		String incName = "i" + srcNum;
		Integer incCol = txt.getColumnByName(incName);
		
		if (incCol == null) {
			//try alt name
			incName = "PLOAD_INC_" + predictData.getSrcMetadata().getString(srcNum - 1, 1);
			incCol = txt.getColumnByName(incName.toUpperCase());
			
			if (incCol == null) {
				throw new Exception("The incremental column for source " + srcNum +
						" (i" +  srcNum + ") was not found in the file.");
			}
		}
		
		return incCol;
	}
	
	public int getIncAllCol(DataTable txt) throws Exception {
		String incName = "ia";
		Integer incCol = txt.getColumnByName(incName);
		
		if (incCol == null) {
			//try alt name
			incCol = txt.getColumnByName("PLOAD_INC_TOTAL");
			
			if (incCol == null) {
				throw new Exception("The incremental column for all sources " +
						" (ia) was not found in the file.");
			}
		}
		
		return incCol;
	}
	
	public int getTotalCol(int srcNum, DataTable txt, PredictData predictData) throws Exception {
		String totalName = "t" + srcNum;
		Integer totalCol = txt.getColumnByName(totalName);
		
		if (totalCol == null) {
			//try alt name
			totalName = "PLOAD_" + predictData.getSrcMetadata().getString(srcNum - 1, 1);
			totalCol = txt.getColumnByName(totalName.toUpperCase());	
			
			if (totalCol == null) {
				throw new Exception("The total column for source " + srcNum +
						" (t" +  srcNum + ") was not found in the file.");
			}
		}
		
		return totalCol;
	}
	
	public int getTotalAllCol(DataTable txt) throws Exception {
		String totalName = "ta";
		Integer totalCol = txt.getColumnByName(totalName);
		
		
		if (totalCol == null) {
			//try alt name

			totalCol = txt.getColumnByName("PLOAD_TOTAL");	
			if (totalCol == null) {
				throw new Exception("The total column for all sources" + 
				" (ta) was not found in the file.");
			}
		}
		
		return totalCol;
	}
	
	/**
	 * Compares two values and returns true if they are considered equal.
	 * Note that only positive values are expected.  If a negative value
	 * is received for any value, false is returned.
	 * 
	 * The comparison is done on a sliding scale:  values less than ten require
	 * a bit less accuracy.
	 * 
	 * @param expect
	 * @param compare
	 * @return
	 */
	public boolean comp(double expect, double compare) {
		
		if (expect < 0 || compare < 0) {
			return false;
		}
		
		double diff = Math.abs(compare - expect);
		double frac = 0;
		
		if (diff == 0) {
			return true;	//no further comparison required
		}
		
		if (expect < 1d) {
			return (diff < (REQUIRED_COMPARISON_FRACTION * 10d));
		} else {
			frac = diff / expect;	//we are sure at this point that baseValue > 0
		}
		
		if (expect < 10) {
			return frac < (REQUIRED_COMPARISON_FRACTION * 5L);
		} else if (expect < 20) {
			return frac < (REQUIRED_COMPARISON_FRACTION * 2L);
		} else {
			return frac < REQUIRED_COMPARISON_FRACTION;
		}
	}
	
	public void printBadIncRow(DataTable txt, int txtRow, PredictResult pred,
			int predRow, double instreamDecay, PredictData predictData) throws Exception {
		
		long id = pred.getIdForRow(predRow);
		log.debug("** Failed INC values for reach ID " + id + " (row " + predRow + ")");
		
		//Compare Incremental Values (c is column in std data)
		for (int s=1; s <= pred.getSourceCount(); s++) {
			
			double txtIncValue = txt.getDouble(txtRow, getIncCol(s, txt, predictData));
			double predIncValue = pred.getDouble(predRow, pred.getIncrementalColForSrc((long) s));
			predIncValue = predIncValue * instreamDecay;	//correct for instrem decay

			String line;
			if (comp(txtIncValue, predIncValue)) {
				line = "|\tInc " + s;
			} else {
				line = "|>\tInc " + s;
			}
			
			line = line + " " + txtIncValue + " | " + predIncValue + " |";
			log.debug(line);
		}
	}
	
	
	public void printBadTotalRow(DataTable txt, int txtRow, PredictResult pred,
			int predRow, PredictData predictData) throws Exception {
		
		long id = pred.getIdForRow(predRow);
		log.debug("** Failed TOTAL values for reach ID " + id + " (row " + predRow + ")");
		
		//Compare Incremental Values (c is column in std data)
		for (int s=1; s <= pred.getSourceCount(); s++) {

			double txtTotalValue = txt.getDouble(txtRow, getTotalCol(s, txt, predictData));
			double predTotalValue = pred.getDouble(predRow, pred.getTotalColForSrc((long) s));

			String line;
			if (comp(txtTotalValue, predTotalValue)) {
				line = "|\tTot " + s;
			} else {
				line = "|>\tTot " + s;
			}
			
			line = line + " " + txtTotalValue + " | " + predTotalValue + " |";
			log.debug(line);
		}
	}
	
	public static String prompt(String prompt) {
	
			      //  prompt the user to enter their name
		  System.out.print(prompt);
		
		  //  open up standard input
		  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		  String val = null;
		
		  //  read the username from the command-line; need to use try/catch with the
		  //  readLine() method
		  try {
		     val = br.readLine();
		  } catch (IOException ioe) {
		     System.out.println("IO error trying to read input!");
		     System.exit(1);
		  } finally {
			  //br.close();
		  }
		  
		  return val;
	  }
	

	
}

