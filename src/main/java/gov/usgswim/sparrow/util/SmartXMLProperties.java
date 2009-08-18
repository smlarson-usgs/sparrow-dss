package gov.usgswim.sparrow.util;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import gov.usgs.webservices.framework.utils.UsgsStAXUtils;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static gov.usgswim.sparrow.util.ParserHelper.*;

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
				System.out.println("on Root: " + in.getLocalName());
				state.parseToNextRootChildStart();
			} else if (state.isOnRootChildStart()){
				System.out.println("on Child Start: " + in.getLocalName());
				state.parseToRootChildEnd();
			} else if (state.isOnRootChildEnd()) {
				System.out.println("on Child End: " + in.getLocalName());
				System.out.println(state.content.toString());
				state.parseToNextRootChildStart();
			} else {
				throw new IllegalStateException("the above should be the only legal states");
			}
		}
	}

}
