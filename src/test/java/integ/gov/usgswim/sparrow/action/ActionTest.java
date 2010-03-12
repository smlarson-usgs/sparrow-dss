package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.TestHelper;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.BasicAnalysis;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.NominalComparison;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Tests that the basic Action class closes connections and statements as
 * advertized.
 * 
 * @author eeverman
 */
public class ActionTest  extends SparrowDBTest {
	
	@Test
	public void closeASingleConnectionAndStatement() throws Exception {
		SimpleAction1 action = new SimpleAction1();
		action.run();
		assertTrue(action.passed);
	}
	
	@Test
	public void closeMultipleStatementsAndConnections() throws Exception {
		SimpleAction2 action = new SimpleAction2();
		action.run();
		assertTrue(action.passed);
	}
	
	@Test
	public void closeConnectionsEvenIfError() throws Exception {
		SimpleAction3 action = new SimpleAction3();
		action.run();
		assertTrue(action.passed);
	}
	
	//Only creates a single prepared statement, doesn't attempt any local closing
	public static class SimpleAction1 extends LoadReachAttributes {

		public boolean passed = true;
		
		Connection conn1;
		Connection conn2;
		
		PreparedStatement state1;
		
		@Override
		protected DataTable doAction() throws Exception {
			String sql = this.getText(QUERY_NAME, LoadReachAttributes.class);
			
			conn1 = getConnection();
			state1 = getNewROPreparedStatement(sql);
			conn2 = getConnection();
			
			if (conn1 != conn2) {
				passed = false;
			}
			
			state1.setLong(1, 9190);
			state1.setLong(2, SparrowDBTest.TEST_MODEL_ID);
			ResultSet rset = state1.executeQuery();	//auto-closed
			DataTableWritable attributes = null;
			attributes = DataTableConverter.toDataTable(rset);
			return attributes;
		}
		
		@Override
		protected void postAction(boolean success, Exception error) {
			super.postAction(success, error);
			
			try {
				if (! conn1.isClosed()) passed = false;
				if (! conn2.isClosed()) passed = false;
				if (! state1.isClosed()) passed = false;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				passed = false;
			}
		}
	}
	
	//Creates two prepared statements, closing connections between
	public static class SimpleAction2 extends LoadReachAttributes {

		public boolean passed = true;
		
		Connection conn1;
		Connection conn2;
		Connection conn3;
		
		PreparedStatement state1;
		PreparedStatement state2;
		
		@Override
		protected DataTable doAction() throws Exception {
			String sql = this.getText(QUERY_NAME, LoadReachAttributes.class);
			
			conn1 = getConnection();
			conn1.close();
			state1 = getNewROPreparedStatement(sql);
			conn2 = getConnection();
			
			if (conn1 == conn2) {
				passed = false;
			}
			
			//Build data from 1st statement
			state1.setLong(1, 9190);
			state1.setLong(2, SparrowDBTest.TEST_MODEL_ID);
			ResultSet rset = state1.executeQuery();	//auto-closed
			DataTableWritable attributes = null;
			attributes = DataTableConverter.toDataTable(rset);
			
			//Get new statement
			state2 = getNewROPreparedStatement(sql);
			conn3 = getConnection();
			//These should be the same conneciton
			if (conn2 != conn3) {
				passed = false;
			}
			
			//Build data from 2nd statement
			state2.setLong(1, 9190);
			state2.setLong(2, SparrowDBTest.TEST_MODEL_ID);
			rset = state2.executeQuery();	//auto-closed
			attributes = DataTableConverter.toDataTable(rset);
			
			return attributes;
		}
		
		@Override
		protected void postAction(boolean success, Exception error) {
			super.postAction(success, error);
			
			try {
				if (! conn1.isClosed()) passed = false;
				if (! conn2.isClosed()) passed = false;
				if (! conn3.isClosed()) passed = false;
				if (! state1.isClosed()) passed = false;
				if (! state2.isClosed()) passed = false;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				passed = false;
			}
		}
	}
	
	//Only creates a single prepared statement, but throws an error during execution
	public static class SimpleAction3 extends LoadReachAttributes {

		public boolean passed = true;
		
		Connection conn1;		
		PreparedStatement state1;
		
		@Override
		protected DataTable doAction() throws Exception {
			String sql = this.getText(QUERY_NAME, LoadReachAttributes.class);
			
			conn1 = getConnection();
			state1 = getNewROPreparedStatement(sql);
			
			state1.setLong(1, 9190);
			state1.setLong(2, SparrowDBTest.TEST_MODEL_ID);
			ResultSet rset = state1.executeQuery();	//auto-closed
			
			//Here we throw an exception...
			Integer zero = 0;
			int divideError = 10 / zero;
			assertTrue(false);	//should never reach this point
			
			DataTableWritable attributes = null;
			attributes = DataTableConverter.toDataTable(rset);
			return attributes;
		}
		
		@Override
		protected void postAction(boolean success, Exception error) {
			super.postAction(success, error);
			
			try {
				if (! conn1.isClosed()) passed = false;
				if (! state1.isClosed()) passed = false;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				passed = false;
			}
		}
	}
	

	
}

