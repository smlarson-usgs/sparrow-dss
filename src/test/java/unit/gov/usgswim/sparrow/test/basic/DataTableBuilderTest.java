package gov.usgswim.sparrow.test.basic;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;

public class DataTableBuilderTest extends TestCase {

	public static final String TEST_FILE = "/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt";
	private DataTableWritable table;

	public DataTableBuilderTest(String testName) {
		super(testName);
	}

	@Override public void setUp() throws Exception {
		InputStream fileStream = getClass().getResourceAsStream(TEST_FILE);
		table = TabDelimFileUtil.read(fileStream, true);
	}

	public void testFindByRowID() throws Exception {
		// Check rowIDs
		assertEquals(0, table.getRowForId(1L));
		assertEquals(1, table.getRowForId(11L));
		assertEquals(2, table.getRowForId(21L));
		assertEquals(3, table.getRowForId(31L));
		assertEquals(9, table.getRowForId(91L));

		// Should not be found (-1)
		assertEquals(-1, table.getRowForId(99L));
	}

	public void testFindByIndex() throws Exception {
		//
		// Check the index on the first column.
		//
		table.buildIndex(0);
		assertEquals(0, table.findFirst(0, 2d));
		assertEquals(1, table.findFirst(0, 12d));
		assertEquals(2, table.findFirst(0, 22d));
		assertEquals(3, table.findFirst(0, 32d));
		assertEquals(9, table.findFirst(0, 92d));

		//
		// Change some values and make sure we find them.
		//
		table.setValue(Double.valueOf(99d), 9, 0);
		table.setValue(Double.valueOf(-1d), 0, 0);

		// Altered values can't be found yet because index was not rebuilt. If
		// we hadn't build an index, this would be ok
		assertEquals(-1, table.findFirst(0, 99d));
		assertEquals(-1, table.findFirst(0,-1d));

		// Now we can find values
		table.buildIndex(0);
		assertEquals(9, table.findFirst(0, 99d));
		assertEquals(0, table.findFirst(0,-1d));

	}

	public void testFindWithoutIndex() throws Exception {
		//
		// Check the index on the first column.
		//
		assertEquals(0, table.findFirst(0, 2d));
		assertEquals(1, table.findFirst(0, 12d));
		assertEquals(2, table.findFirst(0, 22d));
		assertEquals(3, table.findFirst(0, 32d));
		assertEquals(9, table.findFirst(0, 92d));

		//
		// Change some values and make sure we find them.
		//
		table.setValue(Double.valueOf(99d), 9, 0);
		table.setValue(Double.valueOf(-1d), 0, 0);

		// Altered values can be found immediately when no index is built on column
		assertEquals(9, table.findFirst(0, 99d));
		assertEquals(0, table.findFirst(0,-1d));

	}



}


