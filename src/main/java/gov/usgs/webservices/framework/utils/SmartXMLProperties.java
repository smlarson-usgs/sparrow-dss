package gov.usgs.webservices.framework.utils;

import static gov.usgswim.sparrow.util.ParserHelper.parseToStartTag;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Ingests XML and can return child/grandchildren as Strings or Objects or Lists of Strings or Objects
 * @author ilinkuo
 *
 */
public class SmartXMLProperties {
	protected Map<String, String> props = new HashMap<String, String>();

	public void add() {

	}

	public String get(String simpleOrCompoundKey) {
		return null;
	}

	public String get(String childType, String id) {
		return null;
	}

	public void parse(String xml) throws XMLStreamException, XMLParseValidationException {
		// TODO replace this by SourceToStreamConverter calls
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(xml));
		parse(reader);
	}

	public void parse(XMLStreamReader in)
	throws XMLStreamException, XMLParseValidationException {
		parseToStartTag(in);
		int eventCode = in.getEventType();
		ParseState state = new ParseState(in);
		// We assume now that we are at the root
		// parse to next rootchild
		// isItem or isList
		// if isItem, then read to corresponding end tag
		// if isList, then read children


		while(in.hasNext()) {
			if (state.isOnRoot()) {
				state.parseToNextRootChildStart();
			} else if (state.isOnRootChildStart()){
				state.parseToRootChildEnd();
			} else if (state.isOnRootChildEnd()) {
				state.parseToNextRootChildStart();
			} else {
				throw new IllegalStateException("the above should be the only legal states");
			}
		}
	}

}
