package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.utils.DataTableSerializerUtils;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import org.apache.log4j.Level;
import org.junit.Test;



/**
 * Tests the LoadReachAttributes Action.
 *
 * @author eeverman
 */
public class LoadReachAttributesLongRunTest  extends SparrowTestBaseWithDB {

	/**
	 * Tests the results of loading the attributes against a serialized table.
	 * @throws Exception
	 */
	@Test
	public void compareToCannedResult() throws Exception {
		//log.setLevel(Level.DEBUG);
		LoadReachAttributes action = new LoadReachAttributes();
		action.setModelId(SparrowTestBaseWithDB.TEST_MODEL_ID);
		action.setReachId(9190);

		InputStream ins = getResource(this.getClass(), null, "tab");
		DataTable original = DataTableSerializerUtils.deserializeFromText(ins);
		DataTable newVersion = action.run();
		assertTrue(compareTables(original, newVersion, null, true, 0D, true));

	}


	/**
	 * This is a one-time util method to write the current record to a text
	 * file so it can be used for the test.  Please verify the results manually
	 * before assuming the result of this export is the gold standard.
	 * @throws Exception
	 */
//	@Test
	public void writeToText() throws Exception {
		LoadReachAttributes action = new LoadReachAttributes();
		action.setModelId(SparrowTestBaseWithDB.TEST_MODEL_ID);
		action.setReachId(9190);
		DataTable newVersion = action.run();

		File f = new File("/tmp/LoadReachAttributesTest.tab");
		//f.createNewFile();
		FileWriter fw = new FileWriter(f);

		DataTableSerializerUtils.serializeToText(newVersion, fw);

		fw.flush();
		fw.close();
	}

}

