package gov.usgswim.sparrow.test.integration;

import static gov.usgswim.sparrow.test.TestHelper.pipeDispatch;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.MapViewerSparrowDataProvider;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.TestHelper;
import gov.usgswim.sparrow.test.parsers.PredictionContextTest;
import junit.framework.TestCase;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

public class ContextToPredictionTest extends TestCase {

	LifecycleListener lifecycle = new LifecycleListener();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null, true);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		lifecycle.contextDestroyed(null, true);
	}
	
	// ============
	// TEST METHODS
	// ============
	public void testBasicPredictionValues() throws Exception {

		int CONTEXT_ID = PredictionContextTest.PRED_CONTEXT_1_ID;
		
		PredictContextRequest contextReq = PredictionContextTest.buildPredictContext1();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());


		//Confirm that the response xml doc contains the correct context-id,
		//which is and must be repeatable, so as long as the request doesn't change, this number is fixed.
		assertTrue(response.contains( Integer.toString(CONTEXT_ID) ));
		
		//
		//The PredictionContext is now in the cache and can be accessed by its id.
		//Now we request a prediction from this context, using the ID...
		//
		
		
		//Get the prediction context from the cache
		PredictionContext contextFromCache = SharedApplication.getInstance().getPredictionContext(CONTEXT_ID);
		
		//Get the prediction result from cache (this forces it to be calculated, see PredictResultFactory)
		PredictResult predictResult = SharedApplication.getInstance().getPredictResult(contextFromCache);
		
		//For comparison, get the prediction data (original model data) from the cache (cached by PredictResultFactory)
		PredictData predictData = SharedApplication.getInstance().getPredictData(contextFromCache.getModelID());
		
		//Also for comparison, get the nominal predicted values
		PredictionContext nomContext = new PredictionContext(contextFromCache.getModelID(), null, null, null, null);
		PredictResult nomResult = SharedApplication.getInstance().getPredictResult(nomContext);
		
		assertEquals(new Long(1L), contextFromCache.getModelID());
		assertNotNull(predictData);
		assertNotNull(predictResult);
		
		//
		// Now test some of the adjusted values.  Below are the adjustments that
		// were actually made.  Other types of adjustments are in the xml file,
		// but these are the only ones that are implemented:
		//
		// <reach-group enabled="true" name="Wisconsin">
		//		<adjustment src="2" coef=".75"/>  <--- Applies to all (both) reaches in this group
		//		
		//		<reach id="3074">  <------------------ This is the 1st reach in the dataset (reach 0)
		//			<adjustment src="2" abs=".9"/>
		//		</reach>
		//		<reach id="3077">	<------------------- This is the 2nd reach in the dataset (reach 1)
		//			<adjustment src="2" abs="91344"/>
		//		</reach>
		//	</reach-group>
		
		//Get the Original, unadjusted source data
		DataTable orgSrc = predictData.getSrc();
		
		//Get the user adjusted source data, which is also cached.  The adjusted values are cached w/in
		//PredictResultFactory by another cache call, which is handled by AdjustedSourceFactory
		DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(contextReq.getPredictionContext().getAdjustmentGroups());
		
		//Determine the column for source '2'
		int colForSrc2 = predictData.getSourceIndexForSourceID(2);
		int rowForReach3074 = predictData.getRowForReachID(3074);
		int rowForReach3077 = predictData.getRowForReachID(3077);
		
		//The first reach has a cumulative adjustment of the group coef and the reach coef.
		assertEquals(
				new Double(orgSrc.getDouble(rowForReach3074, colForSrc2).doubleValue() * .75d * .9d),
				adjSrc.getDouble(0, colForSrc2),
				.0000001d);
		
		//The 2nd reach has an absolute value adjust, so the group coef is ignored.
		assertEquals(
				new Double(91344d),
				adjSrc.getDouble(rowForReach3077, colForSrc2),
				.0000001d);
		
		//Only 2 sources has been adjusted on 2 reaches.  As a quick test, compare the
		//incremental predicted values (nominal vs adjusted) of some of those sources.
		//Only the adjusted source (source 2) should be affected.
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3074, 1L), nomResult.getIncrementalForSrc(rowForReach3074, 1L));
		assertTrue(predictResult.getIncrementalForSrc(rowForReach3074, 2L) != nomResult.getIncrementalForSrc(rowForReach3074, 2L));
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3074, 3L), nomResult.getIncrementalForSrc(rowForReach3074, 3L));
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3074, 4L), nomResult.getIncrementalForSrc(rowForReach3074, 4L));
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3074, 5L), nomResult.getIncrementalForSrc(rowForReach3074, 5L));
		
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3077, 1L), nomResult.getIncrementalForSrc(rowForReach3077, 1L));
		assertTrue(predictResult.getIncrementalForSrc(rowForReach3077, 2L) != nomResult.getIncrementalForSrc(rowForReach3077, 2L));
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3077, 3L), nomResult.getIncrementalForSrc(rowForReach3077, 3L));
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3077, 4L), nomResult.getIncrementalForSrc(rowForReach3077, 4L));
		assertEquals(predictResult.getIncrementalForSrc(rowForReach3077, 5L), nomResult.getIncrementalForSrc(rowForReach3077, 5L));
		
		////////
		// These tests are added to check that the column indexes and values returned
		// from the new SPARROW specific access methods in PredictResultImm return
		// expected values.
		assertEquals(11, predictResult.getSourceCount());
		assertEquals(0, predictResult.getIncrementalColForSrc(1L));
		assertEquals(10, predictResult.getIncrementalColForSrc(11L));
		assertEquals(11, predictResult.getTotalColForSrc(1L));
		assertEquals(21, predictResult.getTotalColForSrc(11L));
		assertEquals(22, predictResult.getIncrementalCol());
		assertEquals(23, predictResult.getTotalCol());
		
		//Check some actual values for the reach w/ id 3074
		assertEquals(predictResult.getDouble(rowForReach3074, 0), predictResult.getIncrementalForSrc(rowForReach3074, 1L));
		assertEquals(predictResult.getDouble(rowForReach3074, 10), predictResult.getIncrementalForSrc(rowForReach3074, 11L));
		assertEquals(predictResult.getDouble(rowForReach3074, 11), predictResult.getTotalForSrc(rowForReach3074, 1L));
		assertEquals(predictResult.getDouble(rowForReach3074, 21), predictResult.getTotalForSrc(rowForReach3074, 11L));
		assertEquals(predictResult.getDouble(rowForReach3074, 22), predictResult.getIncremental(rowForReach3074));
		assertEquals(predictResult.getDouble(rowForReach3074, 23), predictResult.getTotal(rowForReach3074));
	}
	
	/**
	 * This test is intended to reproduce an issue where the dataseries 'source_value'
	 * seems to show no changes when the nominal comparison 'percent' is used.
	 */
	public void testSourceValuesChange() throws Exception {

		int CONTEXT_ID = PredictionContextTest.PRED_CONTEXT_3_ID;
		PredictContextRequest contextReq = PredictionContextTest.buildPredictContext3();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		
		//Confirm that the response xml doc contains the correct context-id,
		//which is and must be repeatable, so as long as the request doesn't change, this number is fixed.
		assertTrue(response.contains( Integer.toString(CONTEXT_ID) ));
		
		//Get the prediction context from the cache
		PredictionContext contextFromCache = SharedApplication.getInstance().getPredictionContext(CONTEXT_ID);
		

		MapViewerSparrowDataProvider nsProvider = new MapViewerSparrowDataProvider();
		
		NSDataSet data = nsProvider.copyToNSDataSet(contextFromCache);
		
		int zeros = 0;
		int nonZeros = 0;
		
		while (data.next()) {
			NSRow row = data.getRow();
			double v = row.get(1).getDouble();
			if (v == 0d) {
				zeros++;
			} else {
				nonZeros++;
				System.out.println("Non Zero value: " + v);
			}
		}
		
		assertEquals(2, nonZeros);
	
	}
	
	public void testHashCode() throws Exception {
		PredictionContext context1 = PredictionContextTest.buildPredictContext1().getPredictionContext();
		PredictionContext context2 = PredictionContextTest.buildPredictContext1().getPredictionContext();
		TestHelper.testHashCode(context1, context2, context1.clone());

		// test IDs
		assertEquals(context1.hashCode(), context1.getId().intValue());
		assertEquals(context2.hashCode(), context2.getId().intValue());
	}


}

