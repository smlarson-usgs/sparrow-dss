package gov.usgswim.sparrow.test;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

public class ContextToPredictionTest extends TestCase {

	LifecycleListener lifecycle = new LifecycleListener();
	
	//Unchanging context-id for the predictionContext described in the xml file
	//loaded in buildRequest().
	public static final int CONTEXT_ID = -43431453;
	
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		lifecycle.contextDestroyed(null);
	}
	
	public void testBasicPredictionValues() throws Exception {

		PredictContextRequest contextReq = buildRequest();	//Build a context from a canned file
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline pipe = new PredictContextPipeline();
		pipe.dispatch(contextReq, out);
		

		System.out.println("***");
		System.out.println("Response: " + out.toString());
		System.out.println("PredictContextID: " + contextReq.getPredictionContext().hashCode());
		System.out.println("***");
		
		//Confirm that the response xml doc contains the correct context-id,
		//which is and must be repeatable, so as long as the request doesn't change, this number is fixed.
		assertTrue(out.toString().contains( Integer.toString(CONTEXT_ID) ));
		
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
		//			<adjustment src="2" coef=".9"/>
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
		int colForSrc2 = predictData.getSourceColumnForSourceID(2);
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
	
	public void testHashCode() throws Exception {
		PredictionContext context1 = buildRequest().getPredictionContext();
		PredictionContext context2 = buildRequest().getPredictionContext();
		
		assertEquals(context1.hashCode(), context2.hashCode());
		assertEquals(context1.getId(), context2.getId());
		assertEquals(context1.getId().intValue(), context2.hashCode());
	}
	
	
	public PredictContextRequest buildRequest() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-1.xml");
		String xml = readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	
	public String readToString(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		StringBuffer sb = new StringBuffer();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception ex) {
			ex.getMessage();
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}


}

