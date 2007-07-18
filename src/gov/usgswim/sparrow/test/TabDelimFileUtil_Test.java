package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import java.util.Arrays;

import junit.framework.TestCase;


public class TabDelimFileUtil_Test extends TestCase{

	
	public TabDelimFileUtil_Test(String testName) {
		super(testName);
	}
	
	public void testDouble1() throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample.txt");
		double[][] data = TabDelimFileUtil.readAsDouble(fileStream, false, -1).getDoubleData();
		
		this.assertEquals(1d, data[0][0], 0d);
	  this.assertEquals(5.14159d, data[0][4], 0d);
	  this.assertEquals(91d, data[9][0], 0d);
	  this.assertEquals(95d, data[9][4], 0d);
		
		//SparrowUtil.print2DArray(data, "Double: Loaded from /gov/usgswim/sparrow/tab_delimit_sample.txt");
	}
	
	/**
	 * Tests that empty lines are skipped.
	 * @throws Exception
	 */
	public void testDouble2() throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_gap.txt");
		double[][] data = TabDelimFileUtil.readAsDouble(fileStream, false, -1).getDoubleData();
		
		this.assertEquals(1d, data[0][0], 0d);
		this.assertEquals(5.14159d, data[0][4], 0d);
		this.assertEquals(91d, data[9][0], 0d);
		this.assertEquals(95d, data[9][4], 0d);
		
		//SparrowUtil.print2DArray(data, "Double: Loaded from /gov/usgswim/sparrow/tab_delimit_sample_gap.txt");
	}
	
	 /**
		* Tests that headings work.
		* @throws Exception
		*/
	 public void testDouble3() throws Exception {
		 InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		 Double2DImm data2D = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		 double[][] data = data2D.getDoubleData();
		 
		 this.assertEquals(1d, data[0][0], 0d);
		 this.assertEquals(5d, data[0][4], 0d);
		 this.assertEquals(91d, data[9][0], 0d);
		 this.assertEquals(95d, data[9][4], 0d);
		 
		 this.assertEquals("One", data2D.getHeading(0));
		 this.assertEquals("Two", data2D.getHeading(1));
		 this.assertEquals("Three", data2D.getHeading(2));
		 this.assertEquals("Four", data2D.getHeading(3));
		 this.assertEquals("", data2D.getHeading(4));
		 
		 this.assertNull(data2D.getHeading(4, false));
	 }
	
	/**
	 * Tests that an error is thrown if the number of columns varies b/t the rows.
	 * @throws Exception
	 */
	public void testDoubleColumnCountError() throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_err1.txt");
		
		
		try {
		  double[][] data = TabDelimFileUtil.readAsDouble(fileStream, false, -1).getDoubleData();
		} catch (IllegalStateException e) {
			return;	//terminate normally - this error is expected
		}
		
		this.fail("A number format exception was expected b/c the file has mis-matched column counts.");
	}
	
	
	
	/**
	 * Tests that empty lines are skipped.
	 * @throws Exception
	 */
	public void testInteger1() throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_gap_int.txt");
		int[][] data = TabDelimFileUtil.readAsInteger(fileStream, false, -1).getIntData();
		
	  this.assertEquals(1, data[0][0]);
	  this.assertEquals(5, data[0][4]);
	  this.assertEquals(91, data[9][0]);
	  this.assertEquals(95, data[9][4]);
		
		//SparrowUtil.print2DArray(data, "Integer:  Loaded from /gov/usgswim/sparrow/tab_delimit_sample_gap_int.txt");
	}
	
	 /**
		* Tests that headings work.
		* @throws Exception
		*/
	 public void testInteger2() throws Exception {
		 InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
	   Int2DImm data2D = TabDelimFileUtil.readAsInteger(fileStream, true, -1);
		 int[][] data = data2D.getIntData();
		 
		 this.assertEquals(1, data[0][0]);
		 this.assertEquals(5, data[0][4]);
		 this.assertEquals(91, data[9][0]);
		 this.assertEquals(95, data[9][4]);
		 
	   this.assertEquals("One", data2D.getHeading(0));
	   this.assertEquals("Two", data2D.getHeading(1));
	   this.assertEquals("Three", data2D.getHeading(2));
	   this.assertEquals("Four", data2D.getHeading(3));
	   this.assertEquals("", data2D.getHeading(4));
		 
	   this.assertNull(data2D.getHeading(4, false));
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
		 
		 this.assertTrue(Arrays.equals(expectedMap, mapped));
	 }
	 
	 public void testIntColumnMapping() throws Exception {
		 
		 String[] mappedHeadings = new String[] {
			 "LOCAL_ID", "STD_ID", "LOCAL_SAME"
		 };
		 
		 InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
		 Int2DImm data = TabDelimFileUtil.readAsInteger(fileStream, true, mappedHeadings, -1);
		 
		 this.assertEquals(3, data.getColCount());
		 
		 this.assertEquals(3074, data.getInt(0,0));
		 this.assertEquals(3074, data.getInt(0,1));
		 this.assertEquals(1, data.getInt(0,2));
		 this.assertEquals(0, data.getInt(1,2));
		 
		 this.assertTrue(Arrays.equals(mappedHeadings, data.getHeadings()));
		 
		 
			//now try mapping a bad column
		 try {
			mappedHeadings = new String[] {"LOCAL_ID", "STD_ID", "LOCAL_SAME", "lkjhdlkfhlkhlskdfh"};
			fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
			data = TabDelimFileUtil.readAsInteger(fileStream, true, mappedHeadings, -1);
			this.fail("Should have thrown an exception b/c the column does not exist.");
		 } catch (IllegalArgumentException e) {
			 //exception is expected.
		 }
	 }
	 
	 
	 public void testDoubleColumnMapping() throws Exception {
		 
		 String[] mappedHeadings = new String[] {
			 "LOCAL_ID", "STD_ID", "LOCAL_SAME"
		 };
		 
		 InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
		 Double2DImm data = TabDelimFileUtil.readAsDouble(fileStream, true, mappedHeadings, -1);
		 
		 this.assertEquals(3, data.getColCount());
		 
		 this.assertEquals(3074d, data.getDouble(0,0));
		 this.assertEquals(3074d, data.getDouble(0,1));
		 this.assertEquals(1d, data.getDouble(0,2));
		 this.assertEquals(0d, data.getDouble(1,2));
		 
		 this.assertTrue(Arrays.equals(mappedHeadings, data.getHeadings()));
		 
		 //now try mapping a bad column
		 try {
			mappedHeadings = new String[] {"LOCAL_ID", "STD_ID", "LOCAL_SAME", "lkjhdlkfhlkhlskdfh"};
			fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/ancil.txt");
			data = TabDelimFileUtil.readAsDouble(fileStream, true, mappedHeadings, -1);
			this.fail("Should have thrown an exception b/c the column does not exist.");
		 } catch (IllegalArgumentException e) {
			 //exception is expected.
		 }
	 }
	 
}
