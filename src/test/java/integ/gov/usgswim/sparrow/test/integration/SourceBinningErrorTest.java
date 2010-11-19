package gov.usgswim.sparrow.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.cachefactory.BinningRequest;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.binning.BinningPipeline;
import gov.usgswim.sparrow.service.binning.BinningServiceRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.math.BigDecimal;

import org.apache.log4j.Level;
import org.junit.Test;

/**
 * This test was created to recreate an error that seems to occur when a
 * comparison is requested for a source value.
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class SourceBinningErrorTest extends SparrowUnitTest {
	
	@Test
	public void testComparison() throws Exception {
		String xmlContextReq = SparrowUnitTest.getXmlAsString(this.getClass(), "context");
		String xmlContextResp = SparrowUnitTest.getXmlAsString(this.getClass(), "contextResp");
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualContextResponse = SparrowUnitTest.pipeDispatch(contextReq, pipe);
		
		assertTrue(similarXMLIgnoreContextId(xmlContextResp, actualContextResponse));
		Integer contextId = getContextIdFromContext(actualContextResponse);

		
		
		///Try to build bins from a GET request that looks like this:
		//context/
		//getBins?_dc=1259617459336&context-id=-1930836194&bin-count=5&bin-operation=EQUAL_RANGE
		
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable src = pd.getSrc();
		
		//Get min and max values for source 1
		double minVal = src.getMinDouble(0);
		double maxVal = src.getMaxDouble(0);
		double middleVal = (minVal + maxVal) / 2d;
		
		BinningRequest binReq = new BinningRequest(new Integer(contextId), 2, BIN_TYPE.EQUAL_RANGE);
		BigDecimal[] bins = SharedApplication.getInstance().getDataBinning(binReq);
		
		assertEquals(minVal, bins[0].doubleValue(), minVal * .02d);
		assertEquals(middleVal, bins[1].doubleValue(), middleVal * .02d);
		assertEquals(maxVal, bins[2].doubleValue(), maxVal * .02d);
		
		long startTime = System.currentTimeMillis();
		BinningServiceRequest binSvsReq = new BinningServiceRequest(new Integer(contextId), 2, BIN_TYPE.EQUAL_RANGE);
		BinningPipeline binPipe = new BinningPipeline();
		String actualBinResponse = SparrowUnitTest.pipeDispatch(binSvsReq, binPipe);
		long endTime = System.currentTimeMillis();
		assertTrue("These results should be in cache, thus almost instant response", endTime - startTime < 500L);
		assertTrue(actualBinResponse.contains("<bin>" + bins[0].toString() + "</bin>"));
		assertTrue(actualBinResponse.contains("<bin>" + bins[1].toString() + "</bin>"));
		assertTrue(actualBinResponse.contains("<bin>" + bins[2].toString() + "</bin>"));


	}
	
}

