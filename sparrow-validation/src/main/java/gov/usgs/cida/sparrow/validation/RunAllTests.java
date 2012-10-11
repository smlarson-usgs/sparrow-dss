package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationRunner;
import gov.usgs.cida.sparrow.validation.framework.BasicComparator;
import gov.usgs.cida.sparrow.validation.tests.*;

/**
 * This runs only the DB related tests.  Just run the main method and you will
 * be prompted for input.
 * 
 * 
 * @author eeverman
 */
public class RunAllTests extends SparrowModelValidationRunner {

	public static void main(String[] args) throws Exception {
		String myClassName = RunAllTests.class.getCanonicalName();
		
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
		
		
		//WIDE comparisons
		BasicComparator wideComparator = new BasicComparator();
		wideComparator.setAllowedFractionalVarianceForValuesLessThan10(.005d);
		wideComparator.setAllowedFractionalVarianceForValuesLessThan1K(.005d);
		wideComparator.setAllowedFractionalVarianceForValuesLessThan100K(.005d);
		wideComparator.setAllowedFractionalVariance(.0001d);	//any larger value
		wideComparator.setMaxAbsVarianceForValuesLessThanOne(.001d);
		wideComparator.setMaxAbsVariance(1000d);
		
		//PRECISE comparison
		BasicComparator preciseComparator = new BasicComparator();
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan10(.0000001d);
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan1K(.0000001d);
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan100K(.0000001d);
		preciseComparator.setAllowedFractionalVariance(.0000001d);	//any larger value
		preciseComparator.setMaxAbsVarianceForValuesLessThanOne(.000000001d);
		preciseComparator.setMaxAbsVariance(.000000001d);
		
		
		/*
		 * Most validation tests take two main arguments:
		 * 1) The caparator (from above) which specifies how 'tight' the comparison
		 *		should be.  There may be more than one in some cases, such as one
		 *		for incremental values and another for total values.
		 * 2) A true/false flag that indicates if errors (places where the comparator
		 *		says values do not match) should be reported as warnings.  This should
		 *		generally be set to false, except for some tests where a failure is
		 *		something that should be pointed out to the modeler, but is not a for-sure
		 *		error.  An example would be a shore reach with a delivery coef other
		 *		than one:  Suspicious, but not strictly wrong.
		 * 
		 * In addition, some tests may take additional arguments that specify variations
		 * in the tests.  See individual tests for these options.
		 */
		
		
		////////////////////////////////
		//	The following tests are 'failable', meaning that if the comparison
		//	fails, an error is recorded.
		
		/*
		 * Arg1:  Comparator for standard reaches
		 * Arg2:  Comparator for Shore Reaches (they should be exactly equal)
		 * Arg3:	Set to true to force errors to be listed as warnings
		 * Arg4:	Set true to force non-fractioned watershed area calcs.
		 *				Production will always have this as false, but can be toggled here
		 *				for testing.  This takes precidence over Arg 3.
		 */
		addValidator(new CalculatedWaterShedAreaShouldEqualLoadedValue(wideComparator, preciseComparator, false, false));
		addValidator(new FailableDbTests(tightComparator, false));
		addValidator(new FracValuesShouldTotalToOne(tightComparator, false));
		addValidator(new ReachCoefValuesShouldBeLessThanOneAndGreaterThanZero(preciseComparator, false));
		
		
		/*
		 * Arg1:	Comparator for total load values
		 * Arg2:	Comparator for incremental load values (should be able to be tighter)
		 * Arg3		Set to true to force errors to be listed as warnings
		 * Arg4:	True to use decayed values for incremental loads (normally we would expect true)
		 */
		addValidator(new SparrowModelPredictionValidation(tightComparator, preciseComparator, false, true));
		
		addValidator(new TotalLoadEqualsIncLoadForShoreReachesInDb(preciseComparator, false));
		

		
		////////////////////////////////
		//	The following tests are 'warning only', meaning that if the comparison
		//	fails, only a warning is recorded.  This is done via the 2nd argument
		//	set to true.
		
		addValidator(new WarningOnlyDbTests(tightComparator, true));
		addValidator(new ReachCoefValuesShouldBeOneForShoreReaches(preciseComparator, true));
		
		// This test doesn't use a comparator.  Its marked as warning only
		// because it looks like there are some cases where there may be a loop
		// of river reaches... which may be ok.
		addValidator(new HydSeqOfUpstreamReachesShouldBeLessThanDownstreamReach(true));
	}
}
