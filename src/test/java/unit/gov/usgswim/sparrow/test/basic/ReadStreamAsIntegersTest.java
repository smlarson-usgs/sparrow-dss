package gov.usgswim.sparrow.test.basic;


import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

public class ReadStreamAsIntegersTest extends ReadStreamAsDoubleTest {

	@Override
	public void testBasic() throws Exception {
		InputStream fileStream = this
				.getClass()
				.getResourceAsStream(
						"/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");

		DataTableWritable dt = TabDelimFileUtil.readAsInteger(fileStream, true, DataLoader.DO_NOT_INDEX);
		runIDTest(dt);
	}

}
