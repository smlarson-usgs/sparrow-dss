package gov.usgswim.sparrow.test.service;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ComparePercentageView;
import gov.usgswim.sparrow.deprecated.PredictParser;
import gov.usgswim.sparrow.deprecated.PredictService;
import gov.usgswim.sparrow.deprecated.PredictServiceRequest;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

import junit.framework.TestCase;

import org.codehaus.stax2.XMLInputFactory2;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
public class PredictServiceIntegrationTest extends TestCase {


	//private Connection conn;


	/**
	 */
//	public void testReadData1() throws Exception {

//	XMLInputFactory xinFact = XMLInputFactory2.newInstance();
//	XMLStreamReader xsr = xinFact.createXMLStreamReader(
//	getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-1.xml"));

//	PredictParser2 parser = new PredictParser2();
//	PredictServiceRequest req = parser.parse(xsr);

//	assertEquals(gov.usgswim.sparrow.service.PredictServiceRequest.ResponseFilter.ALL, req.getResponseType());
//	assertEquals(gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.VALUES, req.getPredictType());
//	assertEquals(gov.usgswim.sparrow.service.PredictServiceRequest.DataSeries.ALL, req.getDataSeries());

//	PredictRequest2 pReq = req.getPredictRequest2();
//	assertEquals(22L, pReq.getModelId().longValue());
//	assertEquals(2, pReq.getAdjustmentSet2().getAdjustmentCount());


//	Adjustment2 adj = pReq.getAdjustmentSet2().getAdjustments()[0];
//	assertEquals(1, adj.getSrcId());
//	assertEquals(.5d, adj.getValue());

//	adj = pReq.getAdjustmentSet2().getAdjustments()[1];
//	assertEquals(4, adj.getSrcId());
//	assertEquals(2d, adj.getValue());
//	}

//	public void testReadData2() throws Exception {
//	Point.Double point = new Point.Double();
//	point.x = -100;
//	point.y = 40;

//	XMLInputFactory xinFact = XMLInputFactory2.newInstance();
//	XMLStreamReader xsr = xinFact.createXMLStreamReader(
//	getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-2.xml"));

//	PredictParser2 parser = new PredictParser2();
//	PredictServiceRequest req = parser.parse(xsr);

//	assertEquals(gov.usgswim.sparrow.service.PredictServiceRequest.ResponseFilter.NEAR_POINT, req.getResponseType());
//	assertEquals(5, req.getIdByPointRequest().getNumberOfResults());
//	assertEquals(point, req.getIdByPointRequest().getPoint());
//	assertEquals(gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.PERC_CHG_FROM_NOMINAL, req.getPredictType());
//	assertEquals(gov.usgswim.sparrow.service.PredictServiceRequest.DataSeries.INCREMENTAL_ADD, req.getDataSeries());

//	PredictRequest2 pReq = req.getPredictRequest2();
//	assertEquals(22L, pReq.getModelId().longValue());
//	assertEquals(2, pReq.getAdjustmentSet2().getAdjustmentCount());


//	Adjustment2 adj = pReq.getAdjustmentSet2().getAdjustments()[0];
//	assertEquals(1, adj.getSrcId());
//	assertEquals(.5d, adj.getValue());

//	adj = pReq.getAdjustmentSet2().getAdjustments()[1];
//	assertEquals(4, adj.getSrcId());
//	assertEquals(2d, adj.getValue());
//	}

//	public void testAdjustSourceValues() throws Exception {

//	XMLInputFactory xinFact = XMLInputFactory2.newInstance();
//	XMLStreamReader xsr1 = xinFact.createXMLStreamReader(
//	getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-3.xml"));

//	PredictParser2 parser = new PredictParser2();
//	AdjustmentSet2 adjSet = parser.parse(xsr1).getPredictRequest2().getAdjustmentSet2();
//	Adjustment2[] adjs = adjSet.getAdjustments();

//	assertEquals(4, adjSet.getAdjustmentCount());

//	//1st Adjustment
//	assertEquals(AdjustmentType.GROSS_SRC_ADJUST, adjs[0].getType());
//	assertEquals(1, adjs[0].getSrcId());
//	assertEquals(.5d, adjs[0].getValue());

//	//2nd Adjustment
//	assertEquals(AdjustmentType.GROSS_SRC_ADJUST, adjs[1].getType());
//	assertEquals(4, adjs[1].getSrcId());
//	assertEquals(2d, adjs[1].getValue());

//	//3rd Adjustment
//	assertEquals(AdjustmentType.SPECIFIC_ADJUST, adjs[2].getType());
//	assertEquals(1, adjs[2].getSrcId());
//	assertEquals(9.99d, adjs[2].getValue());

//	//4rd Adjustment
//	assertEquals(AdjustmentType.SPECIFIC_ADJUST, adjs[3].getType());
//	assertEquals(2, adjs[3].getSrcId());
//	assertEquals(7.77d, adjs[3].getValue());
//	}

//	public void testAdjustedTotalPrediction() throws Exception {

//	XMLInputFactory xinFact = XMLInputFactory2.newInstance();
//	XMLStreamReader xsr = xinFact.createXMLStreamReader(
//	getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-5.xml"));


//	PredictService2 service = new PredictService2();
//	PredictParser2 parser = new PredictParser2();

//	PredictServiceRequest pr = parser.parse(xsr);
//	DataTable result = service.runPrediction(pr);

//	boolean foundNonZero = false;

//	for (int r = 0; r < result.getRowCount() && ! foundNonZero; r++)  {
//	for (int c = 0; c < result.getColumnCount(); c++)  {
//	if (result.getDouble(r, c) > 0d) {
//	foundNonZero = true;
//	System.out.println("Found non zero total predict value at r:" + r + " c: " + c + " value = " + result.getDouble(r, c));
//	break;
//	}
//	}

//	}

//	if (!foundNonZero) {
//	fail("no non-zero values found.");
//	}

//	}

//	public void testAdjustedCompPrediction() throws Exception {

//	XMLInputFactory xinFact = XMLInputFactory2.newInstance();
//	XMLStreamReader xsr = xinFact.createXMLStreamReader(
//	getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-3.xml"));


//	PredictService2 service = new PredictService2();
//	PredictParser2 parser = new PredictParser2();

//	PredictServiceRequest pr = parser.parse(xsr);
//	DataTable result = service.runPrediction(pr);

//	boolean foundNonZero = false;

//	for (int r = 0; r < result.getRowCount() && ! foundNonZero; r++)  {
//	for (int c = 0; c < result.getColumnCount(); c++)  {
//	if (result.getDouble(r, c) > 0d) {
//	foundNonZero = true;
//	System.out.println("Found non zero comp predict value at r:" + r + " c: " + c + " value = " + result.getDouble(r, c));
//	break;
//	}
//	}

//	}

//	if (!foundNonZero) {
//	fail("no non-zero values found.");
//	}

//	}

	public void testBasicPredictionValues() throws Exception {

		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
				getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-0.xml"));

		PredictService service = new PredictService();
		PredictParser parser = new PredictParser();

		PredictServiceRequest pr = parser.parse(xsr);
		DataTable result = service.runPrediction(pr);
		ComparePercentageView comp = buildPredictionComparison(result);

		System.out.println("col 11 error: " + comp.findMaxCompareValue(11)); //11, 13, 15, 16

		System.out.println(comp.findMaxCompareValue());
		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);
	}

	/**
	 * @deprecated out-of-date
	 * @throws Exception
	 */
//	public void testBasicPredictionResultValidatation() throws Exception {
//
//		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
//		XMLStreamReader xsr = xinFact.createXMLStreamReader(
//				getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-0.xml"));
//		File outFile = File.createTempFile("predict-service-test", ".xml");
//		FileOutputStream fos = new FileOutputStream(outFile);
//
//		PredictService service = new PredictService();
//		PredictParser parser = new PredictParser();
//
//		PredictServiceRequest pr = parser.parse(xsr);
//
//		XMLPassThroughFormatter formatter = new XMLPassThroughFormatter();
//		XMLStreamReader in = service.getXMLStreamReader(pr, false);
//		formatter.dispatch(in, fos);
//
//		fos.close();
//		System.out.println("Result of prediction serialization written to: " + outFile.getAbsolutePath());
//
//		assertTrue(validate(outFile.getAbsolutePath()));
//	}

	protected ComparePercentageView buildPredictionComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};

		ComparePercentageView comp = new ComparePercentageView(toBeCompared, data, DEFAULT_COMP_COLUMN_MAP, false);

		return comp;
	}

	/**
	 * @param path
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @deprecated
	 */
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
		Document schemaDoc1 = parser.parse(getClass().getResourceAsStream("/gov/usgswim/sparrow/prediction_request.xsd"));
		Document schemaDoc2 = parser.parse(getClass().getResourceAsStream("/gov/usgswim/sparrow/prediction_result.xsd"));
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
	}

}

