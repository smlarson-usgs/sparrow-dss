package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.sparrow.util.JDBCUtil;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.IOException;

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
		//this.assertEquals(0d, comp.findMaxCompareValue());
		
		for (int c=0; c<comp.getColCount(); c++) {
			int r = comp.findMaxCompareRow(c);
			
			System.out.println("*** max compare in column " + c + " is row " + r);
			System.out.println("Expected: " + jdbcData.getValue(r, c) + " Actual: " + dldata.getValue(r, c));
		}
		
		
		//dumpComparison(jdbcData, dldata, 999999);
		
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
