package gov.usgswim.sparrow.parser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class DefaultGroup extends ReachGroup {

	public static final String MAIN_ELEMENT_NAME = "default-group";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	// ===========
	// CONSTRUCTOR
	// ===========
	public DefaultGroup(long modelID) {
		super(modelID);
	}
	
	@Override
	public ReachGroup parse(XMLStreamReader in) throws XMLStreamException,
	XMLParseValidationException {

		ReachGroup group = super.parse(in);

		if (group.getName() != null) {
			throw new XMLParseValidationException("The default-group does not allow a name");
		}

		if (group.getExplicitReaches().size() > 0) {
			throw new XMLParseValidationException("The default-group does not allow individual reaches to be specified");
		}

		//TODO:  Once logical sets are added, we need to check for their presense here....

		return group;
	}

	@Override
	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	@Override
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}


	@Override
	public boolean contains(long reachID) {
		// a default group contains ALL reaches
		return true;
	}
	
	
}
