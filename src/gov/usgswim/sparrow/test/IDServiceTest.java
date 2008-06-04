package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.service.idbypoint.IDByPointPipeline;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

public class IDServiceTest extends TestCase {

	LifecycleListener lifecycle = new LifecycleListener();
	
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		lifecycle.contextDestroyed(null, true);
	}
	
	public void testModelByPoint() throws Exception {

		IDByPointRequest req = buildIDByPointRequest1();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IDByPointPipeline pipe = new IDByPointPipeline();
		pipe.dispatch(req, out);
		String response = out.toString();
		
		int reachID = Integer.parseInt( StringUtils.substringBetween(response, "<id>", "</id>") );
		String reachName = StringUtils.substringBetween(response, "<name>", "</name>");

		assertEquals(4428, reachID);
		assertEquals("POTOMAC R", reachName);
		
	}
	
	public void testModelByID() throws Exception {

		IDByPointRequest req = buildIDByPointRequest2();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IDByPointPipeline pipe = new IDByPointPipeline();
		pipe.dispatch(req, out);
		String response = out.toString();
		
		int reachID = Integer.parseInt( StringUtils.substringBetween(response, "<id>", "</id>") );
		String reachName = StringUtils.substringBetween(response, "<name>", "</name>");

		/*
		System.out.println("***");
		System.out.println("Response: " + out.toString());
		System.out.println("ReachID: " + reachID + " Reach Name: " + reachName);
		System.out.println("***");
		*/
		
		assertEquals(4428, reachID);
		assertEquals("POTOMAC R", reachName);
		
	}
	
	
	public void testContextByPoint() throws Exception {

		PredictContextRequest contextReq = buildPredictContext();	//Build a context from a canned file
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline PCPipe = new PredictContextPipeline();
		PCPipe.dispatch(contextReq, out);
		
		/*
		System.out.println("***");
		System.out.println("PredictContextID: " + contextReq.getPredictionContext().hashCode());
		System.out.println("***");
		*/
		
		IDByPointRequest req = buildIDByPointRequest3();
		
		out = new ByteArrayOutputStream();
		IDByPointPipeline IDpipe = new IDByPointPipeline();
		IDpipe.dispatch(req, out);
		String response = out.toString();
		
		int reachID = Integer.parseInt( StringUtils.substringBetween(response, "<id>", "</id>") );
		String reachName = StringUtils.substringBetween(response, "<name>", "</name>");

		assertEquals(4428, reachID);
		assertEquals("POTOMAC R", reachName);
		
	}
	
	public void testContextByID() throws Exception {

		PredictContextRequest contextReq = buildPredictContext();	//Build a context from a canned file
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline PCPipe = new PredictContextPipeline();
		PCPipe.dispatch(contextReq, out);
		
		IDByPointRequest req = buildIDByPointRequest4();
		
		out = new ByteArrayOutputStream();
		IDByPointPipeline IDpipe = new IDByPointPipeline();
		IDpipe.dispatch(req, out);
		String response = out.toString();
		
		int reachID = Integer.parseInt( StringUtils.substringBetween(response, "<id>", "</id>") );
		String reachName = StringUtils.substringBetween(response, "<name>", "</name>");

		assertEquals(4428, reachID);
		assertEquals("POTOMAC R", reachName);
		
	}

	
	/*
	public void testHashCode() throws Exception {
		PredictionContext context1 = buildPredictContext().getPredictionContext();
		PredictionContext context2 = buildPredictContext().getPredictionContext();
		
		assertEquals(context1.hashCode(), context2.hashCode());
		assertEquals(context1.getId(), context2.getId());
		assertEquals(context1.getId().intValue(), context2.hashCode());
	}
	*/

	
	public IDByPointRequest buildIDByPointRequest1() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/id_request_1.xml");
		String xml = readToString(is);
		
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
	public IDByPointRequest buildIDByPointRequest2() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/id_request_2.xml");
		String xml = readToString(is);
		
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
	public IDByPointRequest buildIDByPointRequest3() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/id_request_3.xml");
		String xml = readToString(is);
		
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
	public IDByPointRequest buildIDByPointRequest4() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/id_request_4.xml");
		String xml = readToString(is);
		
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
	public PredictContextRequest buildPredictContext() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-2.xml");
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

