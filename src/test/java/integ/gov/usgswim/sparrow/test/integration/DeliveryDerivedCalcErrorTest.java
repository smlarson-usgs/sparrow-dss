package gov.usgswim.sparrow.test.integration;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.service.binning.BinningPipeline;
import gov.usgswim.sparrow.service.binning.BinningServiceRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class DeliveryDerivedCalcErrorTest extends SparrowDBTest {
	
	
	@Test
	public void testComparison() throws Exception {
		
		
		//Test total context (total flux)
		String xmlContextReq = SparrowUnitTest.getXmlAsString(this.getClass(), "TotalContext");
		String xmlContextResp = SparrowUnitTest.getXmlAsString(this.getClass(), "TotalContextResp");
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualResponse = SparrowUnitTest.pipeDispatch(contextReq, pipe);
		System.out.println("actual: " + actualResponse);
		XMLAssert.assertXMLEqual(xmlContextResp, actualResponse);
		DataColumn totalData = contextReq.getPredictionContext().getDataColumn();
		
		//Test delivery fraction
		xmlContextReq = SparrowUnitTest.getXmlAsString(this.getClass(), "DelFracContext");
		xmlContextResp = SparrowUnitTest.getXmlAsString(this.getClass(), "DelFracContextResp");
		contextReq = pipe.parse(xmlContextReq);
		actualResponse = SparrowUnitTest.pipeDispatch(contextReq, pipe);
		XMLAssert.assertXMLEqual(xmlContextResp, actualResponse);
		DataColumn delFracData = contextReq.getPredictionContext().getDataColumn();

		//Test Total Delivery Flux
		xmlContextReq = SparrowUnitTest.getXmlAsString(this.getClass(), "DelTotalContext");
		xmlContextResp = SparrowUnitTest.getXmlAsString(this.getClass(), "DelTotalContextResp");
		contextReq = pipe.parse(xmlContextReq);
		actualResponse = SparrowUnitTest.pipeDispatch(contextReq, pipe);
		XMLAssert.assertXMLEqual(xmlContextResp, actualResponse);
		DataColumn delTotalData = contextReq.getPredictionContext().getDataColumn();
		
		DataTable totalTable = totalData.getTable();
		int totalCol = totalData.getColumn();
		DataTable delFracTable = delFracData.getTable();
		int delFracCol = delFracData.getColumn();
		DataTable delTotalTable = delTotalData.getTable();
		int delTotalCol = delTotalData.getColumn();
		
		//The rows affected by the target
		int row1 = delFracTable.getRowForId(5552L);
		int row2 = delFracTable.getRowForId(5553L);
		int row3 = delFracTable.getRowForId(5554L);
		int row4 = delFracTable.getRowForId(5555L);
		int row5 = delFracTable.getRowForId(5556L);
		
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("total 1" + totalTable.getDouble(row1, totalCol));
		System.out.println("total 2" + totalTable.getDouble(row2, totalCol));
		System.out.println("total 3" + totalTable.getDouble(row3, totalCol));
		System.out.println("total 4" + totalTable.getDouble(row4, totalCol));
		System.out.println("total 5" + totalTable.getDouble(row5, totalCol));
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("del frac 1" + delFracTable.getDouble(row1, delFracCol));
		System.out.println("del frac 1" + delFracTable.getDouble(row2, delFracCol));
		System.out.println("del frac 1" + delFracTable.getDouble(row3, delFracCol));
		System.out.println("del frac 1" + delFracTable.getDouble(row4, delFracCol));
		System.out.println("del frac 1" + delFracTable.getDouble(row5, delFracCol));
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("del total 1" + delTotalTable.getDouble(row1, delTotalCol));
		System.out.println("del total 2" + delTotalTable.getDouble(row2, delTotalCol));
		System.out.println("del total 3" + delTotalTable.getDouble(row3, delTotalCol));
		System.out.println("del total 4" + delTotalTable.getDouble(row4, delTotalCol));
		System.out.println("del total 5" + delTotalTable.getDouble(row5, delTotalCol));
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		
		//All rows other then the 5 listed should have a del frac of zero
		for (int i=0; i<row1; i++) {
			assertEquals(0d, delFracTable.getDouble(i, delFracCol), .000000001d);
		}
		
		for (int i=(row5+1); i<delFracTable.getRowCount(); i++) {
			assertEquals(0d, delFracTable.getDouble(i, delFracCol), .000000001d);
		}
		
		assertTrue(
				totalTable.getDouble(row1, totalCol) *
				delFracTable.getDouble(row1, delFracCol) ==
				delTotalTable.getDouble(row1, delTotalCol));
		//assertFalse(totalTable.getDouble(row1, totalCol).equals(delTotalTable.getDouble(row1, delTotalCol)));

	}
	
}

