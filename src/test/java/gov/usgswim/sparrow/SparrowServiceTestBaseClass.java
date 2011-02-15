package gov.usgswim.sparrow;

import gov.usgswim.sparrow.SparrowDBTestBaseClass;

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
public abstract class SparrowServiceTestBaseClass extends SparrowDBTestBaseClass {

	
	public static final Long SERVICE_TEST_MODEL = -1L;
	public static final String WEB_XML_LOCATION = "src/test/resources/service_test_web.xml";

	// ============
	// SERVICE URLS
	// ============
	public static final String PREDICT_CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	public static final String BINNING_SERVICE_URL = "http://localhost:8088/sp_binning";


	protected static ServletRunner servletRunner;
	protected static ServletUnitClient client;
	
	@Override
	public void doOneTimeFrameworkSetup() throws Exception {
		super.doOneTimeFrameworkSetup();
		
		servletRunner =  new ServletRunner(new File(SparrowServiceTestBaseClass.WEB_XML_LOCATION));
		client = servletRunner.newClient();
	}

	@Override
	public void doOneTimeFrameworkTearDown() throws Exception {
		super.doOneTimeFrameworkTearDown();
		
		servletRunner.shutDown();
	}
	
	public static void sendGetRequest(String URL) {
		
	}
	
	public static void sendPostRequest(String URL, Map<String, String> params) {
		
	}

	@Override
	protected void doOneTimeLifecycleSetup() {
		//do nothing - the servlet container is handling the lifecycle
	}

	@Override
	protected void doOneTimeLifecycleTearDown() {
		//do nothing - the servlet container is handling the lifecycle
	}




}
