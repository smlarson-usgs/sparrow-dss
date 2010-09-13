package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.SparrowUnitTest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;

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
	
	
	public static void sendGetRequest(String URL) {
		
	}
	
	public static void sendPostRequest(String URL, Map<String, String> params) {
		
	}




}
