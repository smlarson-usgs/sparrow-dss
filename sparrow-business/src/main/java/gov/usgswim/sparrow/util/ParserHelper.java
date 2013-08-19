package gov.usgswim.sparrow.util;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

public abstract class ParserHelper {
	public static final String PARSE_NUMBER_ERROR_MESSAGE ="The '%s' attribute for element '%s' must be %s but was %s";
	public static final String MUST_EXIST_ERROR_MESSAGE = "The '%s' attribute is required for element '%s'";

	public static String parseSimpleElementValue(XMLStreamReader in) throws XMLStreamException {
		assert(in.getEventType() == START_ELEMENT): "only start elements accepted";
		int currentEvent = in.next();

		String elementValue = null;
		if (currentEvent == CHARACTERS) {
			elementValue = in.getText();
			currentEvent = in.next();
		}
		assert(currentEvent == END_ELEMENT): "should now be the end element";

		return elementValue;
	}

	/**
	 * Ignores the current tag element by consuming the stream until the corresponding end tag.
	 * This is not namespace nor context-sensitive. TODO make it so
	 *
	 * @param in
	 * @throws XMLStreamException
	 */
	public static void ignoreElement(XMLStreamReader in) throws XMLStreamException {
		assert(in.getEventType() == START_ELEMENT): "only start elements accepted";
		String ignoredElement = in.getLocalName();
		int currentEvent = in.getEventType();
		String currentElement = in.getLocalName();
		while (!currentElement.equals(ignoredElement) || currentEvent != END_ELEMENT) {
			currentEvent = in.next();
			currentElement = (START_ELEMENT == currentEvent || END_ELEMENT == currentEvent) ? in.getLocalName(): "invalid tag";
		}
		assert(currentElement.equals(ignoredElement) && currentEvent == END_ELEMENT);
	}

	/**
	 * Parses to the end tag. Useful for cleaning up when doing stream parsing.
	 *
	 * @param in
	 * @throws XMLStreamException
	 */
	public static void parseToEndTag(XMLStreamReader in, String... endTags) throws XMLStreamException {
		assert(endTags != null): "at least one endTag must be specified";
		int currentEvent = in.getEventType();
		String currentElement = (currentEvent == END_ELEMENT )? in.getLocalName(): "";
		while (!isInArray(endTags, currentElement) || currentEvent != END_ELEMENT) {
			currentEvent = in.next();
			currentElement = (currentEvent == END_ELEMENT )? in.getLocalName(): "";
		}
		assert(isInArray(endTags, currentElement) && currentEvent == END_ELEMENT);

	}

	/**
	 * Parses to the start tag. Useful for fast forwarding to important part of the stream.
	 *
	 * @param in
	 * @throws XMLStreamException
	 */
	public static void parseToStartTag(XMLStreamReader in, String startTag) throws XMLStreamException {
		assert(startTag != null && startTag.length() > 0): "a startTag must be specified";
		int currentEvent = in.getEventType();
		String currentElement = (currentEvent == START_ELEMENT)? in.getLocalName(): "";
		while ( currentEvent != XMLStreamConstants.START_ELEMENT || !currentElement.equals(startTag) ) {
			currentEvent = in.next();
			currentElement = (currentEvent == START_ELEMENT)? in.getLocalName(): "";
		}
		assert(currentElement.equals(startTag) && currentEvent == START_ELEMENT);
	}

	/**
	 * Parses to the nearest start tag, which may be the current stream event
	 * @param in
	 * @return true if the streamReader has advanced
	 * @throws XMLStreamException if past the end of stream
	 */
	public static boolean parseToStartTag(XMLStreamReader in) throws XMLStreamException {
		int currentEvent = in.getEventType();

		// we're already on a start tag or at end of stream, so don't advance
		if (currentEvent == START_ELEMENT || !in.hasNext()) return false;

		while ( currentEvent != XMLStreamConstants.START_ELEMENT && in.hasNext() ) {
			currentEvent = in.next();
		}
		return true;
	}

	/**
	 * Determines whether a given string is in the String array.
	 *
	 * @param stringList
	 * @param item
	 * @return
	 */
	public static boolean isInArray(String[] stringList, String item) {
		for (String listElement: stringList) {
			if (listElement.equals(item)) return true;
		}
		return false;
	}

	/**
   * Returns the integer value found in the specified attribute of the current
   * element.  If require is true and the attribute does not exist, an error
   * is thrown.
   * @param reader
   * @param attrib
   * @return
   * @throws Exception
   */
  public static int parseAttribAsInt(
  		XMLStreamReader reader, String attrib) throws XMLStreamException {

  	return parseAttribAsLong(reader, attrib, true).intValue();
  }

	/**
   * Returns the Integer value found in the specified attribute of the current
   * element.  If require is true and the attribute does not exist, an error
   * is thrown.  If the attribute does not exist or is empty and require is
   * not true, null is returned.
   * @param reader
   * @param attrib
   * @param require
   * @return
   * @throws Exception
   */
  public static Integer parseAttribAsInt(
  		XMLStreamReader reader, String attrib, boolean require) throws XMLStreamException {

  	Long v = parseAttribAsLong(reader, attrib, require);
  	if (v != null) {
  		return v.intValue();
  	}
  	return null;
  }

	/**
   * Returns the Integer value found in the specified attribute of the current
   * element.  If the attribute does not exist, the default value is returned.
   *
   * @param reader
   * @param attrib
   * @param defaultVal Returned if the specified attribute does not exist.
   * @return
   * @throws Exception If the attribute cannot be parsed to the appropriate type.
   */
  public static Integer parseAttribAsInt(
  		XMLStreamReader reader, String attrib, Integer defaultVal) throws XMLStreamException {

  	Long val = parseAttribAsLong(reader, attrib, (long) defaultVal);
  	if (val != null) {
  		return val.intValue();
  	}
  	return null;
  }

	/**
   * Returns the double value found in the specified attribute of the current
   * element.  If the attribute does not exist or cannot be parsed as a number,
   * an error is thrown.
   *
   * @param reader
   * @param attrib
   * @return
   * @throws Exception
   */
  public static double parseAttribAsDouble(
  		XMLStreamReader reader, String attrib) throws XMLStreamException {

  	return parseAttribAsDouble(reader, attrib, true);
  }

	/**
   * Returns the Double value found in the specified attribute of the current
   * element.  If require is true and the attribute does not exist, an error
   * is thrown.  If the attribute does not exist or is empty and
   * require is not true, null is returned.
   * @param reader
   * @param attrib
   * @param require
   * @return
   * @throws Exception
   */
  public static Double parseAttribAsDouble(
  		XMLStreamReader reader, String attrib, boolean require) throws XMLStreamException {

  	String val = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );

  	if (val != null) {
  		try {
  			return Double.parseDouble(val);
  		} catch (Exception e) {
  			throw new XMLStreamException(String.format(PARSE_NUMBER_ERROR_MESSAGE, attrib, reader.getLocalName(), "a number", val));
  		}

  	} else if (require) {
  		throw new XMLStreamException("The '" + attrib + "' attribute must exist for element '" + reader.getLocalName() + "'");
  	} else {
  		return null;
  	}

  }

	/**
   * Returns the Double value found in the specified attribute of the current
   * element.  If the attribute does not exist, the default value is returned.
   *
   * @param reader
   * @param attrib
   * @param defaultVal Returned if the specified attribute does not exist.
   * @return
   * @throws Exception If the attribute cannot be parsed to the appropriate type.
   */
  public static Double parseAttribAsDouble(
		  XMLStreamReader reader, String attrib, Double defaultVal) throws XMLStreamException {

	  String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );

	  if (v != null) {
		  try {
			  return Double.parseDouble(v);
		  } catch (Exception e) {
			  throw new XMLStreamException(String.format(PARSE_NUMBER_ERROR_MESSAGE, attrib, reader.getLocalName(), "a number", v));
		  }

	  }
	  return defaultVal;
  }

	/**
   * Returns the long value found in the specified attribute of the current
   * element.  If the attribute does not exist or cannot be parsed as a number,
   * an error is thrown.
   *
   * @param reader
   * @param attrib
   * @return
   * @throws Exception
   */
  public static long parseAttribAsLong(
  		XMLStreamReader reader, String attrib) throws XMLStreamException {

  	return parseAttribAsLong(reader, attrib, true);
  }

	/**
   * Returns the Long value found in the specified attribute of the current
   * element.  If require is true and the attribute does not exist, an error
   * is thrown.  If the attribute does not exist or is empty and
   * require is not true, null is returned.
   * @param reader
   * @param attrib
   * @param require
   * @return
   * @throws Exception
   */
  public static Long parseAttribAsLong(
  		XMLStreamReader reader, String attrib, boolean require) throws XMLStreamException {

  	String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );

  	if (v != null) {
  		try {
  			return Long.parseLong(v);
  		} catch (Exception e) {
  			;
  			throw new XMLStreamException(String.format(PARSE_NUMBER_ERROR_MESSAGE, attrib, reader.getLocalName(), "an integer", v));
  		}

  	} else if (require) {
  		throw new XMLStreamException(String.format(MUST_EXIST_ERROR_MESSAGE, attrib, reader.getLocalName()));
  	} else {
  		return null;
  	}

  }

	/**
   * Returns the Long value found in the specified attribute of the current
   * element.  If the attribute does not exist, the default value is returned.
   *
   * @param reader
   * @param attrib
   * @param defaultVal Returned if the specified attribute does not exist.
   * @return
   * @throws Exception If the attribute cannot be parsed to the appropriate type.
   */
  public static Long parseAttribAsLong(
		  XMLStreamReader reader, String attrib, Long defaultVal) throws XMLStreamException {

	  String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );

	  if (v != null) {
		  try {
			  return Long.parseLong(v);
		  } catch (Exception e) {
			  throw new XMLStreamException(String.format(PARSE_NUMBER_ERROR_MESSAGE, attrib, reader.getLocalName(), "an integer", v));
		  }

	  }
	  return defaultVal;
  }

	/**
   * Returns the String found in the specified attribute of the current
   * element.  If the attribute does not exist or is empty, an error is thrown.
   *
   * @param reader
   * @param attrib
   * @return
   * @throws Exception
   */
  public static String parseAttribAsString(
  		XMLStreamReader reader, String attrib) throws XMLStreamException {

  	return parseAttribAsString(reader, attrib, true);
  }

	/**
   * Returns the String found in the specified attribute of the current
   * element.  If require is true and the attribute does not exist, an error
   * is thrown.  If the attribute does not exist or is empty and
   * require is not true, null is returned.
   * @param reader
   * @param attrib
   * @param require
   * @return
   * @throws Exception
   */
  public static String parseAttribAsString(
  		XMLStreamReader reader, String attrib, boolean require) throws XMLStreamException {

  	String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );

  	if (v != null && ! ("".equals(v))) {
  		return v;
  	} else if (require) {
  		throw new XMLStreamException(String.format(MUST_EXIST_ERROR_MESSAGE, attrib, reader.getLocalName()));
  	} else {
  		return null;
  	}

  }

  /**
   * Returns the String value found in the specified attribute of the current
   * element.  If the attribute does not exist, the default value is returned.
   *
   * @param reader
   * @param attrib
   * @param defaultVal Returned if the specified attribute does not exist or is empty.
   * @return
   */
  public static String parseAttribAsString(
		  XMLStreamReader reader, String attrib, String defaultVal) {

	  String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );

	  if (v != null && ! ("".equals(v))) {
		  return v;
	  }
	  return defaultVal;
  }
  
  /**
   * Returns the String value found in the specified attribute of the current
   * element.  If the attribute does not exist, the default value is returned.
   *
   * @param reader
   * @param attrib
   * @param defaultVal Returned if the specified attribute does not exist or is empty.
   * @return
   */
  public static Boolean parseAttribAsBoolean(
		  XMLStreamReader reader, String attrib, Boolean defaultVal) {

	  String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );

	  if (v != null) {
		  //'true' or '1' comes from the xsd spec for boolean, which is what
		  //is used to define the communication.
		  return ("true".equals(v) || "1".endsWith(v));
	  } else {
		  return defaultVal;
	  }
  }
}
