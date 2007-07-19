package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DBuilder;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.Data2DViewWriteLocal;
import gov.usgswim.sparrow.Data2DViewWriteThru;
import gov.usgswim.sparrow.Data2DWritable;
import gov.usgswim.sparrow.Int2DImm;

import junit.framework.TestCase;

public class Data2DViewWriteLocal_Test extends TestCase {
	static final double DELTA = .00000000000000001d;
	
	String[] headings = new String[] { "c0", "c1", "c2" };

	double[][] doubleData =
		 new double[][] {
			{ .2d, .3d, .4d, .5d },
			{ .2, .3, .4, .5 },
			{ .2, .3, .4, .5 },
			{ .2, .3, .4, .5 },
			{ .2, .3, .4, .5 },
			{ .2, .3, .4, .5 },
			{ .2, .3, .4, .5 },
		};

	public Data2DViewWriteLocal_Test(String testName) {
		super(testName);
	}


	public void testBasic1() throws Exception {
		Data2DWritable dataBuilder = new Data2DBuilder(doubleData, headings);
		dataBuilder.setIdColumn(0);
		Data2D dataImm = ((Data2DBuilder)dataBuilder).buildDoubleImmutable(0);

		runBasicTestA(new Data2DViewWriteLocal(dataImm, 0, 4, 0));
		runBasicTestB(new Data2DViewWriteLocal(dataImm, 0, 4, 0));
		runBasicTestA(new Data2DViewWriteLocal(dataBuilder, 0, 4, 0));	//test w/ different base data implementatino
		runBasicTestB(new Data2DViewWriteLocal(dataBuilder, 0, 4, 0));
	}
	

	
	/**
	 * Test assumes a view of the entire doubleData data w/ index on column 0.
	 * 
	 * @throws Exception
	 */
	public void runBasicTestA(Data2DWritable data) throws Exception {

		this.assertEquals(4, data.getColCount());
		this.assertEquals(7, data.getRowCount());

		//Test starting values
		this.assertEquals(.2d, data.getDouble(0, 0));
		this.assertEquals(0, data.getInt(0, 0));
		this.assertEquals(.5d, data.getDouble(6, 3));
		this.assertEquals(new Double(.5d), data.getValueAt(6, 3));

		//These tests are outside the column bound and should throw errors
		try {
			data.getInt(0, 4);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			data.getInt(7, 0);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}

		try {
			Data2DViewWriteLocal data2 = new Data2DViewWriteLocal(data, 0, 5);
			this.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			//expected
		}
		
		try {
			Data2DViewWriteLocal data2 = new Data2DViewWriteLocal(data, 7, 0);
			this.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			//expected
		}
		
		try {
			Data2DViewWriteLocal data2 = new Data2DViewWriteLocal(data, 2, 2, 2);
			this.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			//expected
		}

	}
	
	/**
	 * Test assumes a view of the entire doubleData data w/ index on column 0.
	 * 
	 * It is assumed that the underlying data cannot change (this is not tested)
	 * Set tests
	 * @throws Exception
	 */
	public void runBasicTestB(Data2DWritable data) throws Exception {


		this.assertEquals(.2d, data.getDouble(0, 0), 0d);	//initial value
		data.setValueAt(new Double(9.9), 0, 0);							//set new value
		this.assertEquals(new Double(9.9d), data.getValueAt(0, 0).doubleValue(), 0d);
		this.assertEquals(9.9d, data.getDouble(0, 0), 0d);
		this.assertEquals(9, data.getInt(0, 0));
		data.setValueAt((Number)null, 0, 0);							//reset to initial (Number)
		this.assertEquals(.2d, data.getDouble(0, 0), 0d);	//initial value
		this.assertEquals(new Double(.2d), data.getValueAt(0, 0).doubleValue(), 0d);
		this.assertEquals(0, data.getInt(0, 0));
		
		//Try the reset as string
		data.setValueAt(new Double(9.9), 0, 0);							//set new value
		data.setValueAt((Number)null, 0, 0);							//reset to initial (Number)
		this.assertEquals(.2d, data.getDouble(0, 0), 0d);	//initial value
		
		
		//Test out of bounds
		
		try {
			data.setValueAt(5d, 0, 4);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
		try {
			data.setValueAt(5, 7, 1);
			this.fail("Should have thrown exception");
		} catch (IndexOutOfBoundsException e) {
			//expected
		}
		
	}
	

	
	//TODO:  FIX THIS TEST!!
	/*
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
	*/
}
