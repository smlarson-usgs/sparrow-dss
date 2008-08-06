package gov.usgswim.sparrow.parser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * The IndividualGroup is just a ReachGroup with a different main element and
 * additional business rules. Hence it does not need its own class. Note that in
 * general, we've made each class its own parser, but in this case, the Parser
 * is separate from the parsed class.
 */
public class IndividualGroup extends ReachGroup {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final String MAIN_ELEMENT_NAME = "individual-group";

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

        ReachGroup group = super.parse(in);

        if (group.getName() != null) {
            throw new XMLParseValidationException("The individual-group does not allow a name");
        }
        if (group.getDescription() != null) {
            throw new XMLParseValidationException("The individual-group does not allow a description");
        }
        if (group.getNotes() != null) {
            throw new XMLParseValidationException("The individual-group does not allow a notes");
        }
        if (group.getAdjustments().size() > 0) {
            throw new XMLParseValidationException("The individual-group does not allow group-wide adjustments to be specified");
        }
        if (group.getLogicalSets().size() > 0) {
            throw new XMLParseValidationException("The individual-group does not allow logical sets to be specified");
        }

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
}
