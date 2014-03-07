package gov.usgswim.sparrow;

import gov.usgswim.sparrow.test.SparrowTestBase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

public class IgnoreContextIdDifferenceListenerTest extends SparrowTestBase {

	static String base = "<root attrib=\"attrib_val\"><row context-id=\"1\">row1_value</row></root>";
	
	// ============
	// TEST METHODS
	// ============
	@Test
	public void identical() throws Exception {
		Diff diff = compareXMLIgnoreContextId(base, base);
		assertTrue(similarXMLIgnoreContextId(base, base));
		assertTrue(diff.similar());
		assertTrue(diff.identical());
	}
	
	/**
	 * Comments are ignored anyway...
	 * @throws Exception
	 */
	@Test
	public void similar() throws Exception {
		String mod = "<root attrib=\"attrib_val\"><!-- COMMENT --><row context-id=\"1\">row1_value</row></root>";
		Diff diff = compareXMLIgnoreContextId(base, mod);
		assertTrue(similarXMLIgnoreContextId(base, mod));
		assertTrue(diff.similar());
		assertTrue(diff.identical());
	}
	
	@Test
	public void modifiedContextId() throws Exception {
		String mod = "<root attrib=\"attrib_val\"><row context-id=\"DIFFERENT\">row1_value</row></root>";
		Diff diff = compareXMLIgnoreContextId(base, mod);
		assertTrue(similarXMLIgnoreContextId(base, mod));
		assertTrue(diff.similar());
		assertTrue(diff.identical());
	}
	
	@Test
	public void modifiedAttribute() throws Exception {
		String mod = "<root attrib=\"DIFFERENT\"><row context-id=\"1\">row1_value</row></root>";
		Diff diff = compareXMLIgnoreContextId(base, mod);
		assertFalse(similarXMLIgnoreContextId(base, mod));
		assertFalse(diff.similar());
		assertFalse(diff.identical());
	}
	
	@Test
	public void modifiedValue() throws Exception {
		String mod = "<root attrib=\"attrib_val\"><row context-id=\"1\">DIFFERENT</row></root>";
		Diff diff = compareXMLIgnoreContextId(base, mod);
		assertFalse(similarXMLIgnoreContextId(base, mod));
		assertFalse(diff.similar());
		assertFalse(diff.identical());
	}
	

	
}

