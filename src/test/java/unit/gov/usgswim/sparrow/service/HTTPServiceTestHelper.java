package gov.usgswim.sparrow.service;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class HTTPServiceTestHelper {

	public static final Long TEST_MODEL = -1L;
	public static final String WEB_XML_LOCATION = "public_html/WEB-INF/web.xml";

	// ============
	// SERVICE URLS
	// ============
	public static final String PREDICT_CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	public static final String BINNING_SERVICE_URL = "http://localhost:8088/sp_binning";


	protected static ServletRunner servletRunner;
	protected static ServletUnitClient client;

	public static void setupHTTPUnitTest() throws IOException, SAXException {
		servletRunner =  new ServletRunner(new File(HTTPServiceTestHelper.WEB_XML_LOCATION));
		client = servletRunner.newClient();
	}

	public static void teardownHTTPUnitTest() {
		servletRunner.shutDown();
	}




}
