package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.XMLConstants;
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
public class BasicAnalysis extends Analysis {

	private static final long serialVersionUID = 1L;
	private static final String GROUP_BY_CHILD = "groupBy";
	private static final String DATA_SERIES = "dataSeries";
	
	/**
	 * The element name doesn't match the class name b/c we want the XML to be
	 * as simple as possible for users.
	 */
	public static final String MAIN_ELEMENT_NAME = "analysis";

	//TODO:  Do we need a default?  If so, this wouldn't be it.
	public static final AdvancedAnalysis DEFAULT_TOTAL_INSTANCE = new AdvancedAnalysis(new Select(DataSeriesType.total));

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}


	public BasicAnalysis() {};

	public static AdvancedAnalysis getDefaultTotalAnalysis() {
		return DEFAULT_TOTAL_INSTANCE;
	}

	public static BasicAnalysis parseStream(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
		BasicAnalysis anal = new BasicAnalysis();
		return anal.parse(in);
	}

	private Integer id;
	private DataSeriesType dataSeries;
	private Integer source;
	private ComparisonType nominalComparison = ComparisonType.none;

	// ================
	// INSTANCE METHODS
	// ================
	public BasicAnalysis parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
					} else if (DATA_SERIES.equals(localName)) {
						source = ParserHelper.parseAttribAsInt(in, "source", false);
						String dataSeriesString = ParserHelper.parseSimpleElementValue(in);
						dataSeries = (dataSeriesString != null)?
								Enum.valueOf(DataSeriesType.class, dataSeriesString): null;
						
					} else if (GROUP_BY_CHILD.equals(localName)) {
						parseGroupBy(in);
					} else if ("nominalComparison".equals(localName)) {
						String type = ParserHelper.parseSimpleElementValue(in);

						if (type != null) {
							try {
								nominalComparison = ComparisonType.valueOf(type);
							} catch (IllegalArgumentException e) {
								throw new XMLParseValidationException("The nominalComparison type '" + type + "' is unrecognized");
							}
						}
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
	
	public DataSeriesType getDataSeries() {
		return dataSeries;
	}
	
	@Override
	public boolean isWeighted() {
		return dataSeries.isWeighted();
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
		if (obj instanceof BasicAnalysis) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {
		if (id == null) {
			HashCodeBuilder hash = new HashCodeBuilder(137, 1729);
			
			//Note: The hashcode of an enum is not repeatable
			if (dataSeries != null) {
				hash.append(dataSeries.ordinal());	//must be repeatable (thus ordinal)
			}
			
			hash.append(source);
			hash.append(nominalComparison.ordinal());
			hash.append(super.hashCode());
			
			id = hash.toHashCode();
		}
		return id;
	}

	@Override
	public BasicAnalysis clone() throws CloneNotSupportedException {
		BasicAnalysis myClone = new BasicAnalysis();
		myClone.groupBy = groupBy;
		myClone.aggFunction = aggFunction;
		myClone.dataSeries = dataSeries;
		myClone.source = source;
		myClone.nominalComparison = nominalComparison;
		return myClone;
	}
	
	@Override
	public void checkValidity() throws XMLParseValidationException {
		super.checkValidity();
	}

	@Override
	public Integer getSource() {
		return source;
	}
	
	@Override
	public ComparisonType getNominalComparison() {
		return nominalComparison;
	}

}
