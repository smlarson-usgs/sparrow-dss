package gov.usgs.cida.sparrow.validation;

import gov.usgs.sparrow.validation.tests.*;
import org.apache.log4j.Logger;

/**
 * This runs only the DB related tests.  Just run the main method and you will
 * be prompted for input.
 * 
 * 
 * @author eeverman
 */
public class RunAllDBOnlyTests extends SparrowModelValidationRunner {

	public static void main(String[] args) throws Exception {
		String myClassName = RunAllDBOnlyTests.class.getCanonicalName();
		
		SparrowModelValidationRunner.main(new String[] {myClassName});
	}

	@Override
	public void loadModelValidators() {
		
		addValidator(new FailableDbTests());
		addValidator(new CalculatedWaterShedAreaShouldEqualLoadedValue());
		addValidator(new WarningOnlyDbTests());
		
	}
}
