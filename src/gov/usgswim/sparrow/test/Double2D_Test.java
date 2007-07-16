package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.Int2D;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import java.util.Arrays;

import junit.framework.TestCase;

public class Double2D_Test extends TestCase {


	public Double2D_Test(String testName) {
		super(testName);
	}


	public void testDouble() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		Double2D data2D = TabDelimFileUtil.readAsDouble(fileStream, true);
		data2D.setIdColumn(0);

		this.assertEquals(0, data2D.findRowById(1d));
		this.assertEquals(1, data2D.findRowById(11d));
		this.assertEquals(2, data2D.findRowById(21d));
		this.assertEquals(3, data2D.findRowById(31d));
		this.assertEquals(9, data2D.findRowById(91d));

		//should not be found (-1)
		this.assertEquals(-1, data2D.findRowById(99d));
		
		
		//
		// Change some values and make sure we find them.
		//
		data2D.setValueAt(new Integer(99), 9, 0);
		this.assertEquals(9, data2D.findRowById(99d));
		
		data2D.setValueAt(new Integer(-1), 0, 0);
		this.assertEquals(0, data2D.findRowById(-1d));
		
		//
		// Change the index to the 2nd column.
		//
		data2D.setIdColumn(1);
		this.assertEquals(0, data2D.findRowById(2d));
		this.assertEquals(1, data2D.findRowById(12d));
		this.assertEquals(2, data2D.findRowById(22d));
		this.assertEquals(3, data2D.findRowById(32d));
		this.assertEquals(9, data2D.findRowById(92d));
	}

}
