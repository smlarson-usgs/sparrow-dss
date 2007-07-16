package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Int2D;
import gov.usgswim.sparrow.service.IDByPointComputable;
import gov.usgswim.sparrow.service.IDByPointRequest;
import gov.usgswim.sparrow.service.ModelService;
import gov.usgswim.sparrow.service.ServiceHandler;

import gov.usgswim.sparrow.util.SparrowUtil;

import java.awt.Point;

import java.io.File;
import java.io.FileOutputStream;

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
		IDByPointRequest req = new IDByPointRequest(22L, new Point.Double(-93d, 45d), 5);
		Int2D data = idc.compute(req);
		
		SparrowUtil.print2DArray(data, "Nearest to point:");
		
		assertEquals(5, data.getRowCount());
		
	}
	
}
