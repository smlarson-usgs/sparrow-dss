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
				state.parseToNextRootChildStart();
			} else if (state.isOnRootChildStart()){
				state.parseToRootChildEnd();
			} else if (state.isOnRootChildEnd()) {
				state.parseToNextRootChildStart();
			} else {
				throw new IllegalStateException("the above should be the only legal states");
			}
		}







//		boolean isStarted = false;
//
//		while (in.hasNext()) {
//			if (isStarted) {
//				// Don't advance past the first element.
//				eventCode = in.next();
//			} else {
//				isStarted = true;
//			}
//
//			// Main event loop -- parse until corresponding target end tag encountered.
//			switch (eventCode) {
//				case START_ELEMENT:
//					localName = in.getLocalName();
//					if (isTargetMatch(localName)) {
//						String modelIdString = in.getAttributeValue(DEFAULT_NS_PREFIX, "model-id");
//						modelID = (modelIdString == null || modelIdString.length() == 0)? null: Long.valueOf(modelIdString);
//
//						String idString = in.getAttributeValue(DEFAULT_NS_PREFIX, XMLStreamParserComponent.ID_ATTR);
//						id = (idString == null || idString.length() == 0)? null: Integer.valueOf(idString);
//					}// the following are all children matches
//					else if (AdjustmentGroups.isTargetMatch(localName)) {
//						this.adjustmentGroups = AdjustmentGroups.parseStream(in, modelID);
//						adjustmentGroupsID = (adjustmentGroups == null)? null: adjustmentGroups.getId();
//					} else if (TerminalReaches.isTargetMatch(localName)) {
//						this.terminalReaches = TerminalReaches.parseStream(in, modelID);
//						terminalReachesID = (terminalReaches == null)? null: terminalReaches.getId();
//					} else if (Analysis.isTargetMatch(localName)) {
//						this.analysis = Analysis.parseStream(in);
//						analysisID = (analysis == null)? null: analysis.getId();
//					} else if (AreaOfInterest.isTargetMatch(localName)) {
//						this.areaOfInterest = AreaOfInterest.parseStream(in, modelID);
//						areaOfInterestID = (areaOfInterest == null)? null: areaOfInterest.getId();
//					} else {
//						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
//					}
//					break;
//				case END_ELEMENT:
//					localName = in.getLocalName();
//					if (MAIN_ELEMENT_NAME.equals(localName)) {
//						// TODO [IK] Might want to calculate PC id here.
//						// TODO [eric] If the ID is unavailable because this is
//						// a new PContext, when in the object lifecycle should
//						// id be calculated and populated? Here? on cache.put()?
//						checkValidity();
//						return this; // we're done
//					}
//					// otherwise, error
//					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
//			}
//		}
//		throw new XMLParseValidationException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

}
