package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.util.DataLoader;

import gov.usgswim.sparrow.util.JDBCUtil;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.util.List;

import junit.swingui.TestRunner;

import oracle.jdbc.OracleDriver;

public class DataLoaderTest extends DataLoaderOfflineTest {
	
	private Connection conn;
	
	
	public DataLoaderTest(String sTestName) {
		super(sTestName);
	}
	
	public static void main(String args[]) {
		String args2[] = {"-noloading", "gov.usgswim.sparrow.test.DataLoaderTest"};
		TestRunner.main(args2);
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
	
	public void testReadSystemInfo() throws Exception {
		Data2D jdbcData = JDBCUtil.loadSystemInfo(conn, 22);
		Data2D dldata = DataLoader.loadSystemInfo(conn, 22);
		
		Data2DCompare comp = new Data2DCompare(jdbcData, dldata);
		
		
		for (int c=0; c<comp.getColCount(); c++) {
			int r = comp.findMaxCompareRow(c);
			
			System.out.println("*** max compare in column " + c + " is row " + r);
			System.out.println("Expected: " + jdbcData.getValue(r, c) + " Actual: " + dldata.getValue(r, c));
		}
		
	  this.assertEquals(0d, comp.findMaxCompareValue(), .000000000000001d);
		//dumpComparison(jdbcData, dldata, 999999);
		
	}
	
	/**
	 * @see DataLoader#loadMinimalPredictDataSet(Connection conn, int modelId)
	 */
	public void testLoadMinimalPredictDataSet() throws Exception {
		PredictData ds = DataLoader.loadMinimalPredictDataSet(conn, 1);
		PredictRunner ps = new PredictRunner(ds);
		
		Double2DImm result = ps.doPredict();

		Data2DCompare comp = buildPredictionComparison(result);
		
		for (int i = 0; i < comp.getColCount(); i++)  {
			System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
		}
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);

	}
	
	public void testDoFullCompare() throws Exception {
		
		Data2DCompare comp = null;	//used for all comparisons
		
		PredictData expect = JDBCUtil.loadFullModelDataSet(conn, 22);
		PredictData data = DataLoader.loadFullModelDataSet(conn, 22);
		
		PredictRunner expectPr = new PredictRunner(expect);
		PredictRunner dataPr = new PredictRunner(data);
		
		Double2DImm expectResult = expectPr.doPredict();
		Double2DImm dataResult = expectPr.doPredict();

		//Compare result values
		comp = new Data2DCompare(expectResult, dataResult);
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

		comp = new Data2DCompare(expect.getCoef(), data.getCoef());
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
		
		comp = new Data2DCompare(expect.getDecay(), data.getDecay());
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
		
		comp = new Data2DCompare(expect.getSrc(), data.getSrc());
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
		
		comp = new Data2DCompare(expect.getSrcIds(), data.getSrcIds());
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
		
		comp = new Data2DCompare(expect.getSys(), data.getSys());
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
		
		comp = new Data2DCompare(expect.getTopo(), data.getTopo());
		//assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
                for (int i = 0; i < comp.getColCount(); i++)  {
                    System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
                    int row = comp.findMaxCompareRow(i);
                    System.out.println("id: " + expect.getTopo().getIdForRow(row));
                    System.out.println("expected: " + expect.getTopo().getValue(row, i));
                    System.out.println("found: " + data.getTopo().getValue(row, i));
                }
		
	}
	
	public void testReadModelMetadata() throws Exception {
		List<ModelBuilder> models = DataLoader.loadModelMetaData(conn);
		
		Model m = models.get(0);
		Source s1 = m.getSource(1);	//get by identifier
		Source s2 = m.getSource(11);	//get by identifier
		
		//test that we get the first and last sources
		this.assertEquals(s1, m.getSources().get(0));	//get via list index
		this.assertEquals(s2, m.getSources().get(10));	//get via list index
		
		
		//model
		this.assertEquals(1, m.getId().intValue());
		this.assertEquals("Chesapeake bay", m.getName());
		this.assertEquals(11, m.getSources().size());
		
		//1st source
		this.assertEquals(1, s1.getId().intValue());
		this.assertEquals("pttn", s1.getName());
		this.assertEquals(1, s1.getSortOrder());
		this.assertEquals(1, s1.getModelId().intValue());
		this.assertEquals(1, s1.getIdentifier());
		
		//last source
		this.assertEquals(11, s2.getId().intValue());
		this.assertEquals("frst", s2.getName());
		this.assertEquals(11, s2.getSortOrder());
		this.assertEquals(1, s2.getModelId().intValue());
		this.assertEquals(11, s2.getIdentifier());
		
	}
	

	
	/**
	 * @see JDBCUtil#loadTopo(Connection,int)
	 */
	public void testLoadTopo() throws Exception {
		Int2DImm jdbcData = DataLoader.loadTopo(conn, 1);
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(4, jdbcData.getColCount());
		
		Data2DCompare comp = buildTopoComparison(jdbcData);
		
		this.assertEquals(0, (int) comp.findMaxCompareValue());
	}
	
	/**
	 * @see JDBCUtil#loadSourceIds(java.sql.Connection,int)
	 */
	public void testLoadSource(Connection conn, int modelId) throws Exception {
		Int2DImm jdbcData = DataLoader.loadSourceIds(conn, 1);
		this.assertEquals(11, jdbcData.getRowCount());
		this.assertEquals(1, jdbcData.getColCount());
		
		//This basic set of sources should have id's 0 to 10.
		for (int i = 0; i < 11; i++)  {
			this.assertEquals(i, jdbcData.getInt(i, 0));
		}
		
	}
	
	/**
	 * @see JDBCUtil#loadSourceReachCoef(Connection, int, int, Int2D)
	 */
	public void testLoadSourceReachCoef() throws Exception {
		Int2DImm sources = DataLoader.loadSourceIds(conn, 1);
		Data2D jdbcData = DataLoader.loadSourceReachCoef(conn, 1, 0, sources);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(11, jdbcData.getColCount());
		
		Data2DCompare comp = buildSourceReachCoefComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	
	/**
	 * @see JDBCUtil#loadDecay(Connection, int, int)
	 */
	public void testLoadDecay() throws Exception {
		Double2DImm jdbcData = DataLoader.loadDecay(conn, 1, 0);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(2, jdbcData.getColCount());
		
		Data2DCompare comp = buildDecayComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	/**
	 * @see JDBCUtil#loadSourceValues(Connection, int, Int2D)
	 */
	public void testLoadSourceValues() throws Exception {
		Int2DImm sources = DataLoader.loadSourceIds(conn, 1);
		Data2D jdbcData = DataLoader.loadSourceValues(conn, 1, sources);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(11, jdbcData.getColCount());
		
		Data2DCompare comp = buildSourceValueComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	protected Data2DCompare buildTopoComparison(Data2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/topo.txt");
		Data2D data = TabDelimFileUtil.readAsInteger(fileStream, true, -1);
		
		Data2DCompare comp = new Data2DCompare(data, toBeCompared);
		
		return comp;
	}
	
	protected Data2DCompare buildSourceReachCoefComparison(Data2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		Data2D data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.orderedSearchFirst(1, 0);
		Data2D view = new Data2DView(data, 0, firstNonZeroRow, 4, 11);	//Crop to only iteration 0 and remove non-coef columns
		
		Data2DCompare comp = new Data2DCompare(view, toBeCompared);
		
		return comp;
	}
	
	protected Data2DCompare buildDecayComparison(Data2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		Data2D data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.orderedSearchFirst(1, 0);
		Data2D view = new Data2DView(data, 0, firstNonZeroRow, 1, 2);	//Crop to only iteration 0 and only the two decay columns
		
		Data2DCompare comp = new Data2DCompare(view, toBeCompared);
		
		return comp;
	}
	
	protected Data2DCompare buildSourceValueComparison(Data2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/src.txt");
		Data2D data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		
		Data2DCompare comp = new Data2DCompare(data, toBeCompared);
		
		return comp;
	}
	
	protected Data2DCompare buildPredictionComparison(Data2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		Data2D data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};
		
		Data2DCompare comp = new Data2DCompare(toBeCompared, data, DEFAULT_COMP_COLUMN_MAP);
		
		return comp;
	}
	
	public void dumpComparison(Data2D expect, Data2D data, int limitRows) {
		for (int r = 0; r < limitRows && r < expect.getRowCount(); r++)  {
		
			StringBuilder out = new StringBuilder(60);
			boolean isEqual = true;
			
			out.append(r + " : ");
			
			for (int c=0; c < expect.getColCount(); c++) {
				Number e = expect.getValue(r, c);
				Number d = data.getValue(r, c);
				
				out.append(e + "/" + d);
				
				if (! e.equals(d)) {
					isEqual = false;
				}
			}
			
			//if (!isEqual) {
				System.out.println(out);
			//}
			
		}
		
		
	}
	
}
