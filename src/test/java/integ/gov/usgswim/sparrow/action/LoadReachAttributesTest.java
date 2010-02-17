package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.TestHelper;
import gov.usgswim.sparrow.cachefactory.ReachID;
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
import gov.usgswim.sparrow.service.idbypoint.IDByPointService;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Tests the LoadReachAttributes Action.
 * 
 * @author eeverman
 */
public class LoadReachAttributesTest  extends SparrowDBTest {
	

	/**
	 * Tests the results of loading the attributes against a serialized table.
	 * @throws Exception
	 */
	//@Test
	public void compareToOriginal() throws Exception {
		LoadReachAttributes action = new LoadReachAttributes();
		action.setModelId(SparrowDBTest.TEST_MODEL_ID);
		action.setReachId(9190);
		
		DataTable original = (DataTable) getFileAsObject(this.getClass(), "tab", "ser");
		DataTable newVersion = action.doAction();
		assertTrue(compareTables(original, newVersion));
		
	}
	
	/**
	 * Writes the current version of the datatable to a file located somewhere
	 * local... possibly your home directory, possibly the root of this project.
	 * Use to create or update the datatable for the test.
	 * @throws Exception
	 */
	public void saveCurrentTableToFile() throws Exception {
		LoadReachAttributes action = new LoadReachAttributes();
		action.setModelId(SparrowDBTest.TEST_MODEL_ID);
		action.setReachId(9190);
		DataTable newVersion = action.doAction();
		
		writeObjectToFile(newVersion, "LoadReachAttributesTest_tab.ser");
	}
	
	/**
	 * This is a test of using the action (which uses a PreparedStatement)
	 * vs just using a non-PreparedStatement.  Verdict:  non-Prep is faster, 
	 * but only slightly.
	 * 
	 * Example values for loading 20 sets of data:
	 * Total Time for Prepared Statements: 49308ms
	 * Total Time for Non-Prepared Statements: 47581ms
	 * 
	 * In theory, however, we could turn the prepared statement query and
	 * see a performance boost.
	 * 
	 * @throws Exception
	 */
	//@Test
	public void comparePreparedStatementPerf() throws Exception {
		
		long totalPrepStatementTime = 0;
		long totalNonPrepStatementTime = 0;
		String baseQuery = queryTemplate;
		
		for (int i = 9190; i < 9210; i++) {
			
			
			String query = baseQuery + "WHERE SPARROW_MODEL_ID = 50 AND IDENTIFIER = " + i;
			
			long start = System.currentTimeMillis();
			Connection conn = SharedApplication.getInstance().getConnection();
			ResultSet rs = null;
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(200);
			rs = st.executeQuery(query);
			
			while (rs.next()) { /* throw away */ }
			
			long end = System.currentTimeMillis();
			
			totalNonPrepStatementTime+= (end - start);
			
			
			//Using the Action
			start = System.currentTimeMillis();
			DataTable tab = SharedApplication.getInstance().getReachAttributes(new ReachID(50L, i));
			end = System.currentTimeMillis();
			totalPrepStatementTime+= (end - start);
			
			conn.close();
		}
		
		log.error("Total Time for Prepared Statements: " + totalPrepStatementTime);
		log.error("Total Time for Non-Prepared Statements: " + totalNonPrepStatementTime);
	}
	
	String queryTemplate = "SELECT \n" + 
			"	HYDSEQ as \"Hydrological Seq\", \n" + 
			"	FRAC as \"Fraction\", \n" + 
			"	FNODE as \"From Node\", \n" + 
			"	TNODE as \"To Node\", \n" + 
			"	SPARROW_MODEL_ID as \"Sparrow Model ID\", \n" + 
			"	FULL_IDENTIFIER as \"Reach ID\", REACH_NAME as \"Reach Name\", OPEN_WATER_NAME as \"Open Water Name\", \n" + 
			"	MEANQ as \"Mean Q\", MEANV as \"Mean V\", CATCH_AREA as \"Catch Area\", CUM_CATCH_AREA as \"Cumulative Catch Area\", \n" + 
			"	REACH_LENGTH as \"Reach Length\", HUC2, HUC4, HUC6, HUC8, HEAD_REACH as \"Head Reach\", \n" + 
			"	SHORE_REACH as \"Shore Reach\", TERM_TRANS as \"Terminal Trans\", TERM_ESTUARY as \"Terminal Estuary\", \n" + 
			"	TERM_NONCONNECT as \"Terminal Nonconnect\" \n" + 
			"FROM MODEL_ATTRIB_VW ";
	
	
	
}

