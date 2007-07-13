package gov.usgswim.sparrow.test;

import com.ctc.wstx.evt.WstxEventWriter;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.service.DomainSerializer;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.service.HttpServiceHandler;

import gov.usgswim.sparrow.service.ModelRequest;
import gov.usgswim.sparrow.service.ModelService;
import gov.usgswim.sparrow.service.PredictService;
import gov.usgswim.sparrow.service.PredictionSerializer;
import gov.usgswim.sparrow.service.ServiceHandler;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import javax.xml.stream.XMLStreamReader;

import javax.xml.stream.XMLStreamWriter;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import oracle.jdbc.OracleDriver;

import org.codehaus.stax2.XMLInputFactory2;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

public class ModelServiceTest extends TestCase {
	//private Connection conn;

	public ModelServiceTest(String sTestName) {
		super(sTestName);
	}

	
	/**
	 * Not really much of a test - it just writes the document out to a temp file,
	 * but it does validate it.
	 * @throws Exception
	 */
	public void testBasicMetaRequest() throws Exception {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/meta_request_1.xml"));
		
		ServiceHandler service = new ModelService();
		File outFile = File.createTempFile("model-service-test", ".xml");
		FileOutputStream fos = new FileOutputStream(outFile);
		
		service.dispatch(xsr, fos);

		fos.close();
		System.out.println("Result of model request written to: " + outFile.getAbsolutePath());
		
		
		assertTrue(validate(outFile.getAbsolutePath()));
		
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
		Document schemaDoc1 = parser.parse(this.getClass().getResourceAsStream("/gov/usgswim/sparrow/meta_response.xsd"));
		System.out.println("Schema 1 document root element: " + schemaDoc1.getDocumentElement().getNodeName());
    Source schemaFile1 = new DOMSource(schemaDoc1);
		
    Schema schema = factory.newSchema(new Source[] {schemaFile1});

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
	}
}
