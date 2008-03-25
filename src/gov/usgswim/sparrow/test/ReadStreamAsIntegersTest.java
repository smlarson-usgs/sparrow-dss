package gov.usgswim.sparrow.test;


import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.util.JDBCUtil;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

public class ReadStreamAsIntegersTest extends ReadStreamAsDoubleTest {

	public void testBasic() throws Exception {
		InputStream fileStream = this
				.getClass()
				.getResourceAsStream(
						"/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");

		DataTableWritable dt = TabDelimFileUtil.readAsInteger(fileStream, true, JDBCUtil.DO_NOT_INDEX);
		runIDTest(dt);
	}

}
