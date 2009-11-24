package gov.usgswim.sparrow.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Ignore
public class PredictSerializerTest extends TestCase {
	//private Connection conn;

	public PredictSerializerTest(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}
	

	public boolean validate(String path) throws ParserConfigurationException, SAXException,
	IOException {
		// parse an XML document into a DOM tree
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature("http://xml.org/sax/features/namespaces", true);
		DocumentBuilder parser = dbf.newDocumentBuilder();

		Document document = parser.parse(new File(path));


		// create a SchemaFactory capable of understanding WXS schemas
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		// load a WXS schema, represented by a Schema instance
		Document schemaDoc1 = parser.parse(this.getClass().getResourceAsStream("/gov/usgswim/sparrow/prediction_request.xsd"));
		Document schemaDoc2 = parser.parse(this.getClass().getResourceAsStream("/gov/usgswim/sparrow/prediction_result.xsd"));
		System.out.println("Schema 1 document root element: " + schemaDoc1.getDocumentElement().getNodeName());
		System.out.println("Schema 2 document root element: " + schemaDoc2.getDocumentElement().getNodeName());
		Source schemaFile1 = new DOMSource(schemaDoc1);
		Source schemaFile2 = new DOMSource(schemaDoc2);

		Schema schema = factory.newSchema(new Source[] {schemaFile1, schemaFile2});

		// create a Validator instance, which can be used to validate an instance document
		Validator validator = schema.newValidator();

		// validate the DOM tree
		try {

			System.out.println("Validation document w/ root element: " + document.getDocumentElement().getNodeName());
			validator.validate(new DOMSource(document));
			return true;
		} catch (SAXException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		//return false;// eliminate this when uncommenting the rest
	}
	
	@Ignore
	public void testDummy() {
		// do nothing. This is here only because Infinitest complains if a test class has no test methods.
	}

}
