package gov.usgswim.sparrow.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.MapViewerSparrowDataProvider;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.service.binning.BinningPipeline;
import gov.usgswim.sparrow.service.binning.BinningServiceRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.util.Hashtable;

import oracle.mapviewer.share.ext.NSDataSet;

import org.junit.Test;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class DeliveryFractionMapAndBinErrorTest extends SparrowDBTest{
	
	@Test
	public void testFracToNSDataSet() throws Exception {
		
		//Register context from canned file
		String xmlContextReq = SparrowUnitTest.getXmlAsString(this.getClass(), "context");
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualResponse = SparrowUnitTest.pipeDispatch(contextReq, pipe);
		
		Integer contextID = getContextIdFromContext(actualResponse);
		//System.out.println(actualResponse);
		//assertTrue(actualResponse.indexOf(context_id.toString()) > -1);
		
		//Run a binning request on that same context ID
		BinningPipeline binPipe = new BinningPipeline();
		BinningServiceRequest binSvsReq = new BinningServiceRequest(contextID, 5, BIN_TYPE.EQUAL_COUNT);
		actualResponse = SparrowUnitTest.pipeDispatch(binSvsReq, binPipe);
		System.out.println("bin response: " + actualResponse);
		assertTrue(actualResponse.indexOf("<bin>0</bin>") > -1);

		
		//Build the NSDataset from the data
		Hashtable<String, String> props = new Hashtable<String, String>(2);
		props.put(MapViewerSparrowDataProvider.CONTEXT_ID, contextID.toString());
		MapViewerSparrowDataProvider nsProvider = new MapViewerSparrowDataProvider();
		
		NSDataSet nsData = nsProvider.buildDataSet(props);
		assertEquals(8321, nsData.size());

	}
	
}

