package gov.usgswim.sparrow.test;

import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.service.PredictParser;
import gov.usgswim.sparrow.service.PredictSerializer;
import gov.usgswim.sparrow.service.PredictService;
import gov.usgswim.sparrow.service.PredictServiceRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.stax2.XMLInputFactory2;
import org.xml.sax.SAXException;


public class PredictSerializerTest extends TestCase {
	private Connection conn;

	public PredictSerializerTest(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}


	/**
	 * Not really much of a test - it just writes the document out to a temp file,
	 * but it does validate it.
	 * @throws Exception
	 * TODO [IK] re fill this test
	 */
	public void testBasicPrediction() throws Exception {
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
				this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-0.xml"));

		PredictService service = new PredictService();
		PredictParser parser = new PredictParser();
		PredictServiceRequest pr = parser.parse(xsr);
		File outFile = File.createTempFile("predict-serilizer-test", ".xml");
		FileOutputStream fos = new FileOutputStream(outFile);

		XMLPassThroughFormatter formatter = new XMLPassThroughFormatter();
		formatter.dispatch(service.getXMLStreamReader(pr, false), fos);

		fos.close();
		System.out.println("Result of prediction serialization written to: " + outFile.getAbsolutePath());

		// TODO need to assign schemalocation via resolver
		// to D:/sparrow-main/classes/gov/usgswim/sparrow/prediction_result.xsd
		assertTrue(validate(outFile.getAbsolutePath()));


	}

	public boolean validate(String path) throws ParserConfigurationException, SAXException,
	IOException {
//		// parse an XML document into a DOM tree
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		dbf.setFeature("http://xml.org/sax/features/namespaces", true);
//		DocumentBuilder parser = dbf.newDocumentBuilder();
//
//		Document document = parser.parse(new File(path));
//
//
//		// create a SchemaFactory capable of understanding WXS schemas
//		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//
//		// load a WXS schema, represented by a Schema instance
//		Document schemaDoc1 = parser.parse(this.getClass().getResourceAsStream("/gov/usgswim/sparrow/prediction_request.xsd"));
//		Document schemaDoc2 = parser.parse(this.getClass().getResourceAsStream("/gov/usgswim/sparrow/prediction_result.xsd"));
//		System.out.println("Schema 1 document root element: " + schemaDoc1.getDocumentElement().getNodeName());
//		System.out.println("Schema 2 document root element: " + schemaDoc2.getDocumentElement().getNodeName());
//		Source schemaFile1 = new DOMSource(schemaDoc1);
//		Source schemaFile2 = new DOMSource(schemaDoc2);
//
//		Schema schema = factory.newSchema(new Source[] {schemaFile1, schemaFile2});
//
//		// create a Validator instance, which can be used to validate an instance document
//		Validator validator = schema.newValidator();
//
//		// validate the DOM tree
//		try {
//
//			System.out.println("Validation document w/ root element: " + document.getDocumentElement().getNodeName());
//			validator.validate(new DOMSource(document));
//			return true;
//		} catch (SAXException e) {
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//			return false;
//		}
		return false;// eliminate this when uncommenting the rest
	}


}
