package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationRunner;
import gov.usgs.cida.sparrow.validation.framework.BasicComparator;
import gov.usgs.cida.sparrow.validation.tests.*;

/**
 * Investigates a reach by printing its upstream and downstream reaches.
 * 
 * 
 * @author eeverman
 */
public class RunReachViewer extends SparrowModelValidationRunner {

	public static void main(String[] args) throws Exception {
		String myClassName = RunReachViewer.class.getCanonicalName();
		
		SparrowModelValidationRunner.main(new String[] {myClassName});
	}

	@Override
	public void loadModelValidators() {
		
		/*
		 * Configure a comparator used to decide if the expected value matches the
		 * actual value.
		 * For fractional values, .01 would allow a variation of 1 for a value of 100.
		 * 'D' indicates that it is a Double precision number (required).
		 * Note that comparison value must meet the fractional and absolute comparison requirements.
		 */
		BasicComparator tightComparator = new BasicComparator();
		
		//Fractional comparisons based on: (expected - actual) / expected
		tightComparator.setAllowedFractionalVarianceForValuesLessThan10(.000001d);
		tightComparator.setAllowedFractionalVarianceForValuesLessThan1K(.000001d);
		tightComparator.setAllowedFractionalVarianceForValuesLessThan100K(.000001d);
		tightComparator.setAllowedFractionalVariance(.000001d);	//any larger value
		
		//Absolute comparsions based on: expected - actual
		tightComparator.setMaxAbsVarianceForValuesLessThanOne(.000001d);
		tightComparator.setMaxAbsVariance(10d);

		
		/*
		 * Arg1:  Comparator for standard reaches
		 * Arg2:  Comparator for Shore Reaches (they should be exactly equal)
		 * Arg3:	Set true to force non-fractioned watershed area calcs.
		 *				Production will always have this as false, but can be toggled here
		 *				for testing.  This takes precidence over Arg 3.
		 * Arg4:	Set true to force FRAC values totalling to 1 be not corrected.
		 *				Production uses false.
		 */
		addValidator(new ReachViewer(tightComparator));

	}
}
