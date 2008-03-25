package gov.usgswim.sparrow.test;


import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.util.SparrowUtil;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.TestCase;

public class TabDelimFileUtil_Test extends TestCase{

	public TabDelimFileUtil_Test(String testName) {
		super(testName);
	}

	public void testDouble1() throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, false, -1);

		assertEquals(1d, data.getDouble(0, 0), 0d);
		assertEquals(5.14159d, data.getDouble(0,4), 0d);
		assertEquals(91d, data.getDouble(9,0), 0d);
		assertEquals(95d, data.getDouble(9,4), 0d);

		SparrowUtil.printDataTable(data, "Double: Loaded from /gov/usgswim/sparrow/tab_delimit_sample.txt");
	}

	/**
	 * Tests that empty lines are skipped.
	 * @throws Exception
	 */
	public void testDouble2() throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_gap.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, false, -1);

		assertEquals(1d, data.getDouble(0,0), 0d);
		assertEquals(5.14159d, data.getDouble(0,4), 0d);
		assertEquals(91d, data.getDouble(9,0), 0d);
		assertEquals(95d, data.getDouble(9,4), 0d);

		SparrowUtil.printDataTable(data, "Double: Loaded from /gov/usgswim/sparrow/tab_delimit_sample_gap.txt");
	}

	/**
	 * Tests that headings work.
	 * @throws Exception
	 */
	public void testDouble3() throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);

//		old reading, first column is not index, 5 total columns
//		assertEquals(1d, data.getDouble(0,0), 0d);
//		assertEquals(5d, data.getDouble(0,4), 0d);
//		assertEquals(91d, data.getDouble(9,0), 0d);
//		assertEquals(95d, data.getDouble(9,4), 0d);

		// new reading, first column is index, only 4 total columns
		assertEquals(Long.valueOf(1), data.getIdForRow(0));
		assertEquals(5d, data.getDouble(0,3), 0d);
		assertEquals(Long.valueOf(91), data.getIdForRow(9));
		assertEquals(95d, data.getDouble(9,3), 0d);

		assertEquals("One", data.getName(0));
		assertEquals("Two", data.getName(1));
		assertEquals("Three", data.getName(2));
		assertEquals("Four", data.getName(3));
//		assertEquals("", data.getName(4));

	}

	/**
	 * Tests that an error is thrown if the number of columns varies b/t the rows.
	 * @throws Exception
	 */
	public void testDoubleColumnCountError() throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_err1.txt");


		try {
			TabDelimFileUtil.readAsDouble(fileStream, false, -1);
		} catch (IllegalStateException e) {
			return;	//terminate normally - this error is expected
		}

		fail("A number format exception was expected b/c the file has mis-matched column counts.");
	}



	/**
	 * Tests that empty lines are skipped.
	 * @throws Exception
	 */
	public void testInteger1() throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_gap_int.txt");
		DataTable data = TabDelimFileUtil.readAsInteger(fileStream, false, -1);

		assertEquals(Integer.valueOf(1), data.getInt(0,0));
		assertEquals(Integer.valueOf(5), data.getInt(0,4));
		assertEquals(Integer.valueOf(91), data.getInt(9,0));
		assertEquals(Integer.valueOf(95), data.getInt(9,4));

		SparrowUtil.printDataTable(data, "Double: Loaded from /gov/usgswim/sparrow/tab_delimit_sample_gap_int.txt");
	}

	/**
	 * Tests that headings work.
	 * @throws Exception
	 */
	public void testInteger2() throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		DataTable data = TabDelimFileUtil.readAsInteger(fileStream, true, -1);
		// old reading, first column is not index data, 5 columns
//		assertEquals(Integer.valueOf(1), data.getInt(0,0));
//		assertEquals(Integer.valueOf(5), data.getInt(0,4));
//		assertEquals(Integer.valueOf(91), data.getInt(9,0));
//		assertEquals(Integer.valueOf(95), data.getInt(9,4));

		// new reading, first column is id data, only 4 total columns
		assertEquals(Long.valueOf(1), data.getIdForRow(0));
		assertEquals(Integer.valueOf(5), data.getInt(0,3));
		assertEquals(Long.valueOf(91), data.getIdForRow(9));
		assertEquals(Integer.valueOf(95), data.getInt(9,3));

		assertEquals("One", data.getName(0));
		assertEquals("Two", data.getName(1));
		assertEquals("Three", data.getName(2));
		assertEquals("Four", data.getName(3));
//		assertEquals("", data.getName(4));

	}

	public void testColumnMapping() throws Exception {

		String[] fileHeadings = new String[] {
				"c1", "c2", "c3", "C4", "C5", "C6"
		};

		String[] mappedHeadings = new String[] {
				"c6", "C3", "c4", "c1"
		};

		int[] expectedMap = new int[] {3, -1, 1, 2, -1, 0};

		int[] mapped = TabDelimFileUtil.mapByColumnHeadings(fileHeadings, mappedHeadings);

		assertTrue(Arrays.equals(expectedMap, mapped));
	}

	public void testIntColumnMapping() throws Exception {

		String[] mappedHeadings = new String[] {
				"LOCAL_ID", "STD_ID", "LOCAL_SAME"
		};

		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
		DataTable data = TabDelimFileUtil.readAsInteger(fileStream, true, mappedHeadings, -1);

		assertEquals(3, data.getColumnCount());

		assertEquals(Integer.valueOf(3074), data.getInt(0,0));
		assertEquals(Integer.valueOf(3074), data.getInt(0,1));
		assertEquals(Integer.valueOf(1), data.getInt(0,2));
		assertEquals(Integer.valueOf(0), data.getInt(1,2));

//		assertTrue(Arrays.equals(mappedHeadings, data.getHeadings())); not applicable


		//now try mapping a bad column
		try {
			mappedHeadings = new String[] {"LOCAL_ID", "STD_ID", "LOCAL_SAME", "lkjhdlkfhlkhlskdfh"};
			fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
			data = TabDelimFileUtil.readAsInteger(fileStream, true, mappedHeadings, -1);
			fail("Should have thrown an exception b/c the column does not exist.");
		} catch (IllegalArgumentException e) {
			//exception is expected.
		}
	}


	public void testDoubleColumnMapping() throws Exception {

		String[] mappedHeadings = new String[] {
				"LOCAL_ID", "STD_ID", "LOCAL_SAME"
		};

		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, mappedHeadings, -1);

		assertEquals(3, data.getColumnCount());

		assertEquals(3074d, data.getDouble(0,0));
		assertEquals(3074d, data.getDouble(0,1));
		assertEquals(1d, data.getDouble(0,2));
		assertEquals(0d, data.getDouble(1,2));

//		assertTrue(Arrays.equals(mappedHeadings, data.getHeadings())); not applicable

		//now try mapping a bad column
		try {
			mappedHeadings = new String[] {"LOCAL_ID", "STD_ID", "LOCAL_SAME", "lkjhdlkfhlkhlskdfh"};
			fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
			data = TabDelimFileUtil.readAsDouble(fileStream, true, mappedHeadings, -1);
			fail("Should have thrown an exception b/c the column does not exist.");
		} catch (IllegalArgumentException e) {
			//exception is expected.
		}
	}

}

