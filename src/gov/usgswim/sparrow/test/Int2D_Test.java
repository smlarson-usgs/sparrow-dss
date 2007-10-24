package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Tests the Int2DImm class
 */
public class Int2D_Test extends Double2D_Test {


	public Int2D_Test(String testName) {
		super(testName);
	}

	public void testBasic() throws Exception {
		InputStream fileStream =
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
				
		Data2D data2D = TabDelimFileUtil.readAsInteger(fileStream, true, 0);	//Immutable instance
		int[] lastCol = data2D.getIntColumn(4);
		Int2DImm int2DImm = new Int2DImm(data2D.getIntData(), data2D.getHeadings(), 0, lastCol);

		runBasicTest((Int2DImm) data2D);
		runIDTest(int2DImm);
	}
	

}
