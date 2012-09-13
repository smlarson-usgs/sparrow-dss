package gov.usgswim.sparrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.util.Arrays;

import org.junit.Test;

/**
 * This test is a high-level unit test that uses the PredictContextPipeline and
 * XML submissions to do a fast check of delivery calculation based on the 
 * canned predict data.
 * 
 * Reach 5552 (model 50) is used as the target for the test, which results in
 * only 5 reaches affected by the test so it is very easy to verify.
 * 
 * 
 * @author eeverman
 */
public class DeliveryDerivedCalcErrorTest extends SparrowTestBase {
	
	
	@Test
	public void testComparison() throws Exception {
		
		
		//Test total context (total flux)
		String xmlContextReq = SparrowTestBase.getXmlAsString(this.getClass(), "TotalContext");
		String xmlContextResp = SparrowTestBase.getXmlAsString(this.getClass(), "TotalContextResp");
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualResponse = SparrowTestBase.pipeDispatch(contextReq, pipe);
		assertTrue(similarXMLIgnoreContextId(xmlContextResp, actualResponse));
		SparrowColumnSpecifier totalData = contextReq.getPredictionContext().getDataColumn();
		
		//Test delivery fraction
		xmlContextReq = SparrowTestBase.getXmlAsString(this.getClass(), "DelFracContext");
		xmlContextResp = SparrowTestBase.getXmlAsString(this.getClass(), "DelFracContextResp");
		contextReq = pipe.parse(xmlContextReq);
		actualResponse = SparrowTestBase.pipeDispatch(contextReq, pipe);
		assertTrue(similarXMLIgnoreContextId(xmlContextResp, actualResponse));
		SparrowColumnSpecifier delFracData = contextReq.getPredictionContext().getDataColumn();

		//Test Total Delivery Flux
		xmlContextReq = SparrowTestBase.getXmlAsString(this.getClass(), "DelTotalContext");
		xmlContextResp = SparrowTestBase.getXmlAsString(this.getClass(), "DelTotalContextResp");
		contextReq = pipe.parse(xmlContextReq);
		actualResponse = SparrowTestBase.pipeDispatch(contextReq, pipe);
		//System.out.println("actual: " + actualResponse);
		assertTrue(similarXMLIgnoreContextId(xmlContextResp, actualResponse));
		SparrowColumnSpecifier delTotalData = contextReq.getPredictionContext().getDataColumn();
		
		//The rows affected by the target
		int row1 = delFracData.getTable().getRowForId(5552L);
		int row2 = delFracData.getTable().getRowForId(5553L);
		int row3 = delFracData.getTable().getRowForId(5554L);
		int row4 = delFracData.getTable().getRowForId(5555L);
		int row5 = delFracData.getTable().getRowForId(5556L);
		
		int[] affectedRows = new int[] {row1, row2, row3, row4, row5};
		Arrays.sort(affectedRows);
		
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("total 1: " + totalData.getDouble(row1));
		System.out.println("total 2: " + totalData.getDouble(row2));
		System.out.println("total 3: " + totalData.getDouble(row3));
		System.out.println("total 4: " + totalData.getDouble(row4));
		System.out.println("total 5: " + totalData.getDouble(row5));
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("del frac 1: " + delFracData.getDouble(row1));
		System.out.println("del frac 1: " + delFracData.getDouble(row2));
		System.out.println("del frac 1: " + delFracData.getDouble(row3));
		System.out.println("del frac 1: " + delFracData.getDouble(row4));
		System.out.println("del frac 1: " + delFracData.getDouble(row5));
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("del total 1: " + delTotalData.getDouble(row1));
		System.out.println("del total 2: " + delTotalData.getDouble(row2));
		System.out.println("del total 3: " + delTotalData.getDouble(row3));
		System.out.println("del total 4: " + delTotalData.getDouble(row4));
		System.out.println("del total 5: " + delTotalData.getDouble(row5));
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
		
		//All rows other then the 5 listed should have a del frac of zero
		for (int i=0; i<delFracData.getRowCount(); i++) {
			if (Arrays.binarySearch(affectedRows, i) < 0) {
				//Not in the list of affect reaches, so should be zero
				assertEquals(0d, delFracData.getDouble(i), .000000001d);
				assertEquals(0d, delTotalData.getDouble(i), .000000001d);
			} else {
				//In the list of affected reaches, so should be greater than 1
				assertTrue(delFracData.getDouble(i) > 0);
				assertTrue(delTotalData.getDouble(i) > 0);
			}
		}
		

		//Check the affected rows for the basic calc
		for (int i = 0; i < affectedRows.length; i++) {
			assertEquals(
					totalData.getDouble(i) * delFracData.getDouble(i),
					delTotalData.getDouble(i),
					.000000001d
				);
		}

	}
	
}

