package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.Arrays;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardLongColumnData;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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
	
	final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = 5;
	final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = 6;
	
	static int firstModelId;
	static int lastModelId;
	
	//Application lifecycle listener handles startup / shutdown
	static LifecycleListener lifecycle = new LifecycleListener();
	
	final static String TEXT_PATH = "/model_predict/";

	public static void main(String[] args) {
		org.junit.runner.JUnitCore.runClasses(ComparePredictionToText.class);
	}
	
	@BeforeClass
	public static void setUp() throws Exception {

		String firstIdStr  = prompt("Enter the ID of the first model to test:");
		String lastIdStr  = prompt("Enter the ID of the last model to test:");
		String pwd = prompt("Enter the db password:");
		
		firstModelId = Integer.parseInt(firstIdStr);
		lastModelId = Integer.parseInt(lastIdStr);
		
		//Turns on detailed logging
		log.setLevel(Level.DEBUG);
		
		//Generically turn on logging for Actions
		log.getLogger(Action.class).setLevel(Level.DEBUG);
		
		//Turn off logging for the lifecycle
		log.getLogger(LifecycleListener.class).setLevel(Level.ERROR);
		
		lifecycle.contextInitialized(null, true);
		
		//Production Properties
		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.152:1521:widw");
		System.setProperty("dbuser", "sparrow_dss");
		System.setProperty("dbpass", pwd);
		
		//Test DB Properties - igsarmewdbdev.er.usgs.gov
//		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.154:1521:widev");
//		System.setProperty("dbuser", "sparrow_dss");

	}
	
	@Before
	public void beforeTest() {
		SharedApplication.getInstance().clearAllCaches();
	}
	
	@Test
	public void testAll() throws Exception {
		
		int failCount = 0;
		
		try {
			Connection conn = SharedApplication.getInstance().getConnection();
			assertTrue(!conn.isClosed());
		} catch (Exception e) {
			throw new Exception("Oops, a bad pwd, or lack of network access to the db?", e);
		}

		for (long id = firstModelId; id <= lastModelId; id++) {
			URL url = getTextURL(id);
			if (url != null) {
				
				boolean pass = testSingleMmodel(url, id);
				if (!pass) failCount++;

			}
		}
		
		assertEquals(0, failCount);
	}
	
	
	//@Test
	public void testFileRead() throws Exception {
		final int readSize = 8192;
		
		String testFile = "/datausgs/eclipse galileo workspace/sparrow_core/src/test/resources/integ/model_predict/23.txt";
		File file = new File(testFile);
		FileInputStream is = new FileInputStream(file);
		
		
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		StringBuffer curLine = new StringBuffer();
		StringBuffer nextLine = new StringBuffer();
		char cbuf[] = new char[readSize];	//current chunk
		int cbufCount = -1;
		HashSet<String> lineTerms = new HashSet<String>();
		int singleCharLineTerms = 0;
		int lineCount = 0;
		
		cbufCount = br.read(cbuf);
		
		while (cbufCount > 0) {
			boolean lastCharRN = false;
			boolean inRNSection = false;
			String combinedRNSection = "";
			
			for (int i=0; i < cbufCount; i++) {
				char c = cbuf[i];
				
				if (c == '\r' || c == '\n') {
					if (lastCharRN || inRNSection) {
						combinedRNSection = combinedRNSection + c;
						inRNSection = true;
					} else {
						lastCharRN = true;
						combinedRNSection = combinedRNSection + c;
					}
				} else {
					if (lastCharRN && ! inRNSection) {
						//We failed to have two in a row - this is an issue
						singleCharLineTerms++;
						
						combinedRNSection = "";
						inRNSection = false;
						lastCharRN = false;
					} else if (inRNSection) {
						lineTerms.add(combinedRNSection);
						
						//current line is complete
						lineCount++;
						
						combinedRNSection = "";
						inRNSection = false;
						lastCharRN = false;
					} else {
						//curLine.append(c);
					}
				}
				
				
			}
			
			cbufCount = br.read(cbuf);
			
		}
		
		
		System.out.println(lineCount + " Lines");
		System.out.println(singleCharLineTerms + " Single char line terms");
		
		
		Iterator<String> it = lineTerms.iterator();
		while (it.hasNext()) {
			String line = it.next();
			String outLine = "";
			
			for (int i=0; i < line.length(); i++) {
				if (line.charAt(i) == '\r') {
					outLine = outLine + "r";
				} else if (line.charAt(i) == '\n') {
					outLine = outLine + "n";
				} else {
					outLine = outLine + "x";
				}
				
			}
			System.out.println(outLine);
		}
		
	}
	
	//@Test
	public void test22FromFolder() throws Exception {
		
		///datausgs/tmp/tp_dss_folder/predict_edit_headings.txt
		long id = 23;
		URL url = new URL("file:///datausgs/tmp/tp_dss_folder/predict_edit_headings.txt");

		boolean pass = testSingleMmodel(url, id);
		
		assertTrue(pass);

	}
	

	public boolean testSingleMmodel(URL predictFile, Long modelId) throws Exception {
		
		boolean pass = false;	//assume we fail until we pass
		
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
					log.debug("++++++++ Model #" + modelId + " FAILED using DECAYED incremental values.  Details: ++++++++");
					testComparison(t, prs, pd, true, modelId);
				}
			} else if (noDecayFailures < decayFailures) {
				if (noDecayFailures == 0) {
					log.debug("++++++++ Model #" + modelId + " PASSED using NON-DECAYED incremental values +++++++++");
					pass = true;
				} else {
					log.debug("++++++++ Model #" + modelId + " FAILED using NON-DECAYED incremental values.  Details: ++++++++");
					testComparison(t, prs, pd, false, modelId);
				}
			} else if (noDecayFailures == decayFailures) {
				//Equal failures mean there is a row count or other type of error
				log.debug("++++++++ Model #" + modelId + " FAILED.  MAJOR ISSUE - SEE DETAILS: ++++++++");
				testComparison(t, prs, pd, false, modelId);
			}
		} else {
			log.debug("++++++++ Model #" + modelId + " FAILED.  MAJOR ISSUE - SEE DETAILS (above) ++++++++");
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
	
	public URL getTextURL(Long modelId) throws Exception {
		String modelPath = TEXT_PATH + modelId.toString() + ".txt";
		URL url = this.getClass().getResource(modelPath);
		return url;
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
						txt.getDouble(txtRow, getIncCol(s, txt));
				double txtTotalValue =
						txt.getDouble(txtRow, getTotalCol(s, txt));

				
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
					printBadIncRow(txt, txtRow, pred, r, instreamDecay);
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
					printBadTotalRow(txt, txtRow, pred, r);
				}
			}
			
		}
		
		log.debug("Comparison Results for model " + modelId +  " (" + txt.getRowCount() + " rows)");
		log.debug("Non Matching Incremental Values: " + incValueFail +  " (" + incRowFail + " unique rows)");
		log.debug("Non Matching Total Values: " + totalValueFail +  " (" + totalRowFail + " unique rows)");
		log.debug("Non Matching Values (all): " + anyValueFail + " (" + anyRowFail + " rows)");
		
		return anyValueFail;

	}
	
	
	public int getIncCol(int srcNum, DataTable txt) throws Exception {
		String incName = "i" + srcNum;
		Integer incCol = txt.getColumnByName(incName);
		
		if (incCol == null) {
			throw new Exception("The incremental column for source " + srcNum +
					" (i" +  srcNum + ") was not found in the file.");
		}
		
		return incCol;
	}
	
	public int getIncAllCol(DataTable txt) throws Exception {
		String incName = "ia";
		Integer incCol = txt.getColumnByName(incName);
		
		if (incCol == null) {
			throw new Exception("The incremental column for all sources " +
					" (ia) was not found in the file.");
		}
		
		return incCol;
	}
	
	public int getTotalCol(int srcNum, DataTable txt) throws Exception {
		String totalName = "t" + srcNum;
		Integer totalCol = txt.getColumnByName(totalName);
		
		if (totalCol == null) {
			throw new Exception("The total column for source " + srcNum +
					" (t" +  srcNum + ") was not found in the file.");
		}
		
		return totalCol;
	}
	
	public int getTotalAllCol(DataTable txt) throws Exception {
		String totalName = "ta";
		Integer totalCol = txt.getColumnByName(totalName);
		
		if (totalCol == null) {
			throw new Exception("The total column for all sources" + 
					" (ta) was not found in the file.");
		}
		
		return totalCol;
	}
	
	@Test
	public void checkComparison() {
		assertTrue(comp(0d, 0d));
		assertTrue(comp(1000d, 1000.1d));
		assertTrue(comp(1000d, 1000.9999d));
		assertFalse(comp(1000d, 1001.1d));
		assertTrue(comp(.1d, .11d));
		assertFalse(comp(.1d, .111d));
		assertTrue(comp(1d, 1.005d));
		assertFalse(comp(1d, 1.0051d));
		
		//Specific comparisons
		assertTrue(comp(.00073676269211912d, 0d));
		assertTrue(comp(2.1328272875115d, 2.1402908936369247d));
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
	
	public void printBadIncRow(DataTable txt, int txtRow, PredictResult pred, int predRow, double instreamDecay) throws Exception {
		
		long id = pred.getIdForRow(predRow);
		log.debug("** Failed INC values for reach ID " + id + " (row " + predRow + ")");
		
		//Compare Incremental Values (c is column in std data)
		for (int s=1; s <= pred.getSourceCount(); s++) {
			
			double txtIncValue = txt.getDouble(txtRow, getIncCol(s, txt));
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
	
	
	public void printBadTotalRow(DataTable txt, int txtRow, PredictResult pred, int predRow) throws Exception {
		
		long id = pred.getIdForRow(predRow);
		log.debug("** Failed TOTAL values for reach ID " + id + " (row " + predRow + ")");
		
		//Compare Incremental Values (c is column in std data)
		for (int s=1; s <= pred.getSourceCount(); s++) {

			double txtTotalValue = txt.getDouble(txtRow, getTotalCol(s, txt));
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

