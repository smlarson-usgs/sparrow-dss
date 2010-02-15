package gov.usgswim.sparrow;

import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
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
		}
		System.err.println("Unable to extract attribute attributeName from xml");
		return null;
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
	 * Opens an InputStream to the specified resource fild.
	 * 
	 * The file is assumed to have the same name as the passed class,
	 * but with specified extension.  An additional name suffix may be added to
	 * allow multiple files for the same class.
	 * 
	 * Example 1: If the class is named <code>com.foo.MyClass</code>,
	 * the file <code>com/foo/MyClass.[passed extension]</code> would be read.
	 * 
	 * Example 2: If the class is named <code>com.foo.MyClass</code>, the
	 * extension "tab", and the name suffix 'file1' is passed,
	 * the file <code>com/foo/MyClass_file1.tab</code> would be read.  Note the
	 * automatic addition of the underscore.
	 * 
	 * @param forClass The class for which to look for a similar named resource.
	 * @param fileSuffix A name fragment added to the end of the class name w/ an underscore.
	 * @param fileExtension The file extension (after the dot, don't include the dot) of the file.
	 * @return An inputstream from the specified file.
	 * @throws IOException
	 */
	public static InputStream getResource(Class<?> forClass, String fileSuffix, String fileExtension) throws IOException {
		Properties props = new Properties();

		String basePath = forClass.getName().replace('.', '/');
		if (fileSuffix != null) {
			basePath = basePath + "_" + fileSuffix;
		}
		basePath = basePath + "." + fileExtension;
		
		InputStream is = Thread.currentThread().getContextClassLoader().
				getResourceAsStream(basePath);
		
		return is;
	}
	
	/**
	 * Loads any type of text resource file as a string.
	 * 
	 * The file is assumed to have the same name as the passed class,
	 * but with specified extension.  An additional name suffix may be added to
	 * allow multiple files for the same class.
	 * 
	 * Example 1: If the class is named <code>com.foo.MyClass</code>,
	 * the file <code>com/foo/MyClass.[passed extension]</code> would be read.
	 * 
	 * Example 2: If the class is named <code>com.foo.MyClass</code>, the
	 * extension "tab", and the name suffix 'file1' is passed,
	 * the file <code>com/foo/MyClass_file1.tab</code> would be read.  Note the
	 * automatic addition of the underscore.
	 * 
	 * @param forClass The class for which to look for a similar named resource.
	 * @param fileSuffix A name fragment added to the end of the class name w/ an underscore.
	 * @param fileExtension The file extension (after the dot, don't include the dot) of the file.
	 * @return A string loaded from the specified file.
	 * @throws IOException
	 */
	public static String getFileAsString(Class<?> forClass, String fileSuffix, String fileExtension) throws IOException {
		
		InputStream is = getResource(forClass, fileSuffix, fileExtension);
		
		String xml = readToString(is);
		return xml;
	}
	
	/**
	 * Loads an xml resource file as a string.
	 * 
	 * The xml file is assumed to have the same name as the passed class,
	 * but with a '.xml' extension.  An additional name suffix may be added to
	 * allow multiple files for the same class.
	 * 
	 * Example 1: If the class is named <code>com.foo.MyClass</code>,
	 * the file <code>com/foo/MyClass.xml</code> would be read.
	 * 
	 * Example 2: If the class is named <code>com.foo.MyClass</code> and the
	 * name suffix 'file1' is passed,
	 * the file <code>com/foo/MyClass_file1.xml</code> would be read.  Note the
	 * automatic addition of the underscore.
	 * 
	 * @param forClass The class for which to look for a similar named xml resource.
	 * @param fileSuffix A name fragment added to the end of the class name w/ an underscore.
	 * @return An xml string loaded from the xml file.
	 * @throws IOException
	 */
	public static String getXmlAsString(Class<?> forClass, String fileSuffix) throws IOException {
		
		InputStream is = getResource(forClass, fileSuffix, "xml");
		
		String xml = readToString(is);
		return xml;
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
