package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.util.DataLoader;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Ignore;

@Ignore
public class DataLoaderOfflineTest extends TestCase {
	public DataLoaderOfflineTest(String sTestName) {
		super(sTestName);
	}

	public void testInitModelIndex() {
		DataTable modelIndex = DataLoader.initModelIndex();

		assertNotNull(modelIndex);
		assertEquals(2, modelIndex.getColumnCount());
		assertTrue(modelIndex.getRowCount() >= 3);
	}

	//============================
	// TODO determine whether these xtest methods are still relevant
	//==============================
	/**
	 * @see DataLoader#getQuery(String,Object[])
	 */
	public void xtestGetQuery() throws IOException {
		String query = DataLoader.getQuery("SelectSystemData");

		String expected =
		"SELECT MODEL_REACH_ID as MODEL_REACH, HYDSEQ FROM MODEL_REACH WHERE SPARROW_MODEL_ID = $ModelId$ ORDER BY HYDSEQ";

		assertEquals(expected, query);
	}

	/**
	 * @see DataLoader#getQuery(String,long)
	 */
	public void xtestGetQueryWModelID() throws IOException {
		String query = DataLoader.getQuery("SelectSystemData", 999);

		String expected =
		"SELECT MODEL_REACH_ID as MODEL_REACH, HYDSEQ FROM MODEL_REACH WHERE SPARROW_MODEL_ID = 999 ORDER BY HYDSEQ";

		assertEquals(expected, query);
	}

	/**
	 * @see DataLoader#getQuery(String)
	 */
	public void xtestGetQueryWOtherParams() throws IOException {
		String query = DataLoader.getQuery(
			"SelectReachCoef", new Object[] {"ModelId", 999, "Iteration", 888, "SourceId", 777});

		String expected =
		"SELECT coef.VALUE AS Value FROM SOURCE_REACH_COEF coef INNER JOIN MODEL_REACH rch ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID WHERE rch.SPARROW_MODEL_ID = 999 AND coef.Iteration = 888 AND coef.SOURCE_ID = 777 ORDER BY rch.HYDSEQ";

		assertEquals(expected, query);
	}

	@Ignore
	public void testDummy() {
		// do nothing. This is here only because Infinitest complains if a test class has no test methods.
	}
}
