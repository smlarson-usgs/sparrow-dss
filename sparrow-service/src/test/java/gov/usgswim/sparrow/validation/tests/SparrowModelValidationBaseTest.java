package gov.usgswim.sparrow.validation.tests;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eeverman
 */
public class SparrowModelValidationBaseTest {

	
	
	
	@Test
	public void testCompMethod() throws Exception {
		SparrowModelValidationBase inst = new SparrowModelPredictionValidation();
		
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .1D));
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .01D));
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .001D));
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .0001D));
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .00001D));
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .000001D));
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .0000001D));
		assertTrue(inst.comp(23663.721596573d, 23663.7218127502d, .00000001D));

		
		assertTrue(inst.comp(28.2413452945d, 28.240763784d, .1D));
		assertTrue(inst.comp(28.2413452945d, 28.240763784d, .01D));
		assertTrue(inst.comp(28.2413452945d, 28.240763784d, .001D));
		assertTrue(inst.comp(28.2413452945d, 28.240763784d, .0001D));

	}
	
	
}
