package gov.usgswim.sparrow.domain;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.util.ParserHelper;

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
public class AdvancedAnalysis extends Analysis {

	private static final long serialVersionUID = 1L;
	private static final String GROUP_BY_CHILD = "groupBy";
	private static final String LIMIT_TO_CHILD = "limitTo";
	public static final String MAIN_ELEMENT_NAME = "advancedAnalysis";

	public static final AdvancedAnalysis DEFAULT_TOTAL_INSTANCE = new AdvancedAnalysis(new Select(DataSeriesType.total));

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}


	public AdvancedAnalysis() {};

	public AdvancedAnalysis(Select select) {
		this.select = select;
	};

	public static AdvancedAnalysis getDefaultTotalAnalysis() {
		return DEFAULT_TOTAL_INSTANCE;
	}

	public static AdvancedAnalysis parseStream(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
		AdvancedAnalysis anal = new AdvancedAnalysis();
		return anal.parse(in);
	}

	private String limitTo;
	private Integer id;
	private Select select;

	// ================
	// INSTANCE METHODS
	// ================
	public AdvancedAnalysis parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
						parseGroupBy(in);
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new XMLParseValidationException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	@Override
	public boolean isWeighted() {
		return getSelect().isWeighted();
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
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AdvancedAnalysis) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {
		if (id == null) {
			int hash = new HashCodeBuilder(137, 1729).
			append(limitTo).
			append(select).
			append(super.hashCode()).
			toHashCode();

			id = hash;
		}
		return id;
	}

	@Override
	public AdvancedAnalysis clone() throws CloneNotSupportedException {
		AdvancedAnalysis myClone = new AdvancedAnalysis();
		myClone.groupBy = groupBy;
		myClone.aggFunction = aggFunction;
		myClone.limitTo = limitTo;
		myClone.select = select;
		return myClone;
	}
	
	@Override
	public Analysis getNoSourceClone() throws CloneNotSupportedException {
		AdvancedAnalysis myClone = new AdvancedAnalysis();
		myClone.groupBy = groupBy;
		myClone.aggFunction = aggFunction;
		myClone.limitTo = limitTo;
		myClone.select = select.getNoSourceClone();
		return myClone;
	}

	@Override
	public void checkValidity() throws XMLParseValidationException {
		super.checkValidity();
		select.checkValidity();

		//Some series do not allow any type of aggregate or post analysis.
		//Error estimates are one example.
		if (getDataSeries().isAnalysisDisallowed() && aggFunction != null) {
			throw new XMLParseValidationException(
				"The dataSeries '" + getDataSeries() +
				"' does not allow analysis like aggregation.");
		}
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public String getLimitTo(){
		return limitTo;
	}

	public Select getSelect(){
		return select;
	}


	@Override
	public DataSeriesType getDataSeries() {
		return select.getDataSeries();
	}


	@Override
	public Integer getSource() {
		return select.getSource();
	}
	
	/**
	 * Returns true if the state represented by this object
	 * is likely to be reused by multiple clients (Advanced analysis never is).
	 * 
	 * @return 
	 */
	@Override
	public boolean isLikelyReusable() {
		return false;
	}

}
