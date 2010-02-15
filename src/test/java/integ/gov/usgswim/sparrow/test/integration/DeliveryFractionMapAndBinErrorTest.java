package gov.usgswim.sparrow.test.integration;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.MapViewerSparrowDataProvider;
import gov.usgswim.sparrow.TestHelper;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.service.binning.BinningPipeline;
import gov.usgswim.sparrow.service.binning.BinningServiceRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.util.Hashtable;

import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class DeliveryFractionMapAndBinErrorTest {
	protected static Logger log =
		Logger.getLogger(DeliveryFractionMapAndBinErrorTest.class); //logging for this class
	
	static LifecycleListener lifecycle = new LifecycleListener();
	
	static final Long MODEL_ID = 50L;
	
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		//Turns on detailed logging
		log.setLevel(Level.DEBUG);
		log.getLogger(Action.class).setLevel(Level.DEBUG);
		
		lifecycle.contextInitialized(null, true);
		XMLUnit.setIgnoreWhitespace(true);
		
	}

	@AfterClass
	public static void tearDown() throws Exception {
		lifecycle.contextDestroyed(null, true);
	}
	
	@Test
	public void testFracToNSDataSet() throws Exception {
		
		final Integer context_id = 515774381;	//ID of the context we load
		
		//Register context from canned file
		String xmlContextReq = TestHelper.getXmlAsString(this.getClass(), "context");
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlContextReq);
		String actualResponse = TestHelper.pipeDispatch(contextReq, pipe);
		System.out.println(actualResponse);
		assertTrue(actualResponse.indexOf(context_id.toString()) > -1);
		
		//Run a binning request on that same context ID
		BinningPipeline binPipe = new BinningPipeline();
		BinningServiceRequest binSvsReq = new BinningServiceRequest(context_id, 5, BIN_TYPE.EQUAL_COUNT);
		actualResponse = TestHelper.pipeDispatch(binSvsReq, binPipe);
		System.out.println("bin response: " + actualResponse);
		assertTrue(actualResponse.indexOf("<bin>0</bin>") > -1);

		
		//Build the NSDataset from the data
		Hashtable props = new Hashtable();
		props.put(MapViewerSparrowDataProvider.CONTEXT_ID, context_id.toString());
		MapViewerSparrowDataProvider nsProvider = new MapViewerSparrowDataProvider();
		
		NSDataSet nsData = nsProvider.buildDataSet(props);
		assertEquals(8321, nsData.size());

//		BinningRequest binReq = new BinningRequest(context_id, 5, BIN_TYPE.EQUAL_COUNT);
//		BigDecimal[] bins = SharedApplication.getInstance().getDataBinning(binReq);
//		

//		System.out.println("bin 1: " + bins[0]);
//		System.out.println("bin 2: " + bins[1]);
//		System.out.println("bin 3: " + bins[2]);
	}
	
}

