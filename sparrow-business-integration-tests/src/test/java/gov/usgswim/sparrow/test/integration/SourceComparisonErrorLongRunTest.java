package gov.usgswim.sparrow.test.integration;

import gov.usgs.cida.binning.domain.BinSet;
import gov.usgs.cida.binning.domain.BinType;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.ServletResponseParser;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import org.apache.log4j.Level;
import org.junit.Test;

/**
 * This test was created to recreate an error that seems to occur when a
 * comparison is requested for a source value.
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class SourceComparisonErrorLongRunTest extends SparrowTestBaseWithDBandCannedModel50 {
	
	@Override
	public void doBeforeClassSingleInstanceSetup() throws Exception {
		//Uncomment to debug
		setLogLevel(Level.DEBUG);
	}
	
	@Test
	public void testComparison() throws Exception {
		String xmlContextReq = SparrowTestBase.getXmlAsString(this.getClass(), "context");
		String xmlContextResp = SparrowTestBase.getXmlAsString(this.getClass(), "contextResp");
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualContextResponse = SparrowTestBase.pipeDispatch(contextReq, pipe);

		log.debug("actual: " + actualContextResponse);
		similarXMLIgnoreContextId(xmlContextResp, actualContextResponse);
		
		String contextId = getXPathValue("//@context-id", actualContextResponse);
		
		
		///Try to build bins from a GET request that looks like this:
		//context/
		//getBins?_dc=1259617459336&context-id=-1930836194&bin-count=5&bin-operation=EQUAL_RANGE
		BinningRequest binReq = new BinningRequest(new Integer(contextId), 2, BinType.EQUAL_RANGE);
		BinSet binSet = SharedApplication.getInstance().getDataBinning(binReq);
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(binSet, null, ServiceResponseStatus.OK,
				ServiceResponseOperation.CALCULATE);
		
		String actualXml = SharedApplication.getInstance().getXmlXStream().toXML(wrap);
		log.debug("actual binning: " + actualXml);
		
		String xmlBinResp = SparrowTestBase.getXmlAsString(this.getClass(), "binResp");
		similarXMLIgnoreContextId(xmlBinResp, actualXml);

	}
	
}

