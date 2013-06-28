
package gov.usgswim.sparrow.util;

import java.io.IOException;
import static junit.framework.Assert.assertNull;
import org.junit.Test;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;


public class SparrowResourceUtilsTest extends XMLTestCase {

	public final Long NONEXISTENT_MODEL = -666L;
	
	public final String DEFAULT_MODEL_SOURCE_1 =
			"	<Name>People</Name>" + 
			"	<Units/>" + 
			"	<Description/>";

	protected void setUp() throws Exception {
		XMLUnit.setIgnoreWhitespace(true);
	}


	public void testModelResourceFilePathReturnsNullIfAnyArgumentIsNull() {
		String result = SparrowResourceUtils.getModelResourceFilePath(null, "filename.txt");
		assertNull(result);

		result = SparrowResourceUtils.getModelResourceFilePath(1L, null);
		assertNull(result);
	}
	

	public void testNonExistentModelThrowsException() throws Exception {
		try {
			String result = SparrowResourceUtils.lookupModelHelp(NONEXISTENT_MODEL, "any");
		} catch (Exception e) {
			//everthing is good
			return;
		}
		fail("This should have throws an exception");
	}


	public void testRetrieveValuesFromDefaultModel() throws Exception {
		String source1 = SparrowResourceUtils.lookupModelHelp(-1L, "Sources.1");
		System.out.println(source1);
		assertXMLEqual(DEFAULT_MODEL_SOURCE_1, source1);
	}

	public void testRetrieveCommonTerms() throws Exception {
		String help1 = SparrowResourceUtils.lookupMergedHelp(-1L, "CommonTerms.MRB", "div");
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
