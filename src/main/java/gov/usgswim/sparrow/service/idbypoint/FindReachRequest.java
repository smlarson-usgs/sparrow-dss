package gov.usgswim.sparrow.service.idbypoint;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.HashMap;
import java.util.Map;

import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;




public class FindReachRequest implements XMLStreamParserComponent{

	private static final long serialVersionUID = 1L;
	private static final String MAIN_ELEMENT_NAME = "sparrow-reach-request";
	public String modelID;
	public String reachIDs;
	public String reachName;
	public String meanQLo, meanQHi;
	public String basinAreaLo, basinAreaHi;
	public String state;
	public String huc;
	public String boundingBox;
	public String edaName;
	public String edaCode;
	public Map<String, String> ancilValues = new HashMap<String, String>();

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	@Override
	public void checkValidity() throws XMLParseValidationException {
		// TODO Auto-generated method stub

	}
	@Override
	public String getParseTarget() {
		// TODO Auto-generated method stub
		return MAIN_ELEMENT_NAME;
	}
	@Override
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
	@Override
	public boolean isValid() {
		// TODO fill in later
		return true;
	}

	public boolean isEmptyRequest() {
		return (reachIDs == null) && (reachName == null)
				&& (meanQHi == null && meanQLo == null)
				&& (basinAreaHi == null && basinAreaLo == null)
				&& (huc == null)
				&& (edaCode == null) && (edaName == null);
	}

	//==================
	//PUBLIC CONSTRUCTOR
	//==================
	@Override
	public FindReachRequest parse(XMLStreamReader in)
	throws XMLStreamException, XMLParseValidationException {

		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : this.getClass().getSimpleName()
		+ " can only parse " + MAIN_ELEMENT_NAME + " elements.";
		boolean isStarted = false;

		while (in.hasNext()) {
			if (isStarted) {
				// Don't advance past the first element.
				eventCode = in.next();
			} else {
				isStarted = true;
			}

			// Main event loop -- parse until corresponding target end tag encountered.
			switch (eventCode) {
				case START_ELEMENT:
					localName = in.getLocalName();
					if (isTargetMatch(localName)) {
						//Nothing to do for the root element
					} else if ("model-id".equals(localName)) {
						modelID = ParserHelper.parseSimpleElementValue(in);
					} else if ("match-query".equals(localName)) {
						// don't do anything
					} else if ("reach-ids".equals(localName)) {
						// TODO change this to reach-ids
						reachIDs = ParserHelper.parseSimpleElementValue(in);
					} else if ("reach-name".equals(localName)) {
						reachName = ParserHelper.parseSimpleElementValue(in);
					} else if ("meanQHi".equals(localName)) {
						meanQHi = ParserHelper.parseSimpleElementValue(in);
					} else if ("meanQLo".equals(localName)) {
						meanQLo = ParserHelper.parseSimpleElementValue(in);
					} else if ("catch-area-hi".equals(localName)) {
						basinAreaHi = ParserHelper.parseSimpleElementValue(in);
					} else if ("catch-area-lo".equals(localName)) {
						basinAreaLo = ParserHelper.parseSimpleElementValue(in);
					} else if ("huc".equals(localName)) {
						huc = ParserHelper.parseSimpleElementValue(in);
					} else if ("reach-name".equals(localName)) {
						reachName = ParserHelper.parseSimpleElementValue(in);
					} else if ("bbox".equals(localName)) {
						boundingBox = ParserHelper.parseSimpleElementValue(in);
					} else if ("content".equals(localName) || "response-format".equals(localName)) {
						// ignoring for now
						ParserHelper.ignoreElement(in);
					} else if ("edaname".equals(localName)) {
						edaName = ParserHelper.parseSimpleElementValue(in);
					} else if ("edacode".equals(localName)) {
						edaCode = ParserHelper.parseSimpleElementValue(in);
					} else if (localName.startsWith("ancil_")) {
						String key = localName.substring("ancil_".length());
						String value = ParserHelper.parseSimpleElementValue(in);
						ancilValues.put(key, value);
					}
					// add more here
					else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {

						return this; // we're done
					}
					break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}


}
