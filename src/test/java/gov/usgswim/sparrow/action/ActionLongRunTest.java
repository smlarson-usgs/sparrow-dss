package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.SparrowDBTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;



/**
 * Tests that the basic Action class closes connections and statements as
 * advertized.
 * 
 * @author eeverman
 */
public class ActionLongRunTest  extends SparrowDBTest {
	
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
		
		//Turn off logging for Actions - Its intended to throw an exception
		Level orgLevel = Logger.getLogger(Action.class).getLevel();
		Logger.getLogger(Action.class).setLevel(Level.FATAL);
		
		action.run();
		
		//restore logging
		Logger.getLogger(Action.class).setLevel(orgLevel);
		
		
		assertTrue(action.passed);
	}
	
	//Only creates a single prepared statement, doesn't attempt any local closing
	public static class SimpleAction1 extends LoadReachAttributes {

		private String sql = "SELECT IDENTIFIER FROM MODEL_ATTRIB_VW WHERE IDENTIFIER=? AND SPARROW_MODEL_ID=?";
		public boolean passed = true;
		
		Connection conn1;
		Connection conn2;
		
		PreparedStatement state1;
		
		@Override
		public DataTable doAction() throws Exception {
			conn1 = getROConnection();
			state1 = getNewROPreparedStatement(sql);
			conn2 = getROConnection();
			
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

		private String sql = "SELECT IDENTIFIER FROM MODEL_ATTRIB_VW WHERE IDENTIFIER=? AND SPARROW_MODEL_ID=?";
		public boolean passed = true;
		
		Connection conn1;
		Connection conn2;
		Connection conn3;
		
		PreparedStatement state1;
		PreparedStatement state2;
		
		@Override
		public DataTable doAction() throws Exception {

			conn1 = getROConnection();
			conn1.close();
			state1 = getNewROPreparedStatement(sql);
			conn2 = getROConnection();
			
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
			conn3 = getROConnection();
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

		private String sql = "SELECT IDENTIFIER FROM MODEL_ATTRIB_VW WHERE IDENTIFIER=? AND SPARROW_MODEL_ID=?";
		public boolean passed = true;
		
		Connection conn1;		
		PreparedStatement state1;
		
		@Override
		public DataTable doAction() throws Exception {

			conn1 = getROConnection();
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

