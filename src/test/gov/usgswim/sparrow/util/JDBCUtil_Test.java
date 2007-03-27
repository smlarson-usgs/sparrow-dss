package test.gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.Int2D;
import gov.usgswim.sparrow.util.JDBCUtil;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import java.sql.Connection;

import junit.framework.TestCase;
import java.sql.*;
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
	 * @see JDBCUtil#loadTopo(Connection,int)
	 */
	public void testLoadTopo() throws Exception {
		Int2D jdbcData = JDBCUtil.loadTopo(conn, 1);
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(4, jdbcData.getColCount());
		
		Data2DCompare comp = buildTopoComparison(jdbcData);
		
		this.assertEquals(0, (int) comp.findMaxCompareValue());
	}
	
	/**
	 * @see JDBCUtil#loadSource(Connection, int)
	 */
	public void testLoadSource(Connection conn, int modelId) throws Exception {
		Int2D jdbcData = JDBCUtil.loadSource(conn, 1);
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
		Int2D sources = JDBCUtil.loadSource(conn, 1);
		Double2D jdbcData = JDBCUtil.loadSourceReachCoef(conn, 1, 0, sources);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(11, jdbcData.getColCount());
		
		Data2DCompare comp = buildSourceReachCoefComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	
	/**
	 * @see JDBCUtil#loadDecay(Connection, int, int)
	 */
	public void testLoadDecay() throws Exception {
		Double2D jdbcData = JDBCUtil.loadDecay(conn, 1, 0);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(2, jdbcData.getColCount());
		
		Data2DCompare comp = buildDecayComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	
	/**
	 * @see JDBCUtil#loadSourceValues(Connection, int, Int2D)
	 */
	public void testLoadSourceValues() throws Exception {
		Int2D sources = JDBCUtil.loadSource(conn, 1);
		Double2D jdbcData = JDBCUtil.loadSourceValues(conn, 1, sources);
		
		this.assertEquals(2339, jdbcData.getRowCount());
		this.assertEquals(11, jdbcData.getColCount());
		
		Data2DCompare comp = buildSourceValueComparison(jdbcData);
		
		assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
	}
	
	protected Data2DCompare buildTopoComparison(Int2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/topo.txt");
		Int2D data = TabDelimFileUtil.readAsInteger(fileStream, true);
		
		Data2DCompare comp = new Data2DCompare(data, toBeCompared);
		
		return comp;
	}
	
	protected Data2DCompare buildSourceReachCoefComparison(Double2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/coef.txt");
		Double2D data = TabDelimFileUtil.readAsDouble(fileStream, true);
		int firstNonZeroRow = data.orderedSearchFirst(1, 0);
		Data2D view = new Data2DView(data, 0, firstNonZeroRow, 4, 11);	//Crop to only iteration 0 and remove non-coef columns
		
		Data2DCompare comp = new Data2DCompare(view, toBeCompared);
		
		return comp;
	}
	
	protected Data2DCompare buildDecayComparison(Double2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/coef.txt");
		Double2D data = TabDelimFileUtil.readAsDouble(fileStream, true);
		int firstNonZeroRow = data.orderedSearchFirst(1, 0);
		Data2D view = new Data2DView(data, 0, firstNonZeroRow, 1, 2);	//Crop to only iteration 0 and only the two decay columns
		
		Data2DCompare comp = new Data2DCompare(view, toBeCompared);
		
		return comp;
	}
	
	protected Data2DCompare buildSourceValueComparison(Double2D toBeCompared) throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/src.txt");
		Double2D data = TabDelimFileUtil.readAsDouble(fileStream, true);
		
		Data2DCompare comp = new Data2DCompare(data, toBeCompared);
		
		return comp;
	}
}
