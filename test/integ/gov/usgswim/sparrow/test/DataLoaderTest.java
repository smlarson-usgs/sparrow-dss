package gov.usgswim.sparrow.test;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ColumnMappedTable;
import gov.usgswim.datatable.adjustment.FilteredDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import oracle.jdbc.OracleDriver;

public class DataLoaderTest extends DataLoaderOfflineTest {

	private Connection conn;


	public DataLoaderTest(String sTestName) {
		super(sTestName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();


		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		conn = DriverManager.getConnection(thinConn,username,password);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		conn.close();
		conn = null;
	}

//	public void testReadSystemInfo() throws Exception {
//	DataTable jdbcData = JDBCUtil.loadSystemInfo(conn, 22);
//	DataTable dldata = DataLoader.loadSystemInfo(conn, 22);

//	DataTableCompare comp = new DataTableCompare(jdbcData, dldata);


//	for (int c=0; c<comp.getColumnCount(); c++) {
//	int r = comp.findMaxCompareRow(c);

//	System.out.println("*** max compare in column " + c + " is row " + r);
//	System.out.println("Expected: " + jdbcData.getValue(r, c) + " Actual: " + dldata.getValue(r, c));
//	}

//	assertEquals(0d, comp.findMaxCompareValue(), .000000000000001d);
//	//dumpComparison(jdbcData, dldata, 999999);

//	}



	/*
	 * TODO This testDoFullCompare() test skipped until problem of loading 51
	 * iterations into memory is resolved
	 */
//	public void testDoFullCompare() throws Exception {

//	DataTableCompare comp = null;	//used for all comparisons

//	PredictData2 expect = JDBCUtil2.loadFullModelDataSet(conn, 22);
//	PredictData2 data = DataLoader.loadFullModelDataSet(conn, 22);

//	PredictRunner2 expectPr = new PredictRunner2(expect);
//	PredictRunner2 dataPr = new PredictRunner2(data);

//	comp = new DataTableCompare(expect.getCoef(), data.getCoef());
//	assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

//	comp = new DataTableCompare(expect.getDecay(), data.getDecay());
//	assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

//	comp = new DataTableCompare(expect.getSrc(), data.getSrc());
//	assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

//	comp = new DataTableCompare(expect.getSrcIds(), data.getSrcIds());
//	assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

//	comp = new DataTableCompare(expect.getSys(), data.getSys());
//	assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

//	comp = new DataTableCompare(expect.getTopo(), data.getTopo());
//	//assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
//	for (int i = 0; i < comp.getColumnCount(); i++)  {
//	System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
//	int row = comp.findMaxCompareRow(i);
//	System.out.println("id: " + expect.getTopo().getIdForRow(row));
//	System.out.println("expected: " + expect.getTopo().getValue(row, i));
//	System.out.println("found: " + data.getTopo().getValue(row, i));
//	}

//	// now compare results
//	comp = null;
//	DataTable expectResult = expectPr.doPredict2();
//	expectPr = null;
//	DataTable dataResult = dataPr.doPredict2();
//	dataPr = null;

//	//Compare result values
//	comp = new DataTableCompare(expectResult, dataResult);
//	assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

//	}

	// ======================================================
	public void testReadModelMetadata() throws Exception {
		List<ModelBuilder> models = DataLoader.loadModelMetaData(conn);

		Model m = models.get(0);
		Source s1 = m.getSource(1);	//get by identifier
		Source s2 = m.getSource(5);	//get by identifier

		//test that we get the first and last sources
		assertEquals(s1, m.getSources().get(0));	//get via list index
		assertEquals(s2, m.getSources().get(4));	//get via list index


		//model
		assertEquals(22, m.getId().intValue());
		assertEquals("National Total Nitrogen Model - 1987", m.getName());
		assertEquals(5, m.getSources().size());

		//1st source
		assertEquals(1, s1.getId().intValue());
		assertEquals("POINT", s1.getName());
		assertEquals(1, s1.getSortOrder());
		assertEquals(22, s1.getModelId().intValue());
		assertEquals(1, s1.getIdentifier());

		//last source
		assertEquals(5, s2.getId().intValue());
		assertEquals("NONAGR", s2.getName());
		assertEquals(5, s2.getSortOrder());
		assertEquals(22, s2.getModelId().intValue());
		assertEquals(5, s2.getIdentifier());

	}



	/**
	 * @see DataLoader#loadTopo(Connection,int)
	 */
	public void testLoadTopo() throws Exception {
		DataTable jdbcData = DataLoader.loadTopo(conn, 1);
		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(4, jdbcData.getColumnCount());

		DataTableCompare comp = buildTopoComparison(jdbcData);

		assertEquals(Integer.valueOf(0), comp.getMaxInt());
	}
	


	/**
	 * @see DataLoader#loadSourceIds(java.sql.Connection,int)
	 */
	public void testLoadSource(Connection conn, int modelId) throws Exception {
		DataTable jdbcData = DataLoader.loadSrcMetadata(conn, 1);
		assertEquals(11, jdbcData.getRowCount());
		assertEquals(1, jdbcData.getColumnCount());

		//This basic set of sources should have id's 0 to 10.
		for (int i = 0; i < 11; i++)  {
			assertEquals(Integer.valueOf(i), jdbcData.getInt(i, 0));
		}

	}

	/**
	 * @see DataLoader#loadSourceReachCoef(Connection, int, int, Int2D)
	 */
	public void testLoadSourceReachCoef() throws Exception {
		DataTable sources = DataLoader.loadSrcMetadata(conn, 1);
		DataTable jdbcData = DataLoader.loadSourceReachCoef(conn, 1, 0, sources);

		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(11, jdbcData.getColumnCount());

		DataTableCompare comp = buildSourceReachCoefComparison(jdbcData);

		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);
	}


	/**
	 * @see DataLoader#loadDecay(Connection, int, int)
	 */
	public void testLoadDecay() throws Exception {
		DataTable jdbcData = DataLoader.loadDecay(conn, 1, 0);

		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(2, jdbcData.getColumnCount());

		DataTableCompare comp = buildDecayComparison(jdbcData);

		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);
	}

	/**
	 * @see DataLoader#loadSourceValues(Connection, int, Int2D)
	 */
	public void testLoadSourceValues() throws Exception {
		DataTable sources = DataLoader.loadSrcMetadata(conn, 1);
		DataTable jdbcData = DataLoader.loadSourceValues(conn, 1, sources);

		assertEquals(2339, jdbcData.getRowCount());
		assertEquals(11, jdbcData.getColumnCount());

		DataTableCompare comp = buildSourceValueComparison(jdbcData);

		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);
	}
	
	
	public void testLoadMinimalPredictDataSet() throws Exception {
		PredictData ds = DataLoader.loadMinimalPredictDataSet(conn, 1);
		PredictRunner ps = new PredictRunner(ds);

		DataTable result = ps.doPredict();

		DataTableCompare comp = buildPredictionComparison(result);

//		for (int i = 0; i < comp.getColumnCount(); i++)  {
//			System.out.println("col " + i + " error: " + comp.getMaxDouble(i));
//		}

		assertEquals(0d, comp.getMaxDouble(), 0.004d);

	}
	
	
	// ==============
	// HELPER METHODS
	// ==============

	protected DataTableCompare buildTopoComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/topo.txt");
		DataTable data = TabDelimFileUtil.readAsInteger(fileStream, true, -1);

		DataTableCompare comp= new DataTableCompare(data, toBeCompared, true);
		return comp;
	}

	protected DataTableCompare buildSourceReachCoefComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1));
		DataTable view = new FilteredDataTable(data, 0, firstNonZeroRow, 4, 11); //Crop to only iteration 0 and remove non-coef columns

		DataTableCompare comp= new DataTableCompare(view, toBeCompared, true);

		return comp;
	}

	protected DataTableCompare buildDecayComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1));
		DataTable view = new FilteredDataTable(data, 0, firstNonZeroRow, 1, 2);	//Crop to only iteration 0 and only the two decay columns

		DataTableCompare comp= new DataTableCompare(view, toBeCompared, true);

		return comp;
	}

	protected DataTableCompare buildSourceValueComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/src.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);

		DataTableCompare comp= new DataTableCompare(data, toBeCompared, true);

		return comp;
	}
	
	protected DataTableCompare buildPredictionComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};
		ColumnMappedTable mappedTable = new ColumnMappedTable(data, DEFAULT_COMP_COLUMN_MAP);
		
		DataTableCompare comp = new DataTableCompare(toBeCompared, mappedTable, true);

		return comp;
	}


}
