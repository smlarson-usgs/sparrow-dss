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
	
	
  public ReachGroup parse(XMLStreamReader in) throws XMLStreamException,
      XMLParseValidationException {

		ReachGroup group = super.parse(in);
	  
		if (group.getName() != null) {
			throw new XMLParseValidationException("The default-group does not allow a name");
		}
		
		if (group.getReaches().size() > 0) {
			throw new XMLParseValidationException("The default-group does not allow individual reaches to be specified");
		}
		
		//TODO:  Once logical sets are added, we need to check for their presense here....
		
		return group;
  }
  
  public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
  }
	
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
}
