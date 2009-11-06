package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.test.TestHelper;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class AnalysisTest extends TestCase {

    /** Valid xml string represention of the analysis section. */
    public static final String VALID_FRAGMENT = ""
        + "<analysis>"
        + "  <select>"
        + "    <dataSeries source=\"1\" per=\"area\">incremental</dataSeries>"
        + "    <aggFunction per=\"area\">avg</aggFunction>"
        + "    <analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
        + "    <nominalComparison type=\"percent\"/>"
        + "  </select>"
        + "  <limitTo>contributors</limitTo>"
        + "  <groupBy>HUC8</groupBy>"
        + "</analysis>"
        ;
    
    public static final String VALID_FRAGMENT_BUG_1 = "" +
        "<analysis>" +
	        "<select>" +
		        "<dataSeries>incremental_std_error_estimate</dataSeries>" +
		        "<nominalComparison type=\"none\"></nominalComparison>" +
	        "</select>" +
	        "<groupBy></groupBy>" +
        "</analysis>";

    /** Used to create XMLStreamReaders from XML strings. */
    protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public void testParse1() throws Exception {

		Analysis anal = buildTestInstance(VALID_FRAGMENT);

		assertEquals("contributors", anal.getLimitTo());
		assertEquals("HUC8", anal.getGroupBy());

	}
	
	public void testBug1() throws Exception {

		Analysis anal = buildTestInstance(VALID_FRAGMENT_BUG_1);

		assertEquals(DataSeriesType.incremental_std_error_estimate,  anal.getSelect().getDataSeries());
		assertNull(anal.getGroupBy());
		assertTrue(anal.isValid());

	}

	public void testHashcode() throws Exception {

		Analysis analysis1 = buildTestInstance(VALID_FRAGMENT);
		Analysis analysis2 = buildTestInstance(VALID_FRAGMENT);
		TestHelper.testHashCode(analysis1, analysis2, analysis2.clone());

		// test IDs
		assertEquals(analysis1.hashCode(), analysis1.getId().intValue());
		assertEquals(analysis2.hashCode(), analysis2.getId().intValue());
	}


	@SuppressWarnings("static-access")
  public Analysis buildTestInstance(String stringToParse) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(stringToParse));
		Analysis test = new Analysis();
		reader.next();
		test = test.parse(reader);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(test.MAIN_ELEMENT_NAME, reader.getLocalName());

		return test;
	}
}
