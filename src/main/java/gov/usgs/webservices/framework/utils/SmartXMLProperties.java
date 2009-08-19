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
				System.out.println("ROOT: " + in.getLocalName());
				state.parseToNextRootChildStart();
			} else if (state.isOnRootChildStart()){
				System.out.println("  CHILD START: " + in.getLocalName());
				state.setAsRootChild(in.getLocalName());
				state.parseToRootChildEndOrGrandChildStart();
			} else if (state.isOnRootChildEnd()) {
				System.out.println("    CHILD CONTENT: " + state.content);
				System.out.println("    CHILD NODE: " + state.getContentAsNode());
				System.out.println("  CHILD END: " + in.getLocalName());
				state.parseToNextRootChildStart();
			} else if (state.isOnRootGrandChildStart()) {
				if (state.isOnListElementStart()) {
					System.out.println("    LIST ELMT START: " + in.getLocalName());
					state.setAsListElement();
					// get all the grandchildren
					state.parseToListElementEnd();
				} else {
					// continue parsing to end
					state.parseToRootChildEnd();
//					System.out.println("    CHILD CONTENT: " + state.content);
//					System.out.println("    CHILD NODE: " + state.getContentAsNode());
				}
			} else if (state.isOnListElementEnd()) {
				System.out.println("      LIST ELMT CONTENT: " + state.content);
				System.out.println("      LIST ELMT NODE: " + state.getContentAsNode());
				state.parseToNextListElementOrRootChildEnd();
			} else {
				throw new IllegalStateException("the above should be the only legal states");
			}
		}
	}

}
