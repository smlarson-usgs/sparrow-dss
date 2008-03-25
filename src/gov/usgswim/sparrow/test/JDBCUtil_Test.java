package gov.usgswim.sparrow.test;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.FilteredDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.util.JDBCUtil;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import junit.framework.TestCase;
import oracle.jdbc.OracleDriver;
public class JDBCUtil_Test extends TestCase {
	private Connection conn;

	public JDBCUtil_Test(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}

	protected void setUp() throws Exception {
		super.setUp();


		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		conn = DriverManager.getConnection(thinConn,username,password);
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		conn.close();
		conn = null;
	}

	/**
	 * @see JDBCUtil#loadMinimalPredictDataSet(Connection conn, int modelId)
	 */
	public void testLoadMinimalPredictDataSet() throws Exception {
		PredictData ds = JDBCUtil.loadMinimalPredictDataSet(conn, 1);
		PredictRunner ps = new PredictRunner(ds);

		DataTable result = ps.doPredict2();

		DataTableCompare comp = buildPredictionComparison(result);
//		System.out.println(comp.getColumnCount());
//		for (int i = 0; i < comp.getColumnCount(); i++)  {
//			System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
//		}
//		System.out.println("========");
		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);

	}

	public void testReadModelMetadata() throws Exception {
		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(conn);

		Model m = models.get(0);
		Source s1 = m.getSource(1);	//get by identifier
		Source s2 = m.getSource(11);	//get by identifier

		//test that we get the first and last sources
		assertEquals(s1, m.getSources().get(0));	//get via list index
		assertEquals(s2, m.getSources().get(10));	//get via list index


		//model
		assertEquals(1, m.getId().intValue());
		assertEquals("Chesapeake bay", m.getName());
		assertEquals(11, m.getSources().size());

		//1st source
		assertEquals(1, s1.getId().intValue());
		assertEquals("pttn", s1.getName());
		assertEquals(1, s1.getSortOrder());
		assertEquals(1, s1.getModelId().intValue());
		assertEquals(1, s1.getIdentifier());

		//last source
		assertEquals(11, s2.getId().intValue());
		assertEquals("frst", s2.getName());
		assertEquals(11, s2.getSortOrder());
		assertEquals(1, s2.getModelId().intValue());
		assertEquals(11, s2.getIdentifier());

	}

	//TODO:  Should some of these tests be turned back on?

	/**
	 * @see JDBCUtil#JDBCUtil.writePredictDataSet(PredictionDataSet data, Connection conn)
	 * 
	 * Note:  This method does not test this method directly, instead it assumes 
	 * that model 21 was loaded and compare the db values for model 21
	 * to the text files at classpath:  data.ch2007_04_24/
	 * 
	 * Not exactly fullproof.  A better test would be to have a subset.
	 */
	public void xtestDBWriteVsTextFilesDataSet() throws Exception {
		String rootDir = "/data/ch2007_04_24/";
		PredictDataBuilder textDs = new PredictDataBuilder();
		PredictData dbDs = null;


		//Load the text files
		textDs.setAncil( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "ancil.txt"), true, 0) );

		//1st 4 columns are iteration, inc delv, tot_del, and boot error (skip)
		DataTable baseTextCoef = TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "coef.txt"), true, -1);
		//int firstRowBeyondZeroIteration = baseTextCoef.findFirst(0, 1d);

		//For speed, you can choose which section to run below:
		/*
			//Uncomment this section to test only iteration 0.
			textDs.setCoef(
				new Data2DView(baseTextCoef, 0, firstRowBeyondZeroIteration, 4, 11)
			);
		 */

		//Uncomment this section to test all iterations.
		textDs.setCoef(
				new FilteredDataTable(baseTextCoef, 4, 11)
		);


		textDs.setSrc( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "src.txt"), true, -1) );
		textDs.setTopo( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "topo.txt"), true, -1) );


		//Load the db version of the same model
		dbDs = JDBCUtil.loadFullModelDataSet(conn, 21);

		DataTableCompare comp = new DataTableCompare(textDs.getSrc(), dbDs.getSrc());
		DataTableCompare topo = new DataTableCompare(textDs.getTopo(), dbDs.getTopo());
		DataTableCompare coef = new DataTableCompare(textDs.getCoef(), dbDs.getCoef());


		for (int i = 0; i < comp.getColumnCount(); i++)  {
			System.out.println("comp col " + i + " error: " + comp.findMaxCompareValue(i));
		}

		for (int i = 0; i < topo.getColumnCount(); i++)  {
			System.out.println("topo col " + i + " error: " + topo.findMaxCompareValue(i));
		}

		for (int i = 0; i < coef.getColumnCount(); i++)  {
			System.out.println("coef col " + i + " error: " + coef.findMaxCompareValue(i));
			int maxRow = coef.findMaxCompareRow(i);
			System.out.println("--Row Data at max (row #" +  maxRow + ")");
			for (int j=0; j<coef.getColumnCount(); j++) {
				System.out.println("----Col " + j + ": " + coef.getValue(maxRow, j));
			}
		}

		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);
		assertEquals(0d, topo.findMaxCompareValue(), 0.004d);
		assertEquals(0d, coef.findMaxCompareValue(), 0.004d);

	}


	/**
	 * @see JDBCUtil#loadTopo(Connection,int)
	 */
	public void testLoadTopo() throws Exception {
		DataTable jdbcData = JDBCUtil.loadTopo(conn, 1);
		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(4, jdbcData.getColumnCount());

		DataTableCompare comp = buildTopoComparison(jdbcData);

		assertEquals(0, (int) comp.findMaxCompareValue());
	}

	/**
	 * @see JDBCUtil#loadSourceIds(java.sql.Connection,int)
	 */
	public void testLoadSource(Connection conn, int modelId) throws Exception {
		DataTable jdbcData = JDBCUtil.loadSourceIds(conn, 1);
		assertEquals(11, jdbcData.getRowCount());
		assertEquals(1, jdbcData.getColumnCount());

		//This basic set of sources should have id's 0 to 10.
		for (int i = 0; i < 11; i++)  {
			assertEquals(Integer.valueOf(i), jdbcData.getInt(i, 0));
		}

	}

	/**
	 * @see JDBCUtil#loadSourceReachCoef(Connection, int, int, Int2D)
	 */
	public void testLoadSourceReachCoef() throws Exception {
		DataTable sources = JDBCUtil.loadSourceIds(conn, 1);
		DataTable jdbcData = JDBCUtil.loadSourceReachCoef(conn, 1, 0, sources);

		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(11, jdbcData.getColumnCount());

		DataTableCompare comp = buildSourceReachCoefComparison(jdbcData);

		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}


	/**
	 * @see JDBCUtil#loadDecay(Connection, int, int)
	 */
	public void testLoadDecay() throws Exception {
		DataTable jdbcData = JDBCUtil.loadDecay(conn, 1, 0);

		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(2, jdbcData.getColumnCount());

		DataTableCompare comp = buildDecayComparison(jdbcData);

		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}


	/**
	 * @see JDBCUtil#loadSourceValues(Connection, int, Int2D)
	 */
	public void testLoadSourceValues() throws Exception {
		DataTable sources = JDBCUtil.loadSourceIds(conn, 1);
		DataTable jdbcData = JDBCUtil.loadSourceValues(conn, 1, sources);

		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(11, jdbcData.getColumnCount());

		DataTableCompare comp = buildSourceValueComparison(jdbcData);

		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}

	protected DataTableCompare buildTopoComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/topo.txt");
		DataTable data = TabDelimFileUtil.readAsInteger(fileStream, true, -1);

		DataTableCompare comp = new DataTableCompare(data, toBeCompared);

		return comp;
	}

	protected DataTableCompare buildSourceReachCoefComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1));
		DataTable view = new FilteredDataTable(data, 0, firstNonZeroRow, 4, 11);	//Crop to only iteration 0 and remove non-coef columns
		// System.out.println("DEBUG: " + view.getRowCount() + " - " + toBeCompared.getRowCount());
		DataTableCompare comp = new DataTableCompare(view, toBeCompared);

		return comp;
	}

	protected DataTableCompare buildDecayComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1d));
		DataTable view = new FilteredDataTable(data, 0, firstNonZeroRow, 1, 2);	//Crop to only iteration 0 and only the two decay columns

		DataTableCompare comp = new DataTableCompare(view, toBeCompared);

		return comp;
	}

	protected DataTableCompare buildSourceValueComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/src.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);

		DataTableCompare comp = new DataTableCompare(data, toBeCompared);

		return comp;
	}

	protected DataTableCompare buildPredictionComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};

		DataTableCompare comp = new DataTableCompare(toBeCompared, data, DEFAULT_COMP_COLUMN_MAP);

		return comp;
	}
}

