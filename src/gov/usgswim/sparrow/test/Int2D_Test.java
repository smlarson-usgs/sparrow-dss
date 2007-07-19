package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Int2D;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Tests both the Int2D and Int2DImm classes
 */
public class Int2D_Test extends TestCase {


	public Int2D_Test(String testName) {
		super(testName);
	}

	public void testBasic() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
				
		Data2D data2D = TabDelimFileUtil.readAsInteger(fileStream, true, 0);	//Immutable instance
		
		runBasicTest(data2D);
		runBasicTest(new Int2D(data2D.getIntData(), data2D.getHeadings(), 0));
	}
	
	public void runBasicTest(Data2D data2D) throws Exception {

		this.assertEquals(0, data2D.findRowById(1d));
		this.assertEquals(1, data2D.findRowById(11d));
		this.assertEquals(2, data2D.findRowById(21d));
		this.assertEquals(3, data2D.findRowById(31d));
		this.assertEquals(9, data2D.findRowById(91d));

		//should not be found (-1)
		this.assertEquals(-1, data2D.findRowById(99d));
		
	}

}
