package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.SparrowDBTest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * A base class for JUnit tests that access services.
 * @author eeverman
 *
 */
public abstract class SparrowServiceTest extends SparrowDBTest {

	
	public static final Long SERVICE_TEST_MODEL = -1L;
	public static final String WEB_XML_LOCATION = "public_html/WEB-INF/web.xml";

	// ============
	// SERVICE URLS
	// ============
	public static final String PREDICT_CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	public static final String BINNING_SERVICE_URL = "http://localhost:8088/sp_binning";


	protected static ServletRunner servletRunner;
	protected static ServletUnitClient client;
	
	@BeforeClass
	public static void setupHTTPUnitTest() throws IOException, SAXException {
		servletRunner =  new ServletRunner(new File(SparrowServiceTest.WEB_XML_LOCATION));
		client = servletRunner.newClient();
	}

	@AfterClass
	public static void teardownHTTPUnitTest() {
		servletRunner.shutDown();
	}
	
	public static void sendGetRequest(String URL) {
		
	}
	
	public static void sendPostRequest(String URL, Map<String, String> params) {
		
	}

	@Override
	protected void doLifecycleSetup() {
		//do nothing - the servlet container is handling the lifecycle
	}

	@Override
	protected void doLifecycleTearDown() {
		//do nothing - the servlet container is handling the lifecycle
	}




}
