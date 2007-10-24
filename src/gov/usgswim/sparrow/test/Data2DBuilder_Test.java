package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;

import gov.usgswim.sparrow.Data2DBuilder;
import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.util.TabDelimFileUtil;
import java.io.InputStream;
import junit.framework.TestCase;

public class Data2DBuilder_Test extends TestCase {


	public Data2DBuilder_Test(String testName) {
		super(testName);
	}


	public void testDouble() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		Data2DBuilder data2D = TabDelimFileUtil.read(fileStream, true);
		data2D.setIndexColumn(0);

		this.assertEquals(0, data2D.findRowByIndex(1d));
		this.assertEquals(1, data2D.findRowByIndex(11d));
		this.assertEquals(2, data2D.findRowByIndex(21d));
		this.assertEquals(3, data2D.findRowByIndex(31d));
		this.assertEquals(9, data2D.findRowByIndex(91d));

		//should not be found (-1)
		this.assertEquals(-1, data2D.findRowByIndex(99d));
		
		
		//
		// Change some values and make sure we find them.
		//
		data2D.setValueAt(new Integer(99), 9, 0);
		this.assertEquals(9, data2D.findRowByIndex(99d));
		
		data2D.setValueAt(new Integer(-1), 0, 0);
		this.assertEquals(0, data2D.findRowByIndex(-1d));
		
		//
		// Change the index to the 2nd column.
		//
		data2D.setIndexColumn(1);
		this.assertEquals(0, data2D.findRowByIndex(2d));
		this.assertEquals(1, data2D.findRowByIndex(12d));
		this.assertEquals(2, data2D.findRowByIndex(22d));
		this.assertEquals(3, data2D.findRowByIndex(32d));
		this.assertEquals(9, data2D.findRowByIndex(92d));
	}

}
