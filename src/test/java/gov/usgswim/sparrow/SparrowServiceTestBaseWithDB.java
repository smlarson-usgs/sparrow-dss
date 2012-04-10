package gov.usgswim.sparrow;

import gov.usgswim.sparrow.SparrowTestBaseWithDB;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * A base class for JUnit tests that access services.
 * @author eeverman
 *
 */
public abstract class SparrowServiceTestBaseWithDB extends SparrowTestBaseWithDBandCannedModel50 {

	
	public static final String WEB_XML_LOCATION = "src/test/resources/service_test_web.xml";
	
	// ============
	// SERVICE URLS
	// ============


	protected static ServletRunner servletRunner;
	protected static ServletUnitClient client;
	
	@Override
	public void doOneTimeFrameworkSetup() throws Exception {
		super.doOneTimeFrameworkSetup();
		
		servletRunner =  new ServletRunner(new File(SparrowServiceTestBaseWithDB.WEB_XML_LOCATION));
		client = servletRunner.newClient();
	}

	@Override
	public void doOneTimeFrameworkTearDown() throws Exception {
		super.doOneTimeFrameworkTearDown();
		
		servletRunner.shutDown();
	}
	

	@Override
	protected void doOneTimeLifecycleSetup() {
		//do nothing - the servlet container is handling the lifecycle
	}

	@Override
	protected void doOneTimeLifecycleTearDown() {
		//do nothing - the servlet container is handling the lifecycle
	}
	
	/**
	 * Sends a GET request to the unit testing web client and returns the
	 * response text.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String sendGetRequest(String url) throws Exception {
		WebRequest request = new GetMethodWebRequest(url);
		
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();

		return actualResponse;
	}
	
	
	/**
	 * Sends a post request with a single xml parameter to the unit testing web
	 * client and returns the response text.
	 * @param url
	 * @param xmlRequest
	 * @return
	 * @throws Exception
	 */
	protected String sendPostRequest(String url, String xmlRequest) throws Exception {
		HashMap<String, String> params = new HashMap<String, String>(1, 1);
		params.put("xmlreq", xmlRequest);
		return sendPostRequest(url, params);
	}
	
	/**
	 * Sends a post request to the unit testing web client and returns the
	 * response text.
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	protected String sendPostRequest(String url, Map<String, String> params) throws Exception {
		WebRequest request = new PostMethodWebRequest(url);
		
		Set<Entry<String, String>> paramVals = params.entrySet();
		for (Entry<String, String> entry : paramVals) {
			request.setParameter(entry.getKey(), entry.getValue());
		}
		
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();

		return actualResponse;
	}





}
