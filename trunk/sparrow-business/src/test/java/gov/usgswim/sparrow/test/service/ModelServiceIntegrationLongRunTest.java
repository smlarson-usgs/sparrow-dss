package gov.usgswim.sparrow.test.service;

import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.service.model.ModelParser;
import gov.usgswim.sparrow.service.model.ModelRequest;
import gov.usgswim.sparrow.service.model.ModelService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


import org.codehaus.stax2.XMLInputFactory2;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Unit tests for the {@code ModelService} class.
 */
public class ModelServiceIntegrationLongRunTest extends SparrowTestBaseWithDBandCannedModel50 {

	/**
	 * Not really much of a test - it just writes the document out to a temp file,
	 * but it does validate it.
	 *
	 * @throws Exception
	 */
	@Test
	public void testBasicMetaRequest() throws Exception {

		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(this.getClass()
						.getResourceAsStream("/gov/usgswim/sparrow/test/sample/meta_request_1.xml"));

		ModelService service = new ModelService();
		ModelParser parser = new ModelParser();

		File outFile = File.createTempFile("model-service-test", ".xml");
		FileOutputStream fos = new FileOutputStream(outFile);

		ModelRequest req = parser.parse(xsr);

		XMLPassThroughFormatter formatter = new XMLPassThroughFormatter();
		XMLStreamReader in = service.getXMLStreamReader(req, false);
		formatter.dispatch(in, fos);

		fos.close();
		System.out.println("Result of model request written to: "
						+ outFile.getAbsolutePath());

		//assertTrue(validate(outFile.getAbsolutePath())); turned off because can't validate as no xsd provided
	}

	public boolean validate(String path) throws ParserConfigurationException,
					SAXException, IOException {
		// TODO [IK] Temporarily commented out while jaxp issues are to be fixed
		// parse an XML document into a DOM tree
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//dbf.setFeature("http://xml.org/sax/features/namespaces", true);
		DocumentBuilder parser = dbf.newDocumentBuilder();

		Document document = parser.parse(new File(path));

		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory
						.newInstance("http://www.w3.org/2001/XMLSchema");

		// load a WXS schema, represented by a Schema instance
		Document schemaDoc1 = parser.parse(this.getClass().getResourceAsStream(
						"/gov/usgswim/sparrow/meta_response.xsd"));
		System.out.println("Schema 1 document root element: "
						+ schemaDoc1.getDocumentElement().getNodeName());
		Source schemaFile1 = new DOMSource(schemaDoc1);

		Schema schema = factory.newSchema(new Source[]{schemaFile1});

		// create a Validator instance, which can be used to validate an
		// instance document
		Validator validator = schema.newValidator();

		// validate the DOM tree
		try {

			System.out.println("Validation document w/ root element: "
							+ document.getDocumentElement().getNodeName());
			validator.validate(new DOMSource(document));
			return true;
		} catch (SAXException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		// return false;
	}
}
