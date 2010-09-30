package gov.usgswim.sparrow;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.action.LoadModelPredictDataFromFile;
import gov.usgswim.sparrow.cachefactory.PredictDataFactory;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultImm;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.Before;

public abstract class SparrowUnitTest {
	
	/** Logging.  Log messages will use the name of the subclass */
	protected static Logger log =
		Logger.getLogger(SparrowUnitTest.class); //logging for this class
	
	/** lifecycle listener handles startup / shutdown */
	static LifecycleListener lifecycle = new LifecycleListener();
	
	
	/** The model ID of MRB2 in the test db */
	public static final Long TEST_MODEL_ID = 50L;
	
	//Files associated w/ the test model
	public static final String SOURCE_METADATA_FILE = "src_metadata.txt";
	public static final String TOPO_FILE = "topo.txt";
	public static final String SOURCE_COEF_FILE = "coef.txt";
	public static final String SOURCE_VALUES_FILE = "src.txt";
	public static final String PREDICT_RESULTS_FILE = "predict.txt";
	
	/** The package containing standard requests and resources for tests */
	public static final String SHARED_TEST_RESOURCE_PACKAGE = "gov/usgswim/sparrow/test/shared/";
	public static final String BASE_MODEL_FILE_PATH =
		SHARED_TEST_RESOURCE_PACKAGE + "model50/";
	
	private static PredictResult testModelPredictResults;
	private static PredictData testModelPredictData;
	
	//True until the firstRun is complete (used for onetime init)
	private static boolean firstRun = true;
	
	/** A single instance which is destroyed in teardown */
	private static SparrowUnitTest singleInstanceToTearDown;

	
	//Cannot use the @BeforeClass since we need the ability to override methods.
	@Before
	public void SparrowUnitTestSetUp() throws Exception {
		if (firstRun) {
			doOneTimeSetup();
			firstRun = false;
			singleInstanceToTearDown = this;
		}
	}
	

	@AfterClass
	public static void SparrowUnitTestTearDown() throws Exception {
		singleInstanceToTearDown.doOneTimeTearDown();
	}
	
	protected void doOneTimeSetup() throws Exception {
		doOneTimeLogSetup();
		doOneTimeGeneralSetup();
		doOneTimeLifecycleSetup();
		
		try {
			//The junit framework subclasses can override the FrameworkSetup,
			//allowing endpoint tests (ie the classes actually containing the tests)
			//to override CustomSetup w/o having to worry about calling super
			//(or the results of failing to call it).
			doOneTimeFrameworkSetup();	//Intended for framework subclasses (like SparrowDBTest) to override
		} catch (Exception e) {
			log.fatal("Custom test setup doOneTimeFrameworkSetup() is throwing an exception!", e);
			throw e;
		}
		
		try {
			doOneTimeCustomSetup();	//intended endpoint test subclasses to use for setup
		} catch (Exception e) {
			log.fatal("Custom test setup doOneTimeCustomSetup() is throwing an exception!", e);
			throw e;
		}
	}
	
	protected void doOneTimeTearDown() throws Exception {
		
		try {
			
			singleInstanceToTearDown.doOneTimeCustomTearDown();	//for endpoint test classes
			singleInstanceToTearDown.doOneTimeFrameworkTearDown();	//for framework test classes
		} catch (Exception e) {
			log.fatal("Custom test teardown doOneTimeCustomTearDown() is throwing an exception!", e);
		}

		singleInstanceToTearDown.doOneTimeLifecycleTearDown();
		singleInstanceToTearDown.doOneTimeGeneralTearDown();
		singleInstanceToTearDown.doOneTimeLogTearDown();
		
		singleInstanceToTearDown = null;
		firstRun = true;	//reset this flag since it shared by all instances
	}
	
	protected void doOneTimeLogSetup() throws Exception {
		setLogLevel(Level.ERROR);
	}
	
	protected void doOneTimeGeneralSetup() throws Exception {
		
		//Tell JNDI config to not expect JNDI props
		System.setProperty(
				"gov.usgs.cida.config.DynamicReadOnlyProperties.EXPECT_NON_JNDI_ENVIRONMENT",
				"true");
		
		
		//Specifies to use text files instead of loading PredictData from the DB.
		System.setProperty(
				PredictDataFactory.ACTION_IMPLEMENTATION_CLASS,
				"gov.usgswim.sparrow.action.LoadModelPredictDataFromFile");
		
		
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
	}
	
	protected void doOneTimeLifecycleSetup() throws Exception {
		lifecycle.contextInitialized(null, true);
	}

	
	/**
	 * Intended to be overridden by framework subclasses like SparrowDBTest.
	 * If frameworks use this method, endpoint subclasses do not need to call
	 * super if they use doOneTimeCustomSetup().
	 * @throws Exception
	 */
	protected void doOneTimeFrameworkSetup() throws Exception {
		//nothing to do and no need to call super if overriding.
	}
	
	/**
	 * Called only before the first test.
	 * Intended to be overridden for one-time initiation by endpoint test classes.
	 * Those methods should not need to call super() for this method.
	 * @throws Exception
	 */
	protected void doOneTimeCustomSetup() throws Exception {
		//nothing to do and no need to call super if overriding.
	}
	
	protected void doOneTimeLogTearDown() {
		//nothing to do
	}
	
	protected void doOneTimeLifecycleTearDown() {
		lifecycle.contextDestroyed(null, true);
	}
	
	
	protected void doOneTimeGeneralTearDown() {
		//nothing to do
	}
	
	protected void doOneTimeFrameworkTearDown() throws Exception {
		//Nothing to do
	}
	
	/**
	 * For endpoint sublclasses to override.
	 * implementers do not need to call super().
	 * @throws Exception
	 */
	protected void doOneTimeCustomTearDown() throws Exception {
		//nothing to do and no need to call super if overriding.
	}
	
	protected static void setLogLevel(Level level) {
		//Turns on detailed logging
		log.setLevel(level);
		
		
		//The LifecycleListener is set for info level logging by default.
		Logger.getLogger(LifecycleListener.class).setLevel(level);
		
		//Generically set level for all Actions
		Logger.getLogger(Action.class).setLevel(level);
	}
	

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
	 * Loads a serialized object from a file.
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
	public static Object getFileAsObject(Class<?> forClass, String fileSuffix,
				String fileExtension) throws Exception {
		InputStream is = getResource(forClass, fileSuffix, fileExtension);
        ObjectInputStream ois = new ObjectInputStream(is);
        Object o = ois.readObject();
        ois.close();
        return o;
	}
	
	/**
	 * Write an object instance to a specified file.
	 * This method is intended to be used for writing data instances to a file
	 * so they can be used as a nominal value for comparisons in tests via the
	 * getFileAsObject() method.
	 * 
	 * @param o
	 * @param filePath
	 * @throws Exception
	 */
	public void writeObjectToFile(Serializable o, String filePath) throws Exception {
        FileOutputStream fos = new FileOutputStream(filePath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(o);
        oos.close();
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
	
	/**
	 * Compares two datatables, returning true if they are equal.
	 * 
	 * Any mismatched values or rowIDs are logged as errors (log.error) and
	 * will cause false to be returned.
	 * 
	 * @param expected
	 * @param actual
	 * @return
	 */
	public boolean compareTables(DataTable expected, DataTable actual) {
		return compareTables(expected, actual, null, true, 0d);
	}
	
	/**
	 * Compares two datatables, returning true if they are equal.
	 * 
	 * Any mismatched values or rowIDs are logged as errors (log.error) and
	 * will cause false to be returned.
	 * 
	 * @param expected
	 * @param actual
	 * @param compareIds
	 * @param fractionalDeltaAllowed The fractional difference allowed, wrt the expected value.
	 * @return
	 */
	public boolean compareTables(DataTable expected, DataTable actual, 
			boolean compareIds, double fractionalDeltaAllowed) {
		
		return compareTables(expected, actual, null, compareIds,
				fractionalDeltaAllowed);
	}
	
	/**
	 * Compare two tables, ignoring any column indexes listed in the ignoreColumn array.
	 * Row IDs are compared if compareIds is true.
	 * 
	 * The fractionalDeltaAllowed allows small variances to be considered equal.
	 * The fractionalDeltaAllowed is multiplied by the expected value and then
	 * the actual value is allowed to be the expected value +/- the product.
	 * For an exact comparison, use the value zero.
	 * 
	 * @param expected
	 * @param actual
	 * @param ignoreColumn
	 * @param compareIds
	 * @param fractionalDeltaAllowed The fractional difference allowed, wrt the expected value.
	 * @return
	 */
	public boolean compareTables(DataTable expected, DataTable actual,
			int[] ignoreColumn, boolean compareIds, double fractionalDeltaAllowed) {
		
		boolean match = true;
		boolean checkIds = false;
		
		if (ignoreColumn == null) {
			ignoreColumn = new int[]{};
		} else {
			Arrays.sort(ignoreColumn);
		}
		
		if (compareIds) {
			if (expected.hasRowIds() && actual.hasRowIds()) {
				checkIds = true;
			} else if (expected.hasRowIds() ^ actual.hasRowIds()) {
				log.error("One table has row IDs and the other does not.");
				return false;
			}
		}
		
		for (int r = 0; r < expected.getRowCount(); r++) {
			for (int c = 0; c < expected.getColumnCount(); c++) {
				
				if (Arrays.binarySearch(ignoreColumn, c) < 0) {
					
					Object orgValue = expected.getValue(r, c);
					Object newValue = actual.getValue(r, c);
					
					if (! isEqual(orgValue, newValue, fractionalDeltaAllowed)) {
						match = false;
						log.error("Mismatch : " + r + "," + c + ") [" + orgValue + "] [" + newValue + "]");
					}
				} else {
					//skip comparison, its listed in the ignore column
				}
			}
			
			if (checkIds) {
				Long expectId = expected.getIdForRow(r);
				Long actualId = actual.getIdForRow(r);
				
				if (expectId == null && actualId == null) {
					//ok - skip
				} else if (expectId == null || actualId == null) {
					match = false;
					log.error("Mismatched ID for row " + r + " [" + expectId + "] [" + actualId + "]");
				} else if (expectId.equals(actualId)) {
					//ok - skip
				} else {
					//neither are null, but they have different values
					log.error("Mismatched ID for row " + r + " [" + expectId + "] [" + actualId + "]");
				}
			}
		}
		
		return match;
	}
	
	public static boolean isEqual(Object expected, Object actual, double fractionalDeltaAllowed) {
		if (expected instanceof Number && actual instanceof Number) {
			Double e = ((Number) expected).doubleValue();
			Double a = ((Number) actual).doubleValue();
			
			if (e == null || a == null) {
				if (e == null && a == null) {
					return true;
				} else {
					return false;
				}
			} else {
				if (fractionalDeltaAllowed == 0d) {
					return e.equals(a);
				} else {
					

					if (e.isNaN() || a.isNaN()) {
						return e.isNaN() && a.isNaN();
					} else if (e.isInfinite() || a.isInfinite() || e.equals(0d)) {
						return e.equals(a);
					}
					
					double variance = Math.abs( e * fractionalDeltaAllowed );
					double top = e + variance;
					double bot = e - variance;
					
					if (top > bot) {
						return top > a && a > bot;
					} else {
						return bot > a && a > top;
					}
				}
				
			}
		} else {
			//there seems to be a bug in the ObjectUtils class where
			//two null values are not considered equal
			if (expected == null && actual == null) {
				return true;
			} else {
				return ObjectUtils.equals(expected, actual);
			}
		}
	}
	
	/**
	 * Builds the path to the specified model resource.
	 * @param modelId
	 * @param fileName
	 * @return
	 */
	public static String getModelResourceFilePath(Long modelId, String fileName) {
		//Ignore the model - we always return model 50
		return BASE_MODEL_FILE_PATH + fileName;
	}
	
	public static synchronized PredictResult getTestModelPredictResult() throws Exception {
		if (testModelPredictResults != null) {
			return testModelPredictResults;
		} else {
			//143 columns, but treat the first one as a row ID
			final int COL_COUNT = 143 - 1;
			final int SRC_COUNT = 5;
			final int OUTPUT_COL_COUNT = (SRC_COUNT * 2) + 2;
			
			final int TOTAL_LOAD_START_COL = 34;
			final int INC_LOAD_START_COL = 46;	//DECAYED
			final int TOTAL_COL = 33;
			final int INC_TOTAL_COL = 45;	//DECAYED
			
			String[] headings = new String[COL_COUNT];
			
			//Set most columns as String
			Class<?>[] types = new Class<?>[COL_COUNT];
			Arrays.fill(types, String.class);
			
			
			//Assign types for total & inc by source
			for (int src=0; src < SRC_COUNT; src++) {
				types[src + TOTAL_LOAD_START_COL] = Double.class;
				types[src + INC_LOAD_START_COL] = Double.class;
			}
			
			//Assign types for total and inc total
			types[TOTAL_COL] = Double.class;
			types[INC_TOTAL_COL] = Double.class;
			
			
			DataTableWritable predictResults =
				new SimpleDataTableWritable(headings, null, types);
			
			String filePath = getModelResourceFilePath(TEST_MODEL_ID, PREDICT_RESULTS_FILE);
			DataTableUtils.fill(predictResults, filePath, true, "\t", true);
			
			

			
			//Removed Unused Columns & reorder columns
			ArrayList<ColumnDataWritable> cols = new ArrayList<ColumnDataWritable>();
			ColumnDataWritable[] colArray = predictResults.getColumns();
			
			//Add all the inc add columns
			for (int src=0; src < SRC_COUNT; src++) {
				cols.add(colArray[src + INC_LOAD_START_COL]);
			}
			
			//Add all the total columns
			for (int src=0; src < SRC_COUNT; src++) {
				cols.add(colArray[src + TOTAL_LOAD_START_COL]);
			}
			
			//Add the total column
			cols.add(colArray[INC_TOTAL_COL]);
			cols.add(colArray[TOTAL_COL]);
			
			//cleanup a bit
			colArray = null;
			predictResults = null;
			
			
			//!!!! This needs to be flipped so the rows are in the first dimension
			//create data array [row index][col index]
			//!!!!
			int rowCount = cols.get(0).getRowCount();
			int colCount = cols.size();
			double[][] colDatas = new double[rowCount][];
			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			
				double[] oneRowData = new double[colCount];
				for (int colIndex = 0; colIndex < colCount; colIndex++) {
					double oneVal = cols.get(colIndex).getDouble(rowIndex);
					oneRowData[colIndex] = oneVal;
				}
				colDatas[rowIndex] = oneRowData;
			}
			
			//
			//Need to convert from decayed to non-decayed inc load
			PredictData pd = getTestModelPredictData();
			
//			for (int row = 0; row < rowCount; row++) {
//				
//				double totalNonDecayed = 0d;
//				
//				for (int src=0; src < SRC_COUNT; src++) {
//					double decayedVal = colDatas[row][src];
//					double delivery = pd.getDelivery().getDouble(row, PredictData.INSTREAM_DECAY_COL);
//					double nonDecayedVal = decayedVal / delivery;
//					colDatas[row][src] = nonDecayedVal;
//					totalNonDecayed += nonDecayedVal;
//				}
//				
//				colDatas[row][(SRC_COUNT * 2)] = totalNonDecayed;
//				
//			}
			
			
			
			PredictResultImm pr = PredictResultImm.buildPredictResult(colDatas, pd);
			testModelPredictResults = pr;
			return testModelPredictResults;
			
		}
	}
	
	public static synchronized PredictData getTestModelPredictData() throws Exception {
		if (testModelPredictData != null) {
			return testModelPredictData;
		} else {
			LoadModelPredictDataFromFile action = new LoadModelPredictDataFromFile(TEST_MODEL_ID);
			testModelPredictData = action.run();
			return testModelPredictData;
		}
	}
	

}
