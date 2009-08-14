package gov.usgswim.sparrow.util;

import static org.junit.Assert.*;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

public class DataResourceLoaderTest {

	@Test
	public void testInitModelIndex() {
		DataTable modelIndex = DataResourceUtils.initModelIndex();

		assertNotNull(modelIndex);
		assertEquals(2, modelIndex.getColumnCount());
		assertTrue(modelIndex.getRowCount() >= 3);
	}

	@Test
	public void testLoadTopoFromResources() throws SQLException, IOException {
		DataTableWritable topo = DataResourceLoader.loadTopo(-1);

		assertTrue(topo != null);
		assertEquals(5, topo.getColumnCount());
		assertEquals("fnode", topo.getName(1));
		assertTrue(topo.getRowCount() > 10);
	}

}
