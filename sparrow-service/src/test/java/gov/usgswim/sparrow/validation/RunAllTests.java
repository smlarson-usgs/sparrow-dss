package gov.usgswim.sparrow.validation;

import gov.usgswim.sparrow.validation.tests.SparrowModelPredictionValidation;
import gov.usgswim.sparrow.validation.tests.SparrowModelWaterShedAreaValidation;
import gov.usgswim.sparrow.validation.tests.FailableDbTests;
import gov.usgswim.sparrow.validation.tests.TotalLoadEqualsIncLoadForShoreReaches;
import org.apache.log4j.Logger;

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
		 * Arg1:	Allowed percentage variance (.01 would allow a variation of 1 for a value of 100).
		 *				'D' just indicates that it is a Double precision number (required).
		 * Arg2:	True to use decayed values for incremental loads (normally we would expect true)
		 */
		addValidator(new SparrowModelPredictionValidation(.001D, true));
		
		
		/*
		 * Arg1:	Allowed percentage variance (.01 would allow a variation of 1 for a value of 100).
		 *				'D' just indicates that it is a Double precision number (required).
		 *				**Generally want this value very small b/c it is an internal comparison.
		 * Arg2:	Compare the text incremental to text total?*
		 * Arg3:	Compare the db incremental to db total value?*
		 * 
		 * * Generally, only set Arg2 or 3 to true, otherwise, there will be two
		 * errors reported for each row (assuming the text and db are the same).
		 */
		addValidator(new TotalLoadEqualsIncLoadForShoreReaches(.000001D, true, false));
		
		
		/*
		 * No arguments, just runs a bunch of queries listed in a file by the name of:
		 * FailableDbTests.properties
		 */
		addValidator(new FailableDbTests());
//		addValidator(new SparrowModelWaterShedAreaValidation());
//		addValidator(new WarningOnlyDbTests());
		
	}
}
