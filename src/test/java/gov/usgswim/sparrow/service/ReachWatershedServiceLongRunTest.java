package gov.usgswim.sparrow.service;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;

import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * 
 * @author eeverman
 *
 */
public class ReachWatershedServiceLongRunTest extends SparrowServiceTestBaseWithDB {
	
	private static final String SERVICE_URL = "http://localhost:8088/reachwatershed/";

	// ============
	// TEST METHODS
	// ============
	
	@Test
	public void get8153() throws Exception {
		WebRequest req = new PostMethodWebRequest(SERVICE_URL);
		req.setParameter(ReachWatershedService.REACH_ID_PARAM_NAME, "8153");
		req.setParameter(ReachWatershedService.MODEL_ID_PARAM_NAME, "50");

		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		//System.out.println("response: " + actualResponse);
		
		assertXpathEvaluatesTo("OK", "/ServiceResponseWrapper/status", actualResponse);
		assertXpathEvaluatesTo("GET", "/ServiceResponseWrapper/operation", actualResponse);
		assertXpathEvaluatesTo("gov.usgswim.sparrow.domain.ReachGeometry", "/ServiceResponseWrapper/entityClass", actualResponse);
		assertXpathEvaluatesTo("8153", "/ServiceResponseWrapper/entityList/entity[1]/id", actualResponse);
		
	}

	
}

