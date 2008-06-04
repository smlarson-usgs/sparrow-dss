package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.Serializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Top-level child of PredictionContext.
 * 
 * This child is unique, b/c it does NOT include the modelID, which all the other
 * top-level-children do.  This is because Analysis is (almost) sharable between
 * models, so it seems reasonable to allow users to reuse a type of analysis
 * across models.  The exception to this is that source numbers vary b/t models,
 * so there is the possibility of an error, either logical or runtime.
 * @author eeverman
 *
 */
public class Analysis implements XMLStreamParserComponent {

	private static final long serialVersionUID = 6047046812440162869L;
	private static final String GROUP_BY_CHILD = "group-by";
	private static final String LIMIT_TO_CHILD = "limit-to";
	public static final String MAIN_ELEMENT_NAME = "analysis";
	
	public static final Analysis DEFAULT_TOTAL_INSTANCE = new Analysis(new Select(DataSeriesType.total));

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	
	public Analysis() {};
	
	public Analysis(Select select) {
		this.select = select;
	};
	
	public static Analysis getDefaultTotalAnalysis() {
		return DEFAULT_TOTAL_INSTANCE;
	}
	
	public static Analysis parseStream(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
		Analysis anal = new Analysis();
		return anal.parse(in);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private String groupBy;
	private String limitTo;
	private Integer id;
	private Select select;

	// ================
	// INSTANCE METHODS
	// ================
	public Analysis parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
						id = ParserHelper.parseAttribAsInt(in, XMLStreamParserComponent.ID_ATTR, false);
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
						checkValidity();
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
	
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
	
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
  public boolean equals(Object obj) {
	  if (obj instanceof Analysis) {
	  	return obj.hashCode() == hashCode();
	  } else {
	  	return false;
	  }
  }
  
	public synchronized int hashCode() {
		if (id == null) {
			int hash = new HashCodeBuilder(137, 1729).
			append(groupBy).
			append(limitTo).
			append(select).
			toHashCode();

			id = hash;
		}
		return id;
	}	
	
	@Override
	public Analysis clone() throws CloneNotSupportedException {
		Analysis myClone = new Analysis();
		myClone.groupBy = groupBy;
		myClone.limitTo = limitTo;
		myClone.select = select;
		return myClone;
	}
	
	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " is not valid");
		}
	}

	public boolean isValid() {
		return true;
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
	
	public Select getSelect(){
		return select;
	}

	public Integer getId() {
		return hashCode();
	}
}
