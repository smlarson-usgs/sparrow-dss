package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.Int2D;

import java.io.InputStream;

import junit.framework.TestCase;

public class TabDelimFileReader_Test extends TestCase{

	
	public TabDelimFileReader_Test(String testName) {
		super(testName);
	}
	
	public void testDouble1() throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/tab_delimit_sample.txt");
		double[][] data = TabDelimFileUtil.readAsDouble(fileStream, false).getData();
		
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
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/tab_delimit_sample_gap.txt");
		double[][] data = TabDelimFileUtil.readAsDouble(fileStream, false).getData();
		
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
		 InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/tab_delimit_sample_heading.txt");
		 Double2D data2D = TabDelimFileUtil.readAsDouble(fileStream, true);
		 double[][] data = data2D.getData();
		 
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
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/tab_delimit_sample_err1.txt");
		
		
		try {
		  double[][] data = TabDelimFileUtil.readAsDouble(fileStream, false).getData();
		} catch (NumberFormatException e) {
			return;	//terminate normally - this error is expected
		}
		
		this.fail("A number format exception was expected b/c the file has mis-matched column counts.");
	}
	
	
	
	/**
	 * Tests that empty lines are skipped.
	 * @throws Exception
	 */
	public void testInteger1() throws Exception {
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/tab_delimit_sample_gap_int.txt");
		int[][] data = TabDelimFileUtil.readAsInteger(fileStream, false).getData();
		
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
		 InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/tab_delimit_sample_heading.txt");
	   Int2D data2D = TabDelimFileUtil.readAsInteger(fileStream, true);
		 int[][] data = data2D.getData();
		 
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
}
