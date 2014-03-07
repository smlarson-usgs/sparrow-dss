package gov.usgswim.sparrow.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.binning.domain.BinSet;
import gov.usgs.cida.binning.domain.BinType;
import gov.usgswim.sparrow.MapViewerSparrowDataProvider;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.cachefactory.BinningFactory;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgswim.sparrow.service.ServletResponseParser;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.util.Hashtable;

import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.log4j.Level;
import org.junit.Test;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 */
public class DeliveryFractionMapAndBinErrorLongRunTest extends SparrowTestBaseWithDBandCannedModel50 {
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		setLogLevel(Level.DEBUG);
	}
	
	@Test
	public void testFracToNSDataSet() throws Exception {
		
		//Register context from canned file
		String xmlContextReq = SparrowTestBase.getXmlAsString(this.getClass(), "context");
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualResponse = SparrowTestBase.pipeDispatch(contextReq, pipe);
		
		Integer contextID = getContextIdFromContext(actualResponse);
		//System.out.println(actualResponse);
		//assertTrue(actualResponse.indexOf(context_id.toString()) > -1);
		
		//Run a binning request on that same context ID
		BinningRequest binReq = new BinningRequest(contextID, 5, BinType.EQUAL_COUNT);
		BinSet binSet = SharedApplication.getInstance().getDataBinning(binReq);
		String xml = ServletResponseParser.getXMLXStream().toXML(binSet);

		log.debug("bin response: " + xml);

		
		//Build the NSDataset from the data
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(MapViewerSparrowDataProvider.CONTEXT_ID, contextID.toString());
		MapViewerSparrowDataProvider nsProvider = new MapViewerSparrowDataProvider();
		
		NSDataSet nsData = nsProvider.buildDataSet(props);
		assertEquals(8321, nsData.size());

	}
	
}

