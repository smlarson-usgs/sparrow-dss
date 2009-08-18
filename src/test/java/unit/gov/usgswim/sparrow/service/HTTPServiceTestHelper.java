package gov.usgswim.sparrow.service;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public abstract class HTTPServiceTestHelper {

	public static final Long TEST_MODEL = -1L;
	public static final String WEB_XML_LOCATION = "public_html/WEB-INF/web.xml";
	static ServletRunner servletRunner;
	static ServletUnitClient client;

	public static void setupHTTPUnitTest() throws IOException, SAXException {
		servletRunner =  new ServletRunner(new File(HTTPServiceTestHelper.WEB_XML_LOCATION));
		client = servletRunner.newClient();
	}

	public static void teardownHTTPUnitTest() {
		servletRunner.shutDown();
	}



}
