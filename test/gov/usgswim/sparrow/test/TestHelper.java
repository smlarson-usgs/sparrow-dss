package gov.usgswim.sparrow.test;

import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

public abstract class TestHelper {

	/**
	 * Convenience method for reading the contents of an input stream to a
	 * String, ignoring errors.
	 * 
	 * @param is
	 * @return
	 */
	public static String readToString(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
	
		StringBuffer sb = new StringBuffer();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception ex) {
			ex.getMessage();
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}

	/**
	 * Retrieves value for first appearance of attribute in xml. Beware this is
	 * regexp parsing, not true xml parsing, so will pickup values in comments.
	 * 
	 * @param xml
	 * @param attributeName
	 * @return
	 */
	public static String getAttributeValue(String xml, String attributeName) {
		assert(attributeName != null): "attribute name required!";
		Pattern patt = Pattern.compile(attributeName + "=\"([^\"]+)\"");
		Matcher m = patt.matcher(xml);
		boolean isFound = m.find();
		if (isFound) {
			return m.group(1);
		} else {
			System.err.println("Unable to extract attribute attributeName from xml");
			return null;
		}
	}
	
	/**
	 * Sets value for first appearance of attribute in xml. Beware this is
	 * regexp parsing, not true xml parsing, so will pickup values in comments.
	 * 
	 * @param xml
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	public static String setAttributeValue(String xml, String attributeName, String attributeValue) {
		assert(attributeName != null): "attribute name required!";
		Pattern patt = Pattern.compile(attributeName + "=\"[^\"]+\"");
		Matcher m = patt.matcher(xml);
		boolean isFound = m.find();
		if (isFound) {
			return m.replaceFirst(attributeName + "=\"" + attributeValue + "\"");
		}
		return xml;
	}
	
	/**
	 * Sets value for first appearance of element in xml. Beware this is
	 * regexp parsing, not true xml parsing, so will pickup values in comments.
	 * 
	 * @param xml
	 * @param elementName
	 * @param elementValue
	 * @return
	 */
	public static String setElementValue(String xml, String elementName, String elementValue) {
		assert(elementName != null): "element name required!";
		Pattern patt = Pattern.compile("<" + elementName + ">" + "[^<]+" + "</" + elementName);
		Matcher m = patt.matcher(xml);
		boolean isFound = m.find();
		if (isFound) {
			return m.replaceFirst("<" + elementName + ">" + elementValue + "</" + elementName);
		}
		return xml;
	}
	
	/**
	 * Retrieves value for first appearance of element in xml. Beware this is
	 * regexp parsing, not true xml parsing, so will pickup values in comments.
	 * 
	 * @param xml
	 * @param elementName
	 * @return
	 */
	public static String getElementValue(String xml, String elementName) {
		assert(elementName != null): "element name required!";	
		return StringUtils.substringBetween(xml, "<" + elementName + ">", "</" + elementName + ">" );
	}
	
	/**
	 * Convenience method for returning a string from a pipe request call.
	 * 
	 * @param req
	 * @param pipe
	 * @return
	 * @throws Exception
	 */
	public static String pipeDispatch(PipelineRequest req, Pipeline pipe) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pipe.dispatch(req, out);
		String response = out.toString();
		return response;
	}
	
	/**
	 * Convenience method to test for hashCode() equality between 2, optionally
	 * three instances of an object
	 * 
	 * @param obj1
	 * @param obj2
	 * @param obj1Clone
	 */
	public static void testHashCode(Object... objects) {
		for (int i=0; i<objects.length; i++) {
			String message = "hashCode comparison failed for item " + i;
			TestCase.assertEquals(message, objects[0].hashCode(), objects[i].hashCode());
		}
	}

}
