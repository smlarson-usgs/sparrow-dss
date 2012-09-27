package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.BasicComparator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eeverman
 */
public class SparrowModelValidationBaseTest {


	@Test
	public void testCompMethod1() throws Exception {

		BasicComparator inst = new BasicComparator();
		inst.setAllowedFractionalVarianceForValuesLessThan10(.0001d);
		inst.setAllowedFractionalVarianceForValuesLessThan1K(.0001d);
		inst.setAllowedFractionalVarianceForValuesLessThan100K(.1d);
		inst.setAllowedFractionalVariance(.001d);
		inst.setMaxAbsVarianceForValuesLessThanOne(.01d);
		inst.setMaxAbsVariance(.1d);
		

		assertTrue(inst.comp(1d, 1d));
		assertTrue(inst.comp(1000d, 1000d));
		assertTrue(inst.comp(1000000d, 1000000d));


		//Values one or less
		assertTrue(inst.comp(1d, 1.0001d));
		assertTrue(inst.comp(1d, 1.00999999d));
		assertFalse(inst.comp(1d, 1.0100000001d));

		//Values just over one
		assertTrue(inst.comp(1.1d, 1.1001d));
		assertFalse(inst.comp(1.1d, 1.10011100001d));
		
		//Values at 1000
		assertTrue(inst.comp(1000d, 1000.099999999999d));
		assertFalse(inst.comp(1000d, 1000.100000000001d));

		//Values just over 1000
		assertTrue(inst.comp(1000.1d, 1000.199999999d));
		assertFalse(inst.comp(1000.1d, 1000.200000001d));
		
		
		//Big values
		assertTrue( inst.comp(1000000.0d, 1000000.0999999999d));
		assertFalse(inst.comp(1000000.0d, 1000000.100000001d));
	}
	
	@Test
	public void testCompMethod2() throws Exception {

		BasicComparator inst = new BasicComparator();
		inst.setAllowedFractionalVarianceForValuesLessThan10(.0001d);
		inst.setAllowedFractionalVarianceForValuesLessThan1K(.0001d);
		inst.setAllowedFractionalVarianceForValuesLessThan100K(.001d);
		inst.setAllowedFractionalVariance(.001d);
		inst.setMaxAbsVarianceForValuesLessThanOne(.01d);
		inst.setMaxAbsVariance(2d);
		

		assertTrue(inst.comp(1d, 1d));
		assertTrue(inst.comp(1000d, 1000d));
		assertTrue(inst.comp(1000000d, 1000000d));


		//Values one or less
		assertTrue(inst.comp(1d, 1.0001d));
		assertTrue(inst.comp(1d, 1.00999999d));
		assertFalse(inst.comp(1d, 1.0100000001d));

		//Values just over one
		assertTrue(inst.comp(1.1d, 1.1001d));
		assertFalse(inst.comp(1.1d, 1.10011100001d));
		
		//Values at 1000
		assertTrue(inst.comp(1000d, 1000.099999999999d));
		assertFalse(inst.comp(1000d, 1000.100000000001d));

		//Values just over 1000
		assertTrue( inst.comp(1001d, 1002.000999999d));
		assertFalse(inst.comp(1001d, 1002.001000001d));
		
		
		//Big values
		assertTrue( inst.comp(1000000.0d, 1000001.999999999d));
		assertFalse(inst.comp(1000000.0d, 1000002.0000000011d));
	}
}
