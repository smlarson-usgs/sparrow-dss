package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.Int2D;
import gov.usgswim.sparrow.util.SparrowUtil;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;

public class Data2DView_Test extends TestCase {

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


	/**
	 * Test column trimming
	 * @throws Exception
	 */
	public void testBasic1() throws Exception {

		Int2D int2D = new Int2D(intData, headings);
		Double2D double2D = new Double2D(doubleData, headings);

		Data2DView int2DView = new Data2DView(int2D, 0, 2);
		Data2DView double2DView = new Data2DView(double2D, 1, 2);

		//int2DView
		this.assertEquals(2, int2DView.getColCount());
		this.assertEquals(7, int2DView.getRowCount());
		this.assertEquals("c0", int2DView.getHeading(0));
		this.assertEquals("c0", int2DView.getHeading(0, true));
		this.assertEquals("c0", int2DView.getHeadings()[0]);
		this.assertEquals("c1", int2DView.getHeadings()[1]);

		this.assertEquals(0, int2DView.getInt(0, 0));
		this.assertEquals(new Integer(0), int2DView.getValueAt(0, 0));
		this.assertEquals(0d, int2DView.getDouble(0, 0), 0d);

		this.assertEquals("c1", int2DView.getHeading(1));
		this.assertEquals(2, int2DView.getInt(0, 1));
		this.assertEquals(new Integer(2), int2DView.getValueAt(0, 1));
		this.assertEquals(2d, int2DView.getDouble(0, 1), 0d);

		//double2DView
		this.assertEquals(2, double2DView.getColCount());
		this.assertEquals(7, double2DView.getRowCount());
		this.assertEquals("c1", double2DView.getHeading(0));
		this.assertEquals("c1", double2DView.getHeadings()[0]);
		this.assertEquals("c2", double2DView.getHeadings()[1]);

		this.assertEquals(0, double2DView.getInt(0, 0));
		this.assertEquals(new Double(.3d), double2DView.getValueAt(0, 0));
		this.assertEquals(.3d, double2DView.getDouble(0, 0), 0d);

		this.assertEquals(0, double2DView.getInt(0, 1));
		this.assertEquals(new Double(.4d), double2DView.getValueAt(0, 1));
		this.assertEquals(.4d, double2DView.getDouble(0, 1), 0d);
		this.assertEquals("c2", double2DView.getHeading(1));


		//Test Setting values
		double2DView.setValueAt(new Double(9.9), 0, 0);
		this.assertEquals(9.9, double2DView.getDouble(0, 0), 0d);
	  this.assertEquals(9.9, double2D.getDouble(0, 1), 0d);	//test base data
		
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
			Data2DView int2DView2 = new Data2DView(int2D, 0, 4);
			this.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			//expected
		}

	}

	/**
	 * Test row and column trimming
	 * @throws Exception
	 */
	public void testBasic2() throws Exception {

		Int2D int2D = new Int2D(intData, headings);
		Data2DView int2DView = new Data2DView(int2D, 1, 5, 0, 2);


		//int2DView
		this.assertEquals(6d, int2DView.findMaxValue(), 0d);
		this.assertEquals(5, int2DView.getRowCount());
		this.assertEquals("c0", int2DView.getHeading(0));


		this.assertEquals(1, int2DView.getInt(0, 0));
		this.assertEquals(new Integer(1), int2DView.getValueAt(0, 0));
		this.assertEquals(1d, int2DView.getDouble(0, 0), 0d);

		this.assertEquals("c1", int2DView.getHeading(1));
		this.assertEquals(4, int2DView.getInt(4, 0));
		this.assertEquals(new Integer(4), int2DView.getValueAt(4, 0));
		this.assertEquals(4d, int2DView.getDouble(4, 0), 0d);

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
	
	/**
	 * Test findMax value
	 * @throws Exception
	 */
	public void testBasic3() throws Exception {

		Int2D int2D = new Int2D(intData, headings);
		Data2DView int2DView = new Data2DView(int2D, 1, 5, 0, 2);


		this.assertEquals(6d, int2DView.findMaxValue(), 0d);
		this.assertEquals(7d, int2D.findMaxValue(), 0d);

	}
	
	public void testfindById() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		Double2D data2Dbase = TabDelimFileUtil.readAsDouble(fileStream, true);
		Data2DView data2D = new Data2DView(data2Dbase, 1, 9, 1, 2);
		data2D.setIdColumn(0);

		this.assertEquals(0, data2D.findRowById(12d));
		this.assertEquals(1, data2D.findRowById(22d));
		this.assertEquals(2, data2D.findRowById(32d));
		this.assertEquals(3, data2D.findRowById(42d));
		this.assertEquals(8, data2D.findRowById(92d));

		//should not be found (-1)
		this.assertEquals(-1, data2D.findRowById(99d));
		
		
		//
		// Change some values and make sure we find them.
		//
		data2D.setValueAt(new Integer(99), 8, 0);
		this.assertEquals(8, data2D.findRowById(99d));
		
		data2D.setValueAt(new Integer(-1), 0, 0);
		this.assertEquals(0, data2D.findRowById(-1d));
		
		//
		// Change the index to the 2nd column.
		//
		data2D.setValueAt(new Integer(99), 0, 1);	//update one row b/f changing index
		data2D.setIdColumn(1);
		this.assertEquals(0, data2D.findRowById(99d));
		this.assertEquals(1, data2D.findRowById(23d));
		this.assertEquals(2, data2D.findRowById(33d));
		this.assertEquals(3, data2D.findRowById(43d));
		this.assertEquals(8, data2D.findRowById(93d));
	}
}
