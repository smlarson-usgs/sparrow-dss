package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;


//TODO: Rename
public class Int2D_Test extends TestCase {


	public Int2D_Test(String testName) {
		super(testName);
	}


	public void testInteger() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
		Int2DImm data2D = TabDelimFileUtil.readAsInteger(fileStream, true, 0);

		this.assertEquals(0, data2D.findRowById(1d));
		this.assertEquals(1, data2D.findRowById(11d));
		this.assertEquals(2, data2D.findRowById(21d));
		this.assertEquals(3, data2D.findRowById(31d));
		this.assertEquals(9, data2D.findRowById(91d));

		//should not be found (-1)
		this.assertEquals(-1, data2D.findRowById(99d));
		
	}

}
