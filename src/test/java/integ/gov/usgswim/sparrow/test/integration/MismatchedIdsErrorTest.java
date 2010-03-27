package gov.usgswim.sparrow.test.integration;

import static org.junit.Assert.assertEquals;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.LoadModelPredictData;

import org.custommonkey.xmlunit.XMLUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class MismatchedIdsErrorTest {
	
	static LifecycleListener lifecycle = new LifecycleListener();
	
	@BeforeClass
	public static void setUp() throws Exception {
		lifecycle.contextInitialized(null, true);
		XMLUnit.setIgnoreWhitespace(true);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		lifecycle.contextDestroyed(null, true);
	}
	

	@Test
	public void testLoadSources() throws Exception {
		PredictData pd = new LoadModelPredictData(42L, true).run();
		
		DataTable topo = pd.getTopo();
		DataTable source = pd.getSrc();
		DataTable coef = pd.getCoef();
		
		compareTableIndexes(topo, source);
		compareTableIndexes(topo, coef);

	}
	
	public void compareTableIndexes(DataTable base, DataTable comp) {
		for (int r=0; r<base.getRowCount(); r++) {
			long baseId = base.getIdForRow(r);
			long compId = comp.getIdForRow(r);
			if (baseId != compId) {
				printRow(base, r, "Base Table");
				printRow(base, r, "Comp Table");
				
				//force test failure
				assertEquals(baseId, compId);
			}
			
		}
	}
	
	public void printRow(DataTable table, int row, String tableName) {
		
		System.out.println(tableName + " : " + table.getRowCount() + " rows");
		
		
		final String TAB = "\t";
		StringBuffer line = new StringBuffer();
		
		line.append("Index");
		for (int c=0; c< table.getColumnCount(); c++) {
			line.append(TAB);
			line.append(table.getName(c));
		}
		System.out.println(line.toString());
		
		line = new StringBuffer();
		
		if (table.hasRowIds()) {
			line.append(table.getIdForRow(row));
		} else {
			line.append("No Ids");
		}
		for (int c=0; c< table.getColumnCount(); c++) {
			line.append(TAB);
			line.append(table.getValue(row, c));
		}
		System.out.println(line.toString());
		
		
	}
	
}

