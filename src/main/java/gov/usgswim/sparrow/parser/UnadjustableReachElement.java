package gov.usgswim.sparrow.parser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

//TODO:  This class and all similar should implement comparable.
//TODO:  This class should throw an error if parse is called 2nd time.

/**
 * Represents a single reach as part of a reach group within a PredictionContext.
 * A Reach has a model-specific Identifier that identifies a specific reach
 * within a Model, but the ID is not a DB PK.  Unadjustable reaches (reaches
 * within a reach group) are not allowed adjustments.
 * 
 * Note that a Reach is not an independent entity and thus does not override 
 * equals or the hashcode.  It does, however, provide a getStateHash method
 * which generates a repeatable hashcode representing the state of the
 * reach and its associated adjustments.  This method is a convenience to parent
 * classes who need to include the state of their reaches in their hashcodes.
 */
public class UnadjustableReachElement extends ReachElement {
    private static final long serialVersionUID = -7230343316711453672L;

    public synchronized ReachElement parse(XMLStreamReader in)
    throws XMLStreamException, XMLParseValidationException {
        
        ReachElement reach = super.parse(in);
        
        // Throw an exception if the XML defines an adjustment for this reach
        if (reach.getAdjustments().size() > 0) {
            throw new XMLParseValidationException("Reaches within a reach group are not allowed to have adjustments");
        }
        
        return reach;
    }
}
