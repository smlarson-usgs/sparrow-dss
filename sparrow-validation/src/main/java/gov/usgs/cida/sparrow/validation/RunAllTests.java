package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.sparrow.validation.tests.FracValuesShouldTotalToOne;
import gov.usgs.cida.sparrow.validation.tests.TotalLoadEqualsIncLoadForShoreReaches;
import gov.usgs.cida.sparrow.validation.tests.CalculatedWaterShedAreaShouldEqualLoadedValue;
import gov.usgs.cida.sparrow.validation.tests.SparrowModelPredictionValidation;

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
		
		//Configure a comparator used to decide if the expected value matches the
		//actual value.
		//Note that comparison value must meet the fractional and absolute comparison requirements.
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
		
		
		/*
		 * FOR ALL TESTS THAT TAKE PARAMETERS:
		 * 
		 * The first parameter (Arg1) is always the allowed fractional variation.
		 * .01 would allow a variation of 1 for a value of 100).
		 * 'D' indicates that it is a Double precision number (required).
		 * 
		 */
		
		
		/*
		 * Arg1:	Allowed fractional variance.
		 * Arg2:	True to use decayed values for incremental loads (normally we would expect true)
		 */
		addValidator(new SparrowModelPredictionValidation(tightComparator, true));
		
		
		/*
		 * Arg1:	Allowed fractional variance.
		 *				**Generally want this value very small b/c it is an internal comparison.
		 * Arg2:	Compare the text incremental to text total?*
		 * Arg3:	Compare the db incremental to db total value?*
		 * 
		 * * Generally, only set Arg2 or 3 to true, otherwise, there will be two
		 * errors reported for each row (assuming the text and db are the same).
		 */
		addValidator(new TotalLoadEqualsIncLoadForShoreReaches(tightComparator, true, false));
		
		/*
		 * Arg1:	Allowed fractional variance.
		 *				**Generally want this value very small b/c it is an internal comparison.
		 */
		addValidator(new FracValuesShouldTotalToOne(tightComparator));
		
		/*
		 * No arguments, just runs a bunch of queries listed in a file by the name of:
		 * FailableDbTests.properties
		 */
		//addValidator(new FailableDbTests());
		
		/*
		 * Arg1:	Allowed fractional variance.
		 * Arg2:	Set true to force non-fractioned watershed area calcs.
		 *				Production will always have this as false, but can be toggled here
		 *				for testing.  This takes precidence over Arg 3.
		 * Arg3:	Set true to force FRAC values totalling to 1 be not corrected.
		 *				Production uses false.
		 */
		addValidator(new CalculatedWaterShedAreaShouldEqualLoadedValue(wideComparator, false, false));
		
		
//		addValidator(new WarningOnlyDbTests());
		
	}
}
