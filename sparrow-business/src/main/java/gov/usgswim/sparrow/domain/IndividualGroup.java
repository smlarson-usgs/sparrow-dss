package gov.usgswim.sparrow.domain;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.util.Collections;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

/**
 * The IndividualGroup is just a ReachGroup with a different main element and
 * additional business rules. Hence it does not need its own class. Note that in
 * general, we've made each class its own parser, but in this case, the Parser
 * is separate from the parsed class.
 */
public class IndividualGroup extends ReachGroup {
	protected static Logger log = Logger.getLogger(IndividualGroup.class);

	/**
     *
     */
	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "individualGroup";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	// ===========
	// CONSTRUCTOR
	// ===========
	public IndividualGroup(long modelID) {
		super(modelID);
	}

	@Override
	public ReachGroup parse(XMLStreamReader in) throws XMLStreamException,
			XMLParseValidationException {

		// Huge sets of individual adjustments can be an issue
		// Count and log them if the number is large
		int indReachAdjustmentsCount = 0;

		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		if (!isParseTarget(localName) || eventCode != START_ELEMENT) {
			throw new XMLParseValidationException(this.getClass()
					.getSimpleName()
					+ " can only parse "
					+ getParseTarget()
					+ " elements.");
		}
		boolean isStarted = false;

		while (in.hasNext()) {
			if (isStarted) {
				// Don't advance past the first element.
				eventCode = in.next();
			} else {
				isStarted = true;
			}

			// Main event loop -- parse until corresponding target end tag
			// encountered.
			switch (eventCode) {
			case START_ELEMENT:
				localName = in.getLocalName();
				if (isParseTarget(localName)) {
					isEnabled = "true".equals(in.getAttributeValue(
							XMLConstants.DEFAULT_NS_PREFIX, "enabled"));

					// Disallow the name attribute
					String name = in.getAttributeValue(
							XMLConstants.DEFAULT_NS_PREFIX, "name");
					if (name != null) {
						throw new XMLParseValidationException(
								"The individual group is not allowed a name.");
					}
				} else if (ReachElement.isTargetMatch(localName)) {

					indReachAdjustmentsCount++;

					ReachElement r = new ReachElement();
					r.parse(in);
					reaches.add(r);
					
					//log every 1K reaches
					if (indReachAdjustmentsCount > 999 &&
							(indReachAdjustmentsCount / 1000) * 1000 ==  indReachAdjustmentsCount) {
						log.info("" + indReachAdjustmentsCount + 
								" individual reach adjustments created for model " +
								modelID + " (and counting...)");
					}
					
				} else {
					throw new XMLParseValidationException(
							"Invalid child element <" + localName + "> for "
									+ MAIN_ELEMENT_NAME);
				}
				break;
			case END_ELEMENT:
				localName = in.getLocalName();
				if (isParseTarget(localName)) {

					// Wrap collections as unmodifiable
					if (reaches != null) {
						reaches = Collections.unmodifiableList(reaches);
					} else {
						reaches = Collections.emptyList();
					}
					
					//log every 1K reaches
					if (indReachAdjustmentsCount > 999) {
						log.info("" + indReachAdjustmentsCount + " individual reach adjustments created for model " + modelID + ", total.");
					}

					checkValidity();
					return this; // we're done
				}
				// otherwise, error
				throw new XMLParseValidationException(
						"unexpected closing tag of </" + localName
								+ ">; expected  " + getParseTarget());
				// break;
			}
		}
		throw new XMLParseValidationException("tag <" + getParseTarget()
				+ "> not closed. Unexpected end of stream?");
	}

	@Override
	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	@Override
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
}
