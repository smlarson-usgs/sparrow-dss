package gov.usgswim.sparrow.test.basic;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;

public class ReadStreamAsDoubleTest extends TestCase {


	public void testBasic() throws Exception {
		InputStream fileStream =
			getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");

		DataTableWritable dt = TabDelimFileUtil.readAsDouble(fileStream,
				true, DataLoader.DO_NOT_INDEX);
		// NOTE: in current implementation, the existence/need for indices is
		// deduced in the read. In the future, read as Double should not do so.
		
		
		runIDTest(dt);
		// TODO also try indices

	}

	public void runIDTest(DataTable dt) throws Exception {

		assertEquals(0, dt.getRowForId(1L));
		assertEquals(1, dt.getRowForId(11L));
		assertEquals(2, dt.getRowForId(21L));
		assertEquals(3, dt.getRowForId(31L));
		assertEquals(9, dt.getRowForId(91L));

		//should not be found (-1)
		assertEquals(-1, dt.getRowForId(99L));

		//Check int Column creation
		int[] firstCol = DataTableUtils.getIntColumn(dt, 0);
		int[] lastCol = DataTableUtils.getIntColumn(dt, 3);
		assertEquals(5, lastCol[0]);	//Check 1st val
		assertEquals(95, lastCol[9]);	//Check last col
		assertEquals(10, lastCol.length);
		assertEquals(2, firstCol[0]);	//Check 1st val
		assertEquals(92, firstCol[9]);	//Check last col
		assertEquals(10, firstCol.length);

		//Check int Row creation
		int[] firstRow = DataTableUtils.getIntRow(dt, 0);
		int[] lastRow = DataTableUtils.getIntRow(dt, 9);
		assertEquals(2, firstRow[0]);	//Check 1st val
		assertEquals(5, firstRow[3]);	//Check last col
		assertEquals(4, firstRow.length);
		assertEquals(92, lastRow[0]);	//Check 1st val
		assertEquals(95, lastRow[3]);	//Check last col
		assertEquals(4, lastRow.length);

		//Check double Column creation
		double[] firstDCol = DataTableUtils.getDoubleColumn(dt, 0);
		double[] lastDCol = DataTableUtils.getDoubleColumn(dt, 3);
		assertEquals(5d, lastDCol[0]);	//Check 1st val
		assertEquals(95d, lastDCol[9]);	//Check last col
		assertEquals(10, lastDCol.length);
		assertEquals(2d, firstDCol[0]);	//Check 1st val
		assertEquals(92d, firstDCol[9]);	//Check last col
		assertEquals(10, firstDCol.length);

		//Check double Row creation
		double[] firstDRow = DataTableUtils.getDoubleRow(dt, 0);
		double[] lastDRow = DataTableUtils.getDoubleRow(dt, 9);
		assertEquals(2d, firstDRow[0]);	//Check 1st val
		assertEquals(5d, firstDRow[3]);	//Check last col
		assertEquals(4, firstDRow.length);
		assertEquals(92d, lastDRow[0]);	//Check 1st val
		assertEquals(95d, lastDRow[3]);	//Check last col
		assertEquals(4, lastDRow.length);

	}

}

