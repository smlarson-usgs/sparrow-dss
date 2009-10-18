package gov.usgswim.sparrow.service.findReachService;

import static org.junit.Assert.*;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.service.idbypoint.FindReachRequest;


public class FindReachRequestTest {

	String sampleRequest = "<sparrow-reach-request"
		+ "  xmlns=\"http://www.usgs.gov/sparrow/id-point-request/v0_2\""
		+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
		+ "	<model-id>22</model-id>"
		+ "	<match-query>"
		+ "		<reach-name>wolf</reach-name>"
		+ "		<meanQHi>123400</meanQHi>"
		+ "		<meanQLo>1</meanQLo>"
		+ "		<catch-area-hi>2345</catch-area-hi>"
		+ "		<catch-area-lo>1</catch-area-lo>"
		+ "		<huc>02</huc>"
		+ "	</match-query>"
		+ "	<content>"
		+ "	</content>"
		+ "	<response-format>"
		+ "		<mime-type>XML</mime-type>"
		+ "	</response-format>"
		+ "</sparrow-reach-request>";

	@Test
	public void testFindReachRequestParse() throws XMLStreamException, XMLParseValidationException {
		FindReachRequest frReq = new FindReachRequest();
		XMLStreamReader reader = convert(sampleRequest);
		reader.next();
		frReq = frReq.parse(reader);


		assertEquals( "22", frReq.modelID);
		assertEquals("wolf", frReq.reachName);
		assertEquals("123400", frReq.meanQHi);
		assertEquals("1", frReq.meanQLo );
		assertEquals("2345", frReq.basinAreaHi);
		assertEquals("1", frReq.basinAreaLo);
		assertEquals("02", frReq.huc);

	}


//	return (reachIDs == null) && (reachName == null)
//	&& (meanQHi == null && meanQLo == null)
//	&& (basinAreaHi == null && basinAreaLo == null)
//	&& (huc == null)
//	&& (edaCode == null) && (edaName == null);

	public void testIsEmptySucceedsForFields() {
		FindReachRequest request = null;

		{
			request = new FindReachRequest();
			request.meanQHi = "75";
			assertFalse(request.isEmptyRequest());
		}
		{
			request = new FindReachRequest();
			request.meanQLo = "65";
			assertFalse(request.isEmptyRequest());
		}
		{
			request = new FindReachRequest();
			request.basinAreaHi = "75";
			assertFalse(request.isEmptyRequest());
		}
		{
			request = new FindReachRequest();
			request.basinAreaLo = "65";
			assertFalse(request.isEmptyRequest());
		}
		{
			request = new FindReachRequest();
			request.huc = "75";
			assertFalse(request.isEmptyRequest());
		}
		{
			request = new FindReachRequest();
			request.edaCode = "75";
			assertFalse(request.isEmptyRequest());
		}
		{
			request = new FindReachRequest();
			request.edaName = "75";
			assertFalse(request.isEmptyRequest());
		}

	}

	public void testIsEmptyFails() {
		FindReachRequest request = null;

		{	// completely empty request
			request = new FindReachRequest();
			assertTrue(request.isEmptyRequest());
		}
	}


	/**
	 * TODO move this out of here , back to SourceToStreamConverter
	 * @param XMLString
	 * @return
	 * @throws XMLStreamException
	 */
	public static XMLStreamReader convert(String XMLString) throws XMLStreamException{
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(XMLString));

		return reader;
	}
}
