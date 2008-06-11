package gov.usgswim.sparrow.test;

import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.deprecated.IDByPointComputable;
import gov.usgswim.sparrow.deprecated.IDByPointRequest_old;

import java.awt.Point;

import junit.framework.TestCase;


public class IDByPointComputableTest extends TestCase {
	//private Connection conn;

	public IDByPointComputableTest(String sTestName) {
		super(sTestName);
	}

	
	/**
	 * Not really much of a test - it just writes the document out to a temp file,
	 * but it does validate it.
	 * @throws Exception
	 */
	public void testBasic() throws Exception {
		
		IDByPointComputable idc = new IDByPointComputable();
		IDByPointRequest_old req = new IDByPointRequest_old(22L, new Point.Double(-93d, 45d), 5);
		DataTable data = idc.compute(req);
		
		TemporaryHelper.printDataTable(data, "Nearest to point:");
		
		assertEquals(5, data.getRowCount());
		
	}
	
}
