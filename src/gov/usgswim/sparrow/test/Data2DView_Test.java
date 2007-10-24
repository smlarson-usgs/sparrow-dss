package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DBuilder;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.Data2DViewWriteLocal;
import gov.usgswim.sparrow.Data2DViewWriteThru;
import gov.usgswim.sparrow.Data2DWritable;
import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.Int2DImm;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

public class Data2DView_Test extends TestCase {
	static final double DELTA = .00000000000000001d;
	
	String[] headings = new String[] { "c0", "c1", "c2" };
	int[][] intData =
		 new int[][] {
			{ 0, 2, 1 },
			{ 1, 2, 1 },
			{ 2, 4, 1 },
			{ 3, 4, 1 },
			{ 5, 6, 1 },
			{ 4, 6, 1 },
			{ 6, 7, 1 },
		};
	double[][] doubleData =
		 new double[][] {
			{ .2d, .3d, .4d },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
			{ .2, .3, .4 },
		};

	public Data2DView_Test(String testName) {
		super(testName);
	}


	public void testBasic1() throws Exception {
		Data2D int2D = new Data2DBuilder(intData, headings);
		Data2D double2D = new Data2DBuilder(doubleData, headings);
		runBasicTestA(int2D, double2D);
		runBasicTestB(int2D);
	}
	
	public void testBasic2() throws Exception {
		Data2D int2D = new Data2DBuilder(intData, headings).buildIntImmutable(-1);
		Data2D double2D = new Data2DBuilder(doubleData, headings).buildDoubleImmutable(-1);
		runBasicTestA(int2D, double2D);
		runBasicTestB(int2D);
	}


	public void runBasicTestA(Data2D int2D, Data2D double2D) throws Exception {
		runBasicTestAWithViews(new Data2DView(int2D, 0, 2),new Data2DView(double2D, 1, 2));
		
		if (int2D instanceof Data2DWritable && double2D instanceof Data2DWritable) {
			runBasicTestAWithViews(
				new Data2DViewWriteThru((Data2DWritable)int2D, 0, 2),
				new Data2DViewWriteThru((Data2DWritable)double2D, 1, 2));
		}
	}
	
	/**
	 * Test assumes these two datasets are passed:
	 * new Data2DView(int2D, 0, 2)
	 * new Data2DView(double2D, 1, 2)
	 * 
	 * @throws Exception
	 */
	public void runBasicTestAWithViews(Data2DView int2DView, Data2DView double2DView) throws Exception {

		//int2DView
		this.assertEquals(2, int2DView.getColCount());
		this.assertEquals(7, int2DView.getRowCount());
		this.assertEquals("c0", int2DView.getHeading(0));
		this.assertEquals("c0", int2DView.getHeading(0, true));
		this.assertEquals("c0", int2DView.getHeadings()[0]);
		this.assertEquals("c1", int2DView.getHeadings()[1]);

		this.assertEquals(0, int2DView.getInt(0, 0));
		this.assertEquals(0d, int2DView.getValue(0, 0).doubleValue());
		this.assertEquals(0d, int2DView.getDouble(0, 0));

		this.assertEquals("c1", int2DView.getHeading(1));
		this.assertEquals(2, int2DView.getInt(0, 1));
		this.assertEquals(2d, int2DView.getValue(0, 1).doubleValue());
		this.assertEquals(2d, int2DView.getDouble(0, 1), 0d);
		
		//--int rows and columns
		int[] row0 = int2DView.getIntRow(0);
		int[] row6 = int2DView.getIntRow(6);
		int[] col0 = int2DView.getIntColumn(0);
		int[] col1 = int2DView.getIntColumn(1);
		this.assertTrue(ArrayUtils.isEquals(new int[] {0, 2}, row0));
		this.assertTrue(ArrayUtils.isEquals(new int[] {6, 7}, row6));
		this.assertTrue(ArrayUtils.isEquals(new int[] {0, 1, 2, 3, 5, 4, 6}, col0));
		this.assertTrue(ArrayUtils.isEquals(new int[] {2, 2, 4, 4, 6, 6, 7}, col1));
		//--double rows and columns
		double[] rowd0 = int2DView.getDoubleRow(0);
		double[] rowd6 = int2DView.getDoubleRow(6);
		double[] cold0 = int2DView.getDoubleColumn(0);
		double[] cold1 = int2DView.getDoubleColumn(1);
		this.assertTrue(ArrayUtils.isEquals(new double[] {0d, 2d}, rowd0));
		this.assertTrue(ArrayUtils.isEquals(new double[] {6d, 7d}, rowd6));
		this.assertTrue(ArrayUtils.isEquals(new double[] {0d, 1d, 2d, 3d, 5d, 4d, 6d}, cold0));
		this.assertTrue(ArrayUtils.isEquals(new double[] {2d, 2d, 4d, 4d, 6d, 6d, 7d}, cold1));

		//
		//double2DView
		this.assertEquals(2, double2DView.getColCount());
		this.assertEquals(7, double2DView.getRowCount());
		this.assertEquals("c1", double2DView.getHeading(0));
		this.assertEquals("c1", double2DView.getHeadings()[0]);
		this.assertEquals("c2", double2DView.getHeadings()[1]);

		this.assertEquals(0, double2DView.getInt(0, 0));
		this.assertEquals(.3d, double2DView.getValue(0, 0).doubleValue());
		this.assertEquals(.3d, double2DView.getDouble(0, 0), 0d);

		this.assertEquals(0, double2DView.getInt(0, 1));
		this.assertEquals(.4d, double2DView.getValue(0, 1).doubleValue());
		this.assertEquals(.4d, double2DView.getDouble(0, 1), 0d);
		this.assertEquals("c2", double2DView.getHeading(1));
		
		//--int rows and columns
		row0 = double2DView.getIntRow(0);
		row6 = double2DView.getIntRow(6);
		col0 = double2DView.getIntColumn(0);
		col1 = double2DView.getIntColumn(1);
		this.assertTrue(ArrayUtils.isEquals(new int[] {0, 0}, row0));
		this.assertTrue(ArrayUtils.isEquals(new int[] {0, 0}, row6));
		this.assertTrue(ArrayUtils.isEquals(new int[] {0, 0, 0, 0, 0, 0, 0}, col0));
		this.assertTrue(ArrayUtils.isEquals(new int[] {0, 0, 0, 0, 0, 0, 0}, col1));
		//--double rows and columns
		rowd0 = double2DView.getDoubleRow(0);
		rowd6 = double2DView.getDoubleRow(6);
		cold0 = double2DView.getDoubleColumn(0);
		cold1 = double2DView.getDoubleColumn(1);
		this.assertTrue(ArrayUtils.isEquals(new double[] {.3d, .4d}, rowd0));
		this.assertTrue(ArrayUtils.isEquals(new double[] {.3d, .4d}, rowd6));
		this.assertTrue(ArrayUtils.isEquals(new double[] {.3d, .3d, .3d, .3d, .3d, .3d, .3d}, cold0));
		this.assertTrue(ArrayUtils.isEquals(new double[] {.4d, .4d, .4d, .4d, .4d, .4d, .4d}, cold1));
		


		//These tests are outside the column bound and should throw errors
		try {
			int2DView.getInt(0, 2);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			double2DView.getInt(0, 2);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			Data2DView int2DView2 = new Data2DView(int2DView, 0, 4);
			this.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			//expected
		}

	}
	
	public void testSet1() throws Exception {

		Data2DBuilder int2D = new Data2DBuilder(intData, headings);
		Data2DBuilder double2D = new Data2DBuilder(doubleData, headings);
		
		
		Data2DViewWriteThru int2DView = new Data2DViewWriteThru(int2D, 0, 2, 0);
		Data2DViewWriteThru double2DView = new Data2DViewWriteThru(double2D, 1, 2, 0);


		//Test Setting values on Int
		int2DView.setValueAt(new Double(9.9), 0, 0);
		this.assertEquals(9.9d, int2DView.getDouble(0, 0), 0d);
	  this.assertEquals(9.9d, int2D.getDouble(0, 0), 0d);	//test base data
		this.assertEquals(9, int2DView.getInt(0, 0));
	  this.assertEquals(9, int2D.getInt(0, 0));	//test base data
		
		int2DView.setValueAt(new Integer(5), 6, 1);
		this.assertEquals(5, int2DView.getInt(6, 1));
		this.assertEquals(5d, int2D.getDouble(6, 1));	//test base data
		
		try {
			int2DView.setValueAt(new Integer(5), 0, 2);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		try {
			int2DView.setValueAt(new Integer(5), 7, 1);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		//
		//Test Setting values on Double
		double2DView.setValueAt(new Double(9.9), 0, 0);
		this.assertEquals(9.9d, double2DView.getDouble(0, 0));
	  this.assertEquals(9.9d, double2D.getDouble(0, 1));	//test base data

	}
	
	/**
	 * Test row and column trimming
	 * @throws Exception
	 */
	public void testSet2() throws Exception {
	
		Data2DBuilder int2D = new Data2DBuilder(intData, headings);
		Data2DViewWriteThru int2DView = new Data2DViewWriteThru(int2D, 1, 5, 0, 2);

		//Test Setting values
		int2DView.setValueAt(new Integer(9), 0, 0);
		this.assertEquals(9, int2DView.getInt(0, 0));
	  this.assertEquals(9, int2D.getInt(1, 0));
	  

		//These tests are outside the column/row bound and should throw errors
		try {
			int2DView.getInt(5, 0);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

	}


	public void runBasicTestB(Data2D int2D) throws Exception {
		runBasicTestBWithViews(new Data2DView(int2D, 1, 5, 0, 2));
		
		if (int2D instanceof Data2DWritable) {
			runBasicTestBWithViews(
				new Data2DViewWriteThru((Data2DWritable)int2D, 1, 5, 0, 2)
			);
		}
	}
	
	
	/**
	 * This test assumes we are given a view as: Data2DView(int2D, 1, 5, 0, 2).
	 * 
	 * @throws Exception
	 */
	public void runBasicTestBWithViews(Data2DView int2DView) throws Exception {

		//int2DView
		this.assertEquals(6d, int2DView.findMaxValue(), 0d);
		this.assertEquals(5, int2DView.getRowCount());
		this.assertEquals("c0", int2DView.getHeading(0));


		this.assertEquals(1, int2DView.getInt(0, 0));
		this.assertEquals(1d, int2DView.getValue(0, 0).doubleValue());
		this.assertEquals(1d, int2DView.getDouble(0, 0), 0d);

		this.assertEquals("c1", int2DView.getHeading(1));
		this.assertEquals(4, int2DView.getInt(4, 0));
		this.assertEquals(4d, int2DView.getValue(4, 0).doubleValue());
		this.assertEquals(4d, int2DView.getDouble(4, 0), 0d);

		//These tests are outside the column/row bound and should throw errors
		try {
			int2DView.getInt(5, 0);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

	}
	
	/**
	 * Test findMax value
	 * @throws Exception
	 */
	public void testBasic3() throws Exception {

		Int2DImm int2D = new Int2DImm(intData, headings);
		Data2DView int2DView = new Data2DView(int2D, 1, 5, 0, 2);


		this.assertEquals(6d, int2DView.findMaxValue(), 0d);
		this.assertEquals(7d, int2D.findMaxValue(), 0d);

	}
	

	/**
	 * Run findById tests on multiple view implementations.
	 * @throws Exception
	 */
	public void testFindById() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		Data2D data2Dbase = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		
		
		Data2DView data2DView = new Data2DView(data2Dbase, 1, 9, 1, 2, 0);
		Data2DView data2DViewWriteLocal = new Data2DViewWriteLocal(data2Dbase, 1, 9, 1, 2, 0);
		Data2DView data2DViewWriteThru = new Data2DViewWriteThru(
			new Data2DViewWriteLocal(data2Dbase), 1, 9, 1, 2, 0
		);	//using a writeLocal to provide a writable ontop of the Imm.
		
		runfindById(data2DView);
		runfindById(data2DViewWriteLocal);
		runfindById(data2DViewWriteThru);
	}
	
	/**
	 * Expects a view created as:
	 * file = /gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt
	 * new Data2DView(data2Dbase, 1, 9, 1, 2, 0);
	 * ...or similar
	 * 
	 * @param data2D
	 * @throws Exception
	 */
	public void runfindById(Data2D data2D) throws Exception {


		this.assertEquals(0, data2D.findRowByIndex(12d));
		this.assertEquals(1, data2D.findRowByIndex(22d));
		this.assertEquals(2, data2D.findRowByIndex(32d));
		this.assertEquals(3, data2D.findRowByIndex(42d));
		this.assertEquals(8, data2D.findRowByIndex(92d));

		//should not be found (-1)
		this.assertEquals(-1, data2D.findRowByIndex(99d));
		
		
		//
		// Change some values and make sure we find them.
		//
		if (data2D instanceof Data2DWritable) {
			Data2DWritable d2dw = (Data2DWritable)data2D;
			
			d2dw.setValueAt(new Integer(99), 8, 0);
			this.assertEquals(8, d2dw.findRowByIndex(99d));
			
			d2dw.setValueAt(new Integer(-1), 0, 0);
			this.assertEquals(0, d2dw.findRowByIndex(-1d));
			
			//
			// Change the index to the 2nd column.
			//
			d2dw.setValueAt(new Integer(99), 0, 1);	//update one row b/f changing index
			d2dw.setIndexColumn(1);
			this.assertEquals(0, d2dw.findRowByIndex(99d));
			this.assertEquals(1, d2dw.findRowByIndex(23d));
			this.assertEquals(2, d2dw.findRowByIndex(33d));
			this.assertEquals(3, d2dw.findRowByIndex(43d));
			this.assertEquals(8, d2dw.findRowByIndex(93d));
		}
	}

}
