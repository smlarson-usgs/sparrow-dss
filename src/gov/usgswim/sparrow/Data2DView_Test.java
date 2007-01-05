package gov.usgswim.sparrow;

import gov.usgswim.sparrow.util.SparrowUtil;

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
}
