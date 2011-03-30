package gov.usgswim.sparrow.test.integration;

import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.request.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.service.binning.BinningPipeline;
import gov.usgswim.sparrow.service.binning.BinningServiceRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import org.junit.Test;

/**
 * This test was created to recreate an error that seems to occur when a
 * comparison is requested for a source value.
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class SourceComparisonErrorLongRunTest extends SparrowTestBaseWithDBandCannedModel50 {
	
	@Test
	public void testComparison() throws Exception {
		String xmlContextReq = SparrowTestBase.getXmlAsString(this.getClass(), "context");
		String xmlContextResp = SparrowTestBase.getXmlAsString(this.getClass(), "contextResp");
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualContextResponse = SparrowTestBase.pipeDispatch(contextReq, pipe);

		System.out.println("actual: " + actualContextResponse);
		similarXMLIgnoreContextId(xmlContextResp, actualContextResponse);
		
		String contextId = getXPathValue("//@context-id", actualContextResponse);
		
		
		///Try to build bins from a GET request that looks like this:
		//context/
		//getBins?_dc=1259617459336&context-id=-1930836194&bin-count=5&bin-operation=EQUAL_RANGE
		BinningServiceRequest binReq = new BinningServiceRequest(new Integer(contextId), 2, BIN_TYPE.EQUAL_RANGE);
		BinningPipeline binPipe = new BinningPipeline();
		String actualBinResponse = SparrowTestBase.pipeDispatch(binReq, binPipe);
		String xmlBinResp = SparrowTestBase.getXmlAsString(this.getClass(), "binResp");
		similarXMLIgnoreContextId(xmlBinResp, actualBinResponse);

	}
	
}

