package gov.usgswim.sparrow.test.basic;


import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgswim.sparrow.util.DLUtils;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ReadStreamAsIntegersTest extends ReadStreamAsDoubleTest {

	@Override
	public void testBasic() throws Exception {
		
		
		BufferedReader fileStream = new BufferedReader(new InputStreamReader(
				this.getClass().getResourceAsStream(
					"/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt"
				)
		));

		DataTableWritable dt = TabDelimFileUtil.readAsInteger(fileStream, true, DLUtils.DO_NOT_INDEX);
		runIDTest(dt);
	}

}
