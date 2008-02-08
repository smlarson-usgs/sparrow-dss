package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.util.TabDelimFileUtil;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 * Tests the Double2DImm class
 */
public class Double2D_Test extends TestCase {


	public Double2D_Test(String testName) {
		super(testName);
	}

	public void testBasic() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
				
		Data2D data2D = TabDelimFileUtil.readAsDouble(fileStream, true, 0);	//Immutable instance
		int[] lastCol = data2D.getIntColumn(4);
		Double2DImm dbl2DImm = new Double2DImm(data2D.getDoubleData(), data2D.getHeadings(), 0, lastCol);
		
		runBasicTest((Double2DImm) data2D);
		runIDTest(dbl2DImm);

	}
	
	public void runBasicTest(Data2D data2D) throws Exception {

		this.assertEquals(0, data2D.findRowByIndex(1d));
		this.assertEquals(1, data2D.findRowByIndex(11d));
		this.assertEquals(2, data2D.findRowByIndex(21d));
		this.assertEquals(3, data2D.findRowByIndex(31d));
		this.assertEquals(9, data2D.findRowByIndex(91d));

		//should not be found (-1)
		this.assertEquals(-1, data2D.findRowByIndex(99d));
		
		//Check int Column creation
		int[] firstCol = data2D.getIntColumn(0);
		int[] lastCol = data2D.getIntColumn(4);
		this.assertEquals(5, lastCol[0]);	//Check 1st val
		this.assertEquals(95, lastCol[9]);	//Check last col
		this.assertEquals(10, lastCol.length);
		this.assertEquals(1, firstCol[0]);	//Check 1st val
		this.assertEquals(91, firstCol[9]);	//Check last col
		this.assertEquals(10, firstCol.length);
		
		//Check int Row creation
		int[] firstRow = data2D.getIntRow(0);
		int[] lastRow = data2D.getIntRow(9);
		this.assertEquals(1, firstRow[0]);	//Check 1st val
		this.assertEquals(5, firstRow[4]);	//Check last col
		this.assertEquals(5, firstRow.length);
		this.assertEquals(91, lastRow[0]);	//Check 1st val
		this.assertEquals(95, lastRow[4]);	//Check last col
		this.assertEquals(5, lastRow.length);
		
		//Check double Column creation
		double[] firstDCol = data2D.getDoubleColumn(0);
		double[] lastDCol = data2D.getDoubleColumn(4);
		this.assertEquals(5d, lastDCol[0]);	//Check 1st val
		this.assertEquals(95d, lastDCol[9]);	//Check last col
		this.assertEquals(10, lastDCol.length);
		this.assertEquals(1d, firstDCol[0]);	//Check 1st val
		this.assertEquals(91d, firstDCol[9]);	//Check last col
		this.assertEquals(10, firstDCol.length);
		
		//Check double Row creation
		double[] firstDRow = data2D.getDoubleRow(0);
		double[] lastDRow = data2D.getDoubleRow(9);
		this.assertEquals(1d, firstDRow[0]);	//Check 1st val
		this.assertEquals(5d, firstDRow[4]);	//Check last col
		this.assertEquals(5, firstDRow.length);
		this.assertEquals(91d, lastDRow[0]);	//Check 1st val
		this.assertEquals(95d, lastDRow[4]);	//Check last col
		this.assertEquals(5, lastDRow.length);
		
	}
	
	public void runIDTest(Data2D data2D) throws Exception {
		this.assertEquals(0, data2D.findRowById(5));	//ID of first row
		this.assertEquals(9, data2D.findRowById(95));	//ID of last row
		this.assertEquals(5, data2D.getIdForRow(0).intValue());
		this.assertEquals(95, data2D.getIdForRow(9).intValue());
	}
	

}
