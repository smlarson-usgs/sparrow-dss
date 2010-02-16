package gov.usgswim.sparrow;

import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.PredictionContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

public abstract class TestHelper {
	
	/** The package containing standard requests and resources for tests */
	public static final String SHARED_TEST_RESOURCE_PACKAGE = "gov/usgswim/sparrow/test/shared/";

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
	 * Returns the specified file, which must exist in the package specified
	 * by SHARED_TEST_RESOURCE_PACKAGE.
	 * 
	 * @param fileName Just the file name and extension.
	 * @return
	 * @throws IOException
	 */
	public static String getSharedTestResource(String fileName) throws IOException {
		return getAnyResource(SHARED_TEST_RESOURCE_PACKAGE + fileName);
	}
	
	/**
	 * Returns the specified xml file as an XMLStreamReader.  The file must
	 * exist in the package specified by SHARED_TEST_RESOURCE_PACKAGE.
	 * 
	 * @param fileName Just the file name and extension.
	 * @return
	 * @throws IOException
	 */
	public static XMLStreamReader getSharedXMLAsReader(String fileName)
			throws Exception {
		return getAnyXMLAsReader(SHARED_TEST_RESOURCE_PACKAGE + fileName);
	}
	
	
	/**
	 * Returns the content of the specified file on the classpath, as a string.
	 * 
	 * The fullPath must be spec'ed in the format:
	 * <path>my.package.file_name</path>
	 * or in the case more likely case that the file name contains a 'dot':
	 * <path>/my/package/file_name.xml</path>
	 * 
	 * @param fullPath Full 'getResourceAsStream' compliant path to a file.
	 * @return
	 * @throws IOException
	 */
	public static String getAnyResource(String fullPath) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().
			getResourceAsStream(fullPath);
		
		String content = readToString(is);
		return content;
	}
	
	/**
	 * Returns an XMLStreamReader for any classpath xml resource.
	 * 
	 * The fullPath must be spec'ed in the format:
	 * <path>my.package.file_name</path>
	 * or in the case more likely case that the file name contains a 'dot':
	 * <path>/my/package/file_name.xml</path>
	 * 
	 * @param fullPath
	 * @return
	 * @throws Exception
	 */
	public static XMLStreamReader getAnyXMLAsReader(String fullPath)
			throws Exception {
		
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		String xml = getAnyResource(fullPath);
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(xml));
		return reader;
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
	 * Loads a text file matched to the passed class,
	 * optionally replacing '$' enclosed parmeters.
	 * 
	 * Text file names are expected to derived as follows:
	 * my/package/My.class with the nameSuffix of 'request_1.xml' would result in
	 * loading the file:
	 * <code>my/package/My_request_1.xml</code>
	 * <br><br>
	 * params are passed in serial pairs as {"name1", "value1", "name2",
	 * "value2"}. toString is called on each item, so it is OK to pass in
	 * autobox numerics. See the DataLoader.properties file for the names of the
	 * parameters available for the requested query.
	 *
	 * @param nameSuffix
	 *            Name chunk tacked onto the end of the class name.
	 * @param clazz
	 * @param params
	 *            An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 *
	 * TODO move this to a utils class of some sort
	 */
	public static String getAnyResourceWithSubstitutions(String nameSuffix,
			Class<?> clazz, Object... params) throws IOException {
		
		String path = clazz.getName().replace('.', '/');

		path = path + "_" + nameSuffix;
		
		InputStream is = Thread.currentThread().getContextClassLoader().
				getResourceAsStream(path);
		
		String content = readToString(is);

		for (int i=0; i<params.length; i+=2) {
			String n = "$" + params[i].toString() + "$";
			String v = params[i+1].toString();

			content = StringUtils.replace(content, n, v);
		}

		return content;
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
