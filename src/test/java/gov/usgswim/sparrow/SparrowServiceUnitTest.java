package gov.usgswim.sparrow;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
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
 * A base class for JUnit tests that access services, but do not require a
 * db connection.
 * @author eeverman
 *
 */
public abstract class SparrowServiceUnitTest extends SparrowUnitTest {

	
	public static final Long SERVICE_TEST_MODEL = -1L;
	public static final String WEB_XML_LOCATION = "public_html/WEB-INF/web.xml";

	// ============
	// SERVICE URLS
	// ============
	public static final String PREDICT_CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	public static final String BINNING_SERVICE_URL = "http://localhost:8088/sp_binning";
	public static final String PREDICT_EXPORT_SERVICE_URL = "http://localhost:8088/sp_predict";


	protected static ServletRunner servletRunner;
	protected static ServletUnitClient client;
	
	
	@Override
	protected void doOneTimeLifecycleSetup() throws Exception {
		//Do nothing - the lifecycle is setup via the servlet
	}

	@Override
	protected void doOneTimeLifecycleTearDown() {
		//Do nothing - the lifecycle is handled via the servlet
	}
	
	@Override
	protected void doOneTimeFrameworkSetup() throws Exception {
		servletRunner =  new ServletRunner(new File(SparrowServiceTest.WEB_XML_LOCATION));
		client = servletRunner.newClient();
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.SparrowUnitTest#doTearDown()
	 */
	@Override
	protected void doOneTimeFrameworkTearDown() throws Exception {
		servletRunner.shutDown();
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
