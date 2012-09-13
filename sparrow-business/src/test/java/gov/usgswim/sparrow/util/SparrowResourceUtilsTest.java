
package gov.usgswim.sparrow.util;

import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;


public class SparrowResourceUtilsTest extends XMLTestCase {

	public final String NONEXISTENT_MODEL = "-666";
	
	public final String DEFAULT_MODEL_SOURCE_1 =
			"	<Name>People</Name>" + 
			"	<Units/>" + 
			"	<Description/>";
	
	@BeforeClass
	protected void setUp() throws Exception {
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void testModelResourceFilePathReturnsNullIfAnyArgumentIsNull() {
		String result = SparrowResourceUtils.getModelResourceFilePath(null, "filename.txt");
		assertNull(result);

		result = SparrowResourceUtils.getModelResourceFilePath(1L, null);
		assertNull(result);
	}

	@Test
	public void testRetrieveValuesFromDefaultModel() throws Exception {
		String source1 = SparrowResourceUtils.lookupModelHelp("-1", "Sources.1");
		System.out.println(source1);
		assertXMLEqual(DEFAULT_MODEL_SOURCE_1, source1);
	}
	
	@Test
	public void testRetrieveCommonTerms() throws Exception {
		String help1 = SparrowResourceUtils.lookupMergedHelp("-1", "CommonTerms.MRB", "div");
		System.out.println(help1);
		//assertXMLEqual(DEFAULT_MODEL_SOURCE_1, source1);
	}

	/**
	 * Override to wrap each xml fragment in an arbitrary element to allow list
	 * style comparisons.
	 * 
	 * eg. <a/><b/>  -- > <parent>a/><b/></parent>
	 */
	@Override
	public void assertXMLEqual(String err, String control, String test)
			throws SAXException, IOException {
		
		control = "<arbitrary-parent>" + control + "</arbitrary-parent>";
		test = "<arbitrary-parent>" + test + "</arbitrary-parent>";
		
		super.assertXMLEqual(err, control, test);
	}
	
	/**
	 * Override to wrap each xml fragment in an arbitrary element to allow list
	 * style comparisons.
	 * 
	 * eg. <a/><b/>  -- > <parent>a/><b/></parent>
	 */
	@Override
	public void assertXMLEqual(String control, String test)
			throws SAXException, IOException {
		
		control = "<arbitrary-parent>" + control + "</arbitrary-parent>";
		test = "<arbitrary-parent>" + test + "</arbitrary-parent>";
		
		super.assertXMLEqual(control, test);
	}
	
	

}
