package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.cachefactory.BinningFactory;
import gov.usgswim.sparrow.cachefactory.BinningRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

public class BinTest extends TestCase {

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
	
	public void testBasic() throws Exception {

		PredictContextRequest contextReq = buildPredictContextEmpty();	//Build a context from a canned file
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline pipe = new PredictContextPipeline();
		pipe.dispatch(contextReq, out);
		
		
		Integer contextId = contextReq.getPredictionContext().getId();
		
		BinningFactory fact = new BinningFactory();
		
		for (int binNumber=0; binNumber<12; binNumber++) {
			float[] bins = (float[]) fact.createEntry(new BinningRequest(contextId, 4, binNumber));
			
			System.out.println("- - - -");
			System.out.println("Bins for column :" + binNumber);
			for (float bin: bins) {
				System.out.println("bin :" + bin);
			}
			System.out.println("- - - -");
		}
	}
	
	
	public PredictContextRequest buildPredictContextEmpty() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-empty.xml");
		String xml = TestHelper.readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}


}

