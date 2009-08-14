package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.ServiceTestConstants.WEB_XML_LOCATION;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class DocumentationServiceTest {

	private static final String DOC_SERVICE_URL = "http://localhost:8088/sp_doc";

	@Test
	public void testDocServiceWithFieldIDAndModelID() throws IOException, SAXException {
		ServletRunner servletRunner = new ServletRunner(new File(WEB_XML_LOCATION));      // (1) use the web.xml file to define mappings
        ServletUnitClient client = servletRunner.newClient();               // (2) create a client to invoke the application

        String queryString = "?model=-1&";

        WebResponse response = client.getResponse( DOC_SERVICE_URL + queryString);
        System.out.println(response.getText());


	}
}
