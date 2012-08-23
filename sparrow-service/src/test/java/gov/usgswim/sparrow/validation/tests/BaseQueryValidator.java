package gov.usgswim.sparrow.validation.tests;

import gov.usgswim.sparrow.validation.tests.TestResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.action.CalcAnalysis;
import gov.usgswim.sparrow.action.LoadModelMetadata;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.extras.DOMConfigurator;

/**
 * This test runs a bunch of validation queries against the db to check that
 * data meets constraints that cannot be expressed by SQL constraints.
 * 
 * The queries are in a properties file of the same name.  Based on a naming
 * convention, the queries must return 1 rows / 1 value / or no rows.
 * 
 * @author eeverman
 */
public class BaseQueryValidator extends SparrowModelValidationBase {
	
	@Override
	public boolean requiresDb() { return true; }
	
	@Override
	public boolean requiresTextFile() { return false; }
	
	protected int queryCount = 0;
	
	/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	@Override
	public TestResult testModel(Long modelId) throws Exception {
		Connection conn = SharedApplication.getInstance().getROConnection();
		
		//Get list of queries in properties file
		Properties props = new Properties();

		String path = this.getClass().getName().replace('.', '/') + ".properties";
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (ins == null) {
			ins = Action.class.getResourceAsStream(path);
		}
		props.load(ins);

		Enumeration<?> elements = props.propertyNames();
		boolean passed = true;
		
		try {
			while (elements.hasMoreElements()) {
				String queryName = elements.nextElement().toString();
				queryCount++;
				testSingleModelDataQuality(modelId, queryName, conn);
			}
			
		} catch (Exception e) {
			recordTestException(modelId, e, "Unknown exception during run");
		} finally {
			SharedApplication.closeConnection(conn, null);
		}
		
		return result;
	}
	
	
	/**
	 * Runs a single QA check.
	 * @param modelId
	 * @param queryName
	 * @param conn
	 * @param failedTestIsWarning If true, a failed test is only a warning, not a failure.
	 * @return
	 * @throws Exception
	 */
	public void testSingleModelDataQuality(Long modelId, String queryName, Connection conn) throws Exception {
		
		
		//There are two types of queries:
		//The 'normal' type returns zero rows to indicate that all is OK.
		//The Alt type can return a single column of a type convertable to int.
		//The 'alt' type is indicated by having the query name include the sufix
		// queryName_XXX where XXX is the integer value expected in the first column.
		boolean expectZeroRows = true;
		int expectedValue = 0;
		
		int splitPos = queryName.lastIndexOf('_');
		if (splitPos > 0 && splitPos < (queryName.length() - 1)) {
			String numString = queryName.substring(splitPos + 1);
			NumberUtils.isDigits(numString);

			recordTrace(modelId, "Query '" + queryName + "' is expecting '" + numString + "' in the first column.");
			expectedValue = Integer.parseInt(numString);
			expectZeroRows = false;
		} else {
			recordTrace(modelId, "Query '" + queryName + "' is expecting no return rows.");
		}
		
		
		CalcAnalysis action = new CalcAnalysis();
		//String[] params = new String[] {"MODEL_ID", modelId.toString()};
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("MODEL_ID", modelId);
		//String sql = Action.getTextWithParamSubstitution(queryName, this.getClass(), params);
		PreparedStatement st = action.getROPSFromPropertiesFile(queryName, this.getClass(), params);
		
		//Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = null;
		
		try {
			rs = st.executeQuery();
				
			if (expectZeroRows) {
				if (rs.next()) {
					
					String msg = "Failed test query '" + queryName + "' (zero return rows exptected). See the "
							+ this.getClass().getName() + ".properties file for query SQL.";
					
					recordError(modelId, msg);

				} else {
					recordTrace(modelId, "Query '" + queryName + "' passed.");
				}
			} else {
				if (! rs.next()) {
					
					String msg = "Failed test query '" + queryName + "' (one return rows exptected). See the "
							+ this.getClass().getName() + ".properties file for query SQL.";
					
					recordError(modelId, msg);

				} else {
					int value = rs.getInt(1);
					if (expectedValue != value) {
						
						String msg = "Failed test query '" + queryName + "' (one return value of " + expectedValue +" expected). See the "
							+ this.getClass().getName() + ".properties file for query SQL.";

						recordError(modelId, msg);
					
					} else {
						recordTrace(modelId, "Query '" + queryName + "' passed.");
					}
				}
			}
		} catch (Exception e) {
			recordTestException(modelId, e, "The query '" + queryName + "' failed with a SQL error. See the "
					+ BaseQueryValidator.class.getName() + ".properties file for query SQL.");
								
		} finally {
			rs.close();
			st.close();
		}
	}
	

	
}

