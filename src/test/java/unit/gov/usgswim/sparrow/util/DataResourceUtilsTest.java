package gov.usgswim.sparrow.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTable;

import org.junit.Test;


public class DataResourceUtilsTest {
	@Test
	public void testInitModelIndex() {
		DataTable modelIndex = DataResourceUtils.initModelIndex();

		assertNotNull(modelIndex);
		assertEquals(2, modelIndex.getColumnCount());
		assertTrue(modelIndex.getRowCount() >= 3);
	}

	@Test
	public void testLoadHelp() {

	}
}
