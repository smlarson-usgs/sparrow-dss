package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A super class for Analysis to allow Advanced and Basic types of Analysis.
 * 
 * @author eeverman
 *
 */
public abstract class Analysis implements XMLStreamParserComponent {

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected String groupBy;
	protected String aggFunction;

	public Analysis() {
		super();
	}
	
	public Analysis(String groupBy, String aggFunction) {
		super();
		this.groupBy = groupBy;
		this.aggFunction = aggFunction;
	}
	
	/**
	 * Utility method to parse Analysis instances w/o needing to known what type
	 * they are.  Primarily used for testing.
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 * @throws XMLParseValidationException
	 */
	public static Analysis parseAnyAnalysis(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {
		
		String localName = in.getLocalName();
		
		if (AdvancedAnalysis.isTargetMatch(localName)) {
			AdvancedAnalysis a = new AdvancedAnalysis();
			try {
				a.parse(in);
			} catch (XMLParseValidationException e) {
				//Continue, ignoring a validation exception
			}
			return a;
		} else if (BasicAnalysis.isTargetMatch(localName)) {
			BasicAnalysis a = new BasicAnalysis();
			try {
				a.parse(in);
			} catch (XMLParseValidationException e) {
				//Continue, ignoring a validation exception
			}
			return a;
		} else {
			throw new XMLParseValidationException("tag <" + localName + "> not a valid type of analysis.");
		}
		
	}
	
	/**
	 * Parses the GroupBy Element.
	 * Does no checking to determine if the correct element was passed and
	 * does not advance the parsing.
	 * @param in
	 * @throws XMLStreamException
	 */
	protected void parseGroupBy(XMLStreamReader in) throws XMLStreamException {
		aggFunction = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "aggFunction");
		groupBy = ParserHelper.parseSimpleElementValue(in);
	}
	
	
	public Integer getId() {
		return hashCode();
	}

	public boolean isAggregated() {
		return getGroupBy() != null && !"".equals(getGroupBy());
	}

	/**
	 * Convenience method for determining if the data series referenced by
	 * this {@code Analysis} object requires a weighting be applied.
	 *
	 * TODO:  This method does not really indicate the correct meaning:
	 * weighting is multiplying (i.e., a larger area counts more).  Currently
	 * this is being used to indicate that it is divided (per) by something.
	 * See the terminology and values available in the predict context schema
	 * on the AdvancedDataSeries element.
	 * 
	 * @return {@code true} if the data series referenced by this
	 *         {@code Analysis} object requires a weighting, {@code false}
	 *         otherwise.
	 *         
	 *
	 */
	public abstract boolean isWeighted();

	public boolean hasGroupBy() {
		return groupBy != null && groupBy.length() > 0;
	}

	/**
	 * Returns the grouping level, typically a HUC.
	 * TODO: THis should really be an enum w/ a NONE default value.
	 * @return
	 */
	public String getGroupBy() {
		return groupBy;
	}
	
	/**
	 * The function (min, max, etc) by which grouping is done.
	 * Should be one of type AggregateType, but that is currently in another
	 * package.
	 * @return
	 */
	public String getAggFunction() {
		return aggFunction;
	}
	
	@Override
	public void checkValidity() throws XMLParseValidationException {
		if (groupBy != null && aggFunction == null) {
			throw new XMLParseValidationException(
					"An aggFunction attribute must be specified if a groupBy" +
					"  element is present.");
		}
	}
	
	@Override
	public boolean isValid() {
		try {
			checkValidity();
			return true;
		} catch (XMLParseValidationException e) {
			return false;
		}
		
	}
	
	/**
	 * Creates a unique and repeatable hash for just the fields within the
	 * Analysis class.  Subclasses should either include these fields or
	 * include the hash value from this function.
	 */
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(137, 1729).
		append(groupBy).
		append(aggFunction).
		toHashCode();
		return hash;
	}
	
	/**
	 * The dataseries this Analysis is based on.
	 * The AdvancedAnalysis will nest the actual dataseries father down the
	 * hierarchy while the BasicAnalysis stores it directly, so this API
	 * unifies them here.
	 * @return A DataSeriesType
	 */
	abstract public DataSeriesType getDataSeries();
	
	/**
	 * The source this Analysis is based on.
	 * The AdvancedAnalysis will nest the actual source father down the
	 * hierarchy while the BasicAnalysis stores it directly, so this API
	 * unifies them here.
	 * @return An integer indicating which source (if any) the data is based on.
	 * Null if not applicable.
	 */
	abstract public Integer getSource();
	
	@Override
	abstract public Analysis clone() throws CloneNotSupportedException;
	
	/**
	 * Returns a clone of this instance with no source specified.
	 * 
	 * This is used for creating the no-source context when calculating the
	 * source shares comparison.
	 * 
	 * @return
	 * @throws CloneNotSupportedException If the dataseries requires a source.
	 */
	abstract public Analysis getNoSourceClone() throws CloneNotSupportedException;

	/**
	 * Returns true if the state represented by this object
	 * is likely to be reused by multiple clients (Advanced analysis never is,
	 * Basic Analysis may be).
	 * 
	 * @return 
	 */
	abstract public boolean isLikelyReusable();

}