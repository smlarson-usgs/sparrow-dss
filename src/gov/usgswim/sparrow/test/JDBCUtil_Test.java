package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.PredictSimple;
import gov.usgswim.sparrow.PredictionDataSet;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.util.JDBCUtil;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import java.sql.Connection;

import junit.framework.TestCase;
import java.sql.*;

import java.util.List;

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
	public void xtestLoadMinimalPredictDataSet() throws Exception {
		PredictionDataSet ds = JDBCUtil.loadMinimalPredictDataSet(conn, 1);
		PredictSimple ps = new PredictSimple(ds);
		
		Double2DImm result = ps.doPredict();

		Data2DCompare comp = buildPredictionComparison(result);
		
		for (int i = 0; i < comp.getColCount(); i++)  {
			System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
		}
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);

	}
	
	public void testReadModelMetadata() throws Exception {
		List<ModelBuilder> models = JDBCUtil.loadModelMetaData(conn);
		
		Model m = models.get(0);
		Source s1 = m.getSource(1);	//get by identifier
		Source s2 = m.getSource(11);	//get by identifier
		
		//test that we get the first and last sources
		this.assertEquals(s1, m.getSources().get(0));	//get via list index
		this.assertEquals(s2, m.getSources().get(10));	//get via list index
		
		
		//model
		this.assertEquals(1, m.getId());
		this.assertEquals("Chesapeake bay", m.getName());
		this.assertEquals(11, m.getSources().size());
		
		//1st source
		this.assertEquals(1, s1.getId());
		this.assertEquals("pttn", s1.getName());
		this.assertEquals(1, s1.getSortOrder());
		this.assertEquals(1, s1.getModelId());
		this.assertEquals(1, s1.getIdentifier());
		
		//last source
		this.assertEquals(11, s2.getId());
		this.assertEquals("frst", s2.getName());
		this.assertEquals(11, s2.getSortOrder());
		this.assertEquals(1, s2.getModelId());
		this.assertEquals(11, s2.getIdentifier());
		
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
		PredictionDataSet textDs = new PredictionDataSet();
		PredictionDataSet dbDs = null;
		
		
		//Load the text files
		textDs.setAncil( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "ancil.txt"), true, 0) );
		
		//1st 4 columns are iteration, inc delv, tot_del, and boot error (skip)
		Data2D baseTextCoef = TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "coef.txt"), true, -1);
		int firstRowBeyondZeroIteration = baseTextCoef.orderedSearchFirst(1d, 0);
		
		//For speed, you can choose which section to run below:
		/*
		//Uncomment this section to test only iteration 0.
		textDs.setCoef(
			new Data2DView(baseTextCoef, 0, firstRowBeyondZeroIteration, 4, 11)
		);
		*/
		
		//Uncomment this section to test all iterations.
		textDs.setCoef(
			new Data2DView(baseTextCoef, 4, 11)
		);
		
		
		textDs.setSrc( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "src.txt"), true, -1) );
		textDs.setTopo( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "topo.txt"), true, -1) );
		
		
		//Load the db version of the same model
		dbDs = JDBCUtil.loadFullModelDataSet(conn, 21);

		Data2DCompare comp = new Data2DCompare(textDs.getSrc(), dbDs.getSrc());
		Data2DCompare topo = new Data2DCompare(textDs.getTopo(), dbDs.getTopo());
		Data2DCompare coef = new Data2DCompare(textDs.getCoef(), dbDs.getCoef());
		
		
		for (int i = 0; i < comp.getColCount(); i++)  {
			System.out.println("comp col " + i + " error: " + comp.findMaxCompareValue(i));
		}
		
		for (int i = 0; i < topo.getColCount(); i++)  {
			System.out.println("topo col " + i + " error: " + topo.findMaxCompareValue(i));
		}
		
		for (int i = 0; i < coef.getColCount(); i++)  {
			System.out.println("coef col " + i + " error: " + coef.findMaxCompareValue(i));
			int maxRow = coef.findMaxCompareRow(i);
			System.out.println("--Row Data at max (row #" +  maxRow + ")");
			for (int j=0; j<coef.getColCount(); j++) {
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
	public void xtestLoadTopo() throws Exception {
		Int2DImm jdbcData = JDBCUtil.loadTopo(conn, 1);
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(4, jdbcData.getColCount());
		
		Data2DCompare comp = buildTopoComparison(jdbcData);
		
		this.assertEquals(0, (int) comp.findMaxCompareValue());
	}
	
	/**
	 * @see JDBCUtil#loadSource(Connection, int)
	 */
	public void xtestLoadSource(Connection conn, int modelId) throws Exception {
		Int2DImm jdbcData = JDBCUtil.loadSource(conn, 1);
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
	public void xtestLoadSourceReachCoef() throws Exception {
		Int2DImm sources = JDBCUtil.loadSource(conn, 1);
		Data2D jdbcData = JDBCUtil.loadSourceReachCoef(conn, 1, 0, sources);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(11, jdbcData.getColCount());
		
		Data2DCompare comp = buildSourceReachCoefComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	
	/**
	 * @see JDBCUtil#loadDecay(Connection, int, int)
	 */
	public void xtestLoadDecay() throws Exception {
		Double2DImm jdbcData = JDBCUtil.loadDecay(conn, 1, 0);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(2, jdbcData.getColCount());
		
		Data2DCompare comp = buildDecayComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	
	/**
	 * @see JDBCUtil#loadSourceValues(Connection, int, Int2D)
	 */
	public void xtestLoadSourceValues() throws Exception {
		Int2DImm sources = JDBCUtil.loadSource(conn, 1);
		Data2D jdbcData = JDBCUtil.loadSourceValues(conn, 1, sources);
		
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
}
