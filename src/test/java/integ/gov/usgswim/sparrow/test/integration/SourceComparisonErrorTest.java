package gov.usgswim.sparrow.test.integration;

import static gov.usgswim.sparrow.test.TestHelper.getAttributeValue;
import static gov.usgswim.sparrow.test.TestHelper.getElementValue;
import static gov.usgswim.sparrow.test.TestHelper.pipeDispatch;
import static gov.usgswim.sparrow.test.TestHelper.readToString;
import static gov.usgswim.sparrow.test.TestHelper.setElementValue;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.cachefactory.AnalysisResultFactory;
import gov.usgswim.sparrow.cachefactory.ComparisonResultFactory;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.PredictionContextTest;
import gov.usgswim.sparrow.service.binning.BinningPipeline;
import gov.usgswim.sparrow.service.binning.BinningRequest;
import gov.usgswim.sparrow.service.idbypoint.IDByPointPipeline;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.TestHelper;
import org.custommonkey.xmlunit.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * This test was created to recreate an error that seems to occur when a
 * comparison is requested for a source value.
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class SourceComparisonErrorTest extends XMLTestCase {
	
	LifecycleListener lifecycle = new LifecycleListener();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null, true);
		
		XMLUnit.setIgnoreWhitespace(true);
		
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		lifecycle.contextDestroyed(null, true);
	}
	
	public void testComparison() throws Exception {
		String xmlContextReq = TestHelper.getXmlAsString(this.getClass(), "context");
		String xmlContextResp = TestHelper.getXmlAsString(this.getClass(), "contextResp");
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualContextResponse = TestHelper.pipeDispatch(contextReq, pipe);

		
		assertXMLEqual(xmlContextResp, actualContextResponse);
		
		
		///Try to build bins from a GET request that looks like this:
		//context/
		//getBins?_dc=1259617459336&context-id=-1930836194&bin-count=5&bin-type=EQUAL_RANGE
		BinningRequest binReq = new BinningRequest(new Integer(714573086), 2, BIN_TYPE.EQUAL_RANGE);
		BinningPipeline binPipe = new BinningPipeline();
		String actualBinResponse = TestHelper.pipeDispatch(binReq, binPipe);
		String xmlBinResp = TestHelper.getXmlAsString(this.getClass(), "binResp");
		assertXMLEqual(xmlBinResp, actualBinResponse);

	}
	
}

