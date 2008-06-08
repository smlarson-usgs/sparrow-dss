package gov.usgswim.sparrow.test;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.MapViewerSparrowDataProvider;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.cachefactory.BinningFactory;
import gov.usgswim.sparrow.cachefactory.BinningRequest;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.IDByPointPipeline;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

import junit.framework.TestCase;

public class BinTest extends TestCase {

	LifecycleListener lifecycle = new LifecycleListener();
	
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null, true);
	}

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

