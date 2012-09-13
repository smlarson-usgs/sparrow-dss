package gov.usgswim.sparrow.validation.tests;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eeverman
 */
public class SparrowModelValidationBaseTest {

	@Test
	public void testCompMethodRealExamples() throws Exception {
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

	@Test
	public void testCompMethod1() throws Exception {

		double allowedFracVar = .001d;
		double allowedFracVarLessThanOneK = .0001d;
		double maxAbsVarForLessThanOne = .01d;
		double maxAbsVar = .1d;

		SparrowModelValidationBase inst = new SparrowModelPredictionValidation();

		assertTrue(inst.comp(1d, 1d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertTrue(inst.comp(1000d, 1000d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertTrue(inst.comp(1000000d, 1000000d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));


		//Values one or less
		assertTrue(inst.comp(1d, 1.0001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertTrue(inst.comp(1d, 1.00999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1d, 1.0100000001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));

		//Values just over one
		assertTrue(inst.comp(1.1d, 1.1001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1.1d, 1.10011100001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		
		//Values at 1000
		assertTrue(inst.comp(1000d, 1000.099999999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1000d, 1000.100000000001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));

		//Values just over 1000
		assertTrue(inst.comp(1000.1d, 1000.199999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1000.1d, 1000.200000001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		
		
		//Big values
		assertTrue( inst.comp(1000000.0d, 1000000.0999999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1000000.0d, 1000000.100000001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
	}
	
	@Test
	public void testCompMethod2() throws Exception {

		double allowedFracVar = .001d;
		double allowedFracVarLessThanOneK = .0001d;
		double maxAbsVarForLessThanOne = .01d;
		double maxAbsVar = 2d;		//increased from 1 above

		SparrowModelValidationBase inst = new SparrowModelPredictionValidation();

		assertTrue(inst.comp(1d, 1d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertTrue(inst.comp(1000d, 1000d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertTrue(inst.comp(1000000d, 1000000d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));


		//Values one or less
		assertTrue(inst.comp(1d, 1.0001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertTrue(inst.comp(1d, 1.00999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1d, 1.0100000001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));

		//Values just over one
		assertTrue(inst.comp(1.1d, 1.1001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1.1d, 1.10011100001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		
		//Values at 1000
		assertTrue(inst.comp(1000d, 1000.099999999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1000d, 1000.100000000001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));

		//Values just over 1000
		assertTrue( inst.comp(1001d, 1002.000999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1001d, 1002.001000001d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		
		
		//Big values
		assertTrue( inst.comp(1000000.0d, 1000001.999999999d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
		assertFalse(inst.comp(1000000.0d, 1000002.0000000011d, allowedFracVar, allowedFracVarLessThanOneK, maxAbsVarForLessThanOne, maxAbsVar));
	}
}
