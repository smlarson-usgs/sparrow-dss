package gov.usgswim.sparrow.util;

/**
 * This class assumes valid, escaped content is passed in when building to StringBuilder
 * 
 * @author ilinkuo
 *
 */
public abstract class SimpleXMLBuilderHelper {
	public static void writeNonNullTag(StringBuilder in, String tagName, String tagContent) {
		if (tagContent != null) {
			in.append("<").append(tagName).append(">");
			in.append(tagContent);
			in.append("</").append(tagName).append(">");
		}
	}
	
	public static void writeNonNullAttribute(StringBuilder in, String attName, String attValue) {
		if (attValue != null) {
			in.append(" ").append(attName).append("=\"").append(attValue).append("\" ");
		}
	}
	
	/**
	 * @param in
	 * @param tagName
	 * @param attributes an even array of name-value pairs
	 */
	public static void writeOpeningTag(StringBuilder in, String tagName, String... attributes) {
		in.append("<").append(tagName);
		for (int i=0; i<attributes.length; i=i+2) {
			writeNonNullAttribute(in, attributes[i], attributes[i+1]);
		}
		in.append(">");
	}
	
	/**
	 * @param in
	 * @param tagName
	 * @param attributes an even array of name-value pairs
	 */
	public static void writeClosedFullTag(StringBuilder in, String tagName, String... attributes) {
		in.append("<").append(tagName);
		for (int i=0; i<attributes.length; i=i+2) {
			writeNonNullAttribute(in, attributes[i], attributes[i+1]);
		}
		in.append(" />");
	}
	
	public static void writeClosingTag(StringBuilder in, String tagName) {
		in.append("</").append(tagName).append(">");
	}
	
	public static String asString(Number value) {
		return (value == null)? null: value.toString();
	}
}
