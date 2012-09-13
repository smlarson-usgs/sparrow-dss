package gov.usgswim.sparrow.validation;

import gov.usgswim.sparrow.validation.tests.*;

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
		addValidator(new SparrowModelPredictionValidation(.001D, true));
		
		
		/*
		 * Arg1:	Allowed fractional variance.
		 *				**Generally want this value very small b/c it is an internal comparison.
		 * Arg2:	Compare the text incremental to text total?*
		 * Arg3:	Compare the db incremental to db total value?*
		 * 
		 * * Generally, only set Arg2 or 3 to true, otherwise, there will be two
		 * errors reported for each row (assuming the text and db are the same).
		 */
		addValidator(new TotalLoadEqualsIncLoadForShoreReaches(.000001D, true, false));
		
		/*
		 * Arg1:	Allowed fractional variance.
		 *				**Generally want this value very small b/c it is an internal comparison.
		 */
		addValidator(new FracValuesShouldTotalToOne(.00000001D));
		
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
		addValidator(new CalculatedWaterShedAreaShouldEqualLoadedValue(.001D, false, false));
		
		
//		addValidator(new WarningOnlyDbTests());
		
	}
}
