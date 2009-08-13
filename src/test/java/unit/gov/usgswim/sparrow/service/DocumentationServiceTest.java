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

	@Test
	public void testDocService() throws IOException, SAXException {
		ServletRunner servletRunner = new ServletRunner(new File(WEB_XML_LOCATION));      // (1) use the web.xml file to define mappings
        ServletUnitClient client = servletRunner.newClient();               // (2) create a client to invoke the application

        WebResponse response = client.getResponse( "http://localhost:8088/sp_doc" );
        System.out.println(response.getText());


	}
}
