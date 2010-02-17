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
	@Test
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
	
	
	
}

