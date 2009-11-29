package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class Select implements XMLStreamParserComponent {

	private static final long serialVersionUID = -3301580483184096772L;
	public static final String MAIN_ELEMENT_NAME = "select";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	private DataSeriesType dataSeries;
	private Integer source;
	private String dataSeriesPer;
	private String partition;
	private String analyticFunction;

	public Select() {};

	public Select(DataSeriesType dataSeries) {
		this.dataSeries = dataSeries;
	}

	// ================
	// INSTANCE METHODS
	// ================
	//TODO:  There are several cases where sources are required or not permitted - should throw err.
	public Select parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
					} else if ("dataSeries".equals(localName)) {
						source = ParserHelper.parseAttribAsInt(in, "source", false);
						dataSeriesPer = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "per");
						String dataSeriesString = ParserHelper.parseSimpleElementValue(in);
						dataSeries = (dataSeriesString != null)?
								Enum.valueOf(DataSeriesType.class, dataSeriesString): null;
					} else if ("analyticFunction".equals(localName)) {
						partition = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "partition");
						analyticFunction = ParserHelper.parseSimpleElementValue(in);
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

	public void checkValidity() throws XMLParseValidationException {


		//DataSeries is required
		if (dataSeries == null) {
			throw new XMLParseValidationException(
			"A dataSeries was not found or was not recognized.  A dataSeries is always required.");
		}

		//Some series require a source
		if (dataSeries.isSourceRequired() && source == null) {
			throw new XMLParseValidationException(
				"The dataSeries '" + dataSeries + "' requires a source, " +
				"which is specified as an attribute, i.e. source=\"1\".");
		}

		//Some series cannot have a source
		if (dataSeries.isSourceDisallowed() && source != null) {
			throw new XMLParseValidationException(
				"The dataSeries '" + dataSeries + "' does not allow a source.");
		}

		//Some series do not allow any type of aggregate or post analysis.
		//Error estimates are one example.
		if (dataSeries.isAnalysisDisallowed() && analyticFunction != null) {
			throw new XMLParseValidationException(
				"The dataSeries '" + dataSeries + "' does not allow analytic functions.");
		}

	}

	public boolean isValid() {
		try {
			checkValidity();
			return true;
		} catch (XMLParseValidationException e) {
			return false;
		}
	}

	public boolean isWeighted() {
		return getDataSeries().isWeighted();
	}

	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}

	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Select) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(137, 1729);

		//Note: The hashcode of an enum is not repeatable
		if (dataSeries != null) {
			hash.append(dataSeries.ordinal());	//must be repeatable (thus ordinal)
		}

		hash.append(source);
		hash.append(dataSeriesPer);
		hash.append(partition);
		hash.append(analyticFunction);

		return hash.toHashCode();

	}

	// =================
	// GETTERS & SETTERS
	// =================
	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public String getAnalyticFunction() {
		return analyticFunction;
	}

	public DataSeriesType getDataSeries() {
		return dataSeries;
	}

	public String getDataSeriesPer() {
		return dataSeriesPer;
	}

	public String getPartition() {
		return partition;
	}

	public Integer getSource() {
		return source;
	}

}
