package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.SparrowUnitTestBaseClass;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class AnalysisTest extends TestCase {

	/** Valid xml string represention of the analysis section. */
	public static final String VALID_ADV_FRAGMENT_1 = ""
		+ "<advancedAnalysis>"
		+ "  <select>"
		+ "    <dataSeries source=\"1\" per=\"area\">incremental</dataSeries>"
		+ "    <analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
		+ "  </select>"
		+ "  <limitTo>contributors</limitTo>"
		+ "  <groupBy aggFunction=\"avg\">huc8</groupBy>"
		+ "</advancedAnalysis>"
		;

	public static final String VALID_ADV_FRAGMENT_2 = ""
		+ "<advancedAnalysis>"
		+ "  <select>"
		+ "    <dataSeries source=\"1\" per=\"area\">incremental</dataSeries>"
		+ "    <analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
		+ "  </select>"
		+ "  <limitTo>contributors</limitTo>"
		+ "  <groupBy/>"
		+ "</advancedAnalysis>"
		;

	public static final String INVALID_ADV_FRAGMENT_1 = ""
		+ "<advancedAnalysis>"
		+ "  <select>"
		+ "    <dataSeries source=\"1\" per=\"area\">incremental</dataSeries>"
		+ "    <analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
		+ "  </select>"
		+ "  <limitTo>contributors</limitTo>"
		+ "  <groupBy>huc8</groupBy>"
		+ "</advancedAnalysis>"
		;

	public static final String VALID_ADV_FRAGMENT_BUG_1 = "" +
	"<advancedAnalysis>" +
	"<select>" +
	"<dataSeries>incremental_std_error_estimate</dataSeries>" +
	"</select>" +
	"<groupBy></groupBy>" +
	"</advancedAnalysis>";
	public static final String VALID_BASIC_FRAGMENT_1 = ""
		+ "<analysis>"
		+ "  <dataSeries source=\"1\">incremental</dataSeries>"
		+ "  <groupBy aggFunction=\"avg\">huc8</groupBy>"
		+ "</analysis>"
		;

	public static final String VALID_BASIC_FRAGMENT_2 = ""
		+ "<analysis>"
		+ "  <dataSeries>incremental</dataSeries>"
		+ "  <groupBy/>"
		+ "</analysis>"
		;

	public static final String INVALID_BASIC_FRAGMENT_1 = ""
		+ "<analysis>"
		+ "  <dataSeries source=\"1\">incremental</dataSeries>"
		+ "  <groupBy>huc8</groupBy>"
		+ "</analysis>"
		;

	/** Used to create XMLStreamReaders from XML strings. */
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public void testAdvParse1() throws Exception {

		Analysis anal = buildTestInstance(VALID_ADV_FRAGMENT_1);
		AdvancedAnalysis advAnal = (AdvancedAnalysis)anal;

		assertEquals("contributors", advAnal.getLimitTo());
		assertEquals("huc8", anal.getGroupBy());
		assertEquals("avg", anal.getAggFunction());
		assertTrue(anal.isValid());

		anal = buildTestInstance(VALID_ADV_FRAGMENT_2);
		advAnal = (AdvancedAnalysis)anal;

		assertEquals("contributors", advAnal.getLimitTo());
		assertNull(anal.getGroupBy());
		assertNull(anal.getAggFunction());
		assertTrue(anal.isValid());

		anal = buildTestInstance(INVALID_ADV_FRAGMENT_1);
		advAnal = (AdvancedAnalysis)anal;

		assertEquals("contributors", advAnal.getLimitTo());
		assertEquals("huc8", anal.getGroupBy());
		assertNull(anal.getAggFunction());
		assertFalse(anal.isValid());
	}

	public void testBug1() throws Exception {

		Analysis anal = buildTestInstance(VALID_ADV_FRAGMENT_BUG_1);
		AdvancedAnalysis advAnal = (AdvancedAnalysis)anal;

		assertEquals(DataSeriesType.incremental_std_error_estimate,  advAnal.getSelect().getDataSeries());
		assertEquals(DataSeriesType.incremental_std_error_estimate,  anal.getDataSeries());
		assertNull(anal.getGroupBy());
		assertTrue(anal.isValid());

	}

	public void testBasicParse() throws Exception {

		Analysis anal = buildTestInstance(VALID_BASIC_FRAGMENT_1);

		assertEquals(DataSeriesType.incremental, anal.getDataSeries());
		assertEquals(Integer.valueOf(1), anal.getSource());
		assertEquals("huc8", anal.getGroupBy());
		assertEquals("avg", anal.getAggFunction());
		assertTrue(anal.isValid());

		anal = buildTestInstance(VALID_BASIC_FRAGMENT_2);

		assertEquals(DataSeriesType.incremental, anal.getDataSeries());
		assertNull(anal.getSource());
		assertNull(anal.getGroupBy());
		assertNull(anal.getAggFunction());
		assertTrue(anal.isValid());

		anal = buildTestInstance(INVALID_BASIC_FRAGMENT_1);

		assertEquals(DataSeriesType.incremental, anal.getDataSeries());
		assertEquals(Integer.valueOf(1), anal.getSource());
		assertEquals("huc8", anal.getGroupBy());
		assertNull(anal.getAggFunction());
		assertFalse(anal.isValid());
	}

	public void testHashcode() throws Exception {

		Analysis analysis1 = buildTestInstance(VALID_ADV_FRAGMENT_1);
		Analysis analysis2 = buildTestInstance(VALID_ADV_FRAGMENT_1);
		SparrowUnitTestBaseClass.testHashCode(analysis1, analysis2, analysis2.clone());

		// test IDs
		assertEquals(analysis1.hashCode(), analysis1.getId().intValue());
		assertEquals(analysis2.hashCode(), analysis2.getId().intValue());
	}


	public Analysis buildTestInstance(String stringToParse) throws XMLStreamException {

		XMLStreamReader reader = null;
		Analysis test = null;

		try {
			reader = inFact.createXMLStreamReader(new StringReader(stringToParse));
			reader.next();
			test = Analysis.parseAnyAnalysis(reader);
		} catch (XMLParseValidationException e) {
			//Ignore - validation errors
		}


		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(test.getParseTarget(), reader.getLocalName());

		return test;
	}
}
