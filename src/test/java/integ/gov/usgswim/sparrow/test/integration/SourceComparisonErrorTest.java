package gov.usgswim.sparrow.test.integration;

import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.service.binning.BinningPipeline;
import gov.usgswim.sparrow.service.binning.BinningRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.TestHelper;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.*;

/**
 * This test was created to recreate an error that seems to occur when a
 * comparison is requested for a source value.
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class SourceComparisonErrorTest {
	
	static LifecycleListener lifecycle = new LifecycleListener();
	
	@BeforeClass
	public static void setUp() throws Exception {
		lifecycle.contextInitialized(null, true);
		
		XMLUnit.setIgnoreWhitespace(true);
		
	}

	@AfterClass
	public static void tearDown() throws Exception {
		lifecycle.contextDestroyed(null, true);
	}
	
	@Test
	public void testComparison() throws Exception {
		String xmlContextReq = TestHelper.getXmlAsString(this.getClass(), "context");
		String xmlContextResp = TestHelper.getXmlAsString(this.getClass(), "contextResp");
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualContextResponse = TestHelper.pipeDispatch(contextReq, pipe);

		
		XMLAssert.assertXMLEqual(xmlContextResp, actualContextResponse);
		
		
		///Try to build bins from a GET request that looks like this:
		//context/
		//getBins?_dc=1259617459336&context-id=-1930836194&bin-count=5&bin-type=EQUAL_RANGE
		BinningRequest binReq = new BinningRequest(new Integer(714573086), 2, BIN_TYPE.EQUAL_RANGE);
		BinningPipeline binPipe = new BinningPipeline();
		String actualBinResponse = TestHelper.pipeDispatch(binReq, binPipe);
		String xmlBinResp = TestHelper.getXmlAsString(this.getClass(), "binResp");
		XMLAssert.assertXMLEqual(xmlBinResp, actualBinResponse);

	}
	
}

