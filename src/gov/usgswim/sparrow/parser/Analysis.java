package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.XMLStreamParserComponent;

import java.io.Serializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class Analysis implements XMLStreamParserComponent, Serializable, Cloneable{

	private static final long serialVersionUID = 6047046812440162869L;
	private static final String GROUP_BY_CHILD = "group-by";
	private static final String LIMIT_TO_CHILD = "limit-to";
	public static final String MAIN_ELEMENT_NAME = "analysis";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private String groupBy;
	private String limitTo;
	private Integer id;
	private Select select;

	// ================
	// INSTANCE METHODS
	// ================
	public Analysis parse(XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : 
			this.getClass().getSimpleName()
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
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						String idString = in.getAttributeValue(DEFAULT_NS_PREFIX, XMLStreamParserComponent.ID_ATTR);
						id = (idString == null)? null: Integer.valueOf(idString);
					} else if ("select".equals(localName)) {
						Select selectElement = new Select();
						selectElement.parse(in);
						this.select = selectElement;
					} else if (LIMIT_TO_CHILD.equals(localName)) {
						limitTo = ParserHelper.parseSimpleElementValue(in);
					} else if (GROUP_BY_CHILD.equals(localName)) {
						groupBy = ParserHelper.parseSimpleElementValue(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						return this; // we're done
					}
					// otherwise, error
					throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	@Override
	public int hashCode() {
		int hash = new HashCodeBuilder(137, 1729).
		append(groupBy).
		append(limitTo).
		append(id).
		append(select)
		.toHashCode();
		return hash;
	}	
	
	@Override
	public Analysis clone() throws CloneNotSupportedException {
		Analysis myClone = new Analysis();
		myClone.groupBy = groupBy;
		myClone.limitTo = limitTo;
		myClone.id = id;
		myClone.select = select;
		return myClone;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public String getLimitTo(){
		return limitTo;
	}
	
	public String getGroupBy(){
		return groupBy;
	}

	public Integer getId() {
		return id;
	}
}
