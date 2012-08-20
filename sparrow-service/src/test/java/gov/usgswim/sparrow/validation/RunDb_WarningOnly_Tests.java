package gov.usgswim.sparrow.validation;

import gov.usgswim.sparrow.validation.tests.WarningOnlyDbTests;
import gov.usgswim.sparrow.validation.tests.SparrowModelWaterShedAreaValidation;
import gov.usgswim.sparrow.validation.tests.FailableDbTests;
import gov.usgswim.sparrow.validation.tests.ModelValidator;
import org.apache.log4j.Logger;

/**
 * This runs only the DB related tests.  Just run the main method and you will
 * be prompted for input.
 * 
 * 
 * @author eeverman
 */
public class RunDb_WarningOnly_Tests extends SparrowModelValidationRunner {

	public static void main(String[] args) throws Exception {
		String myClassName = RunDb_WarningOnly_Tests.class.getCanonicalName();
		
		SparrowModelValidationRunner.main(new String[] {myClassName});
	}

	@Override
	public void loadModelValidators() {
		
		//addValidator(new SparrowModelWaterShedAreaValidation()); //This one does calculation comparisons
		addValidator(new WarningOnlyDbTests());	//Does cumulative vs total comparison for shore reaches
		
	}
	
	@Override
	public boolean initModelValidators() {
		
		boolean ok = true;
		
		for (ModelValidator mv : getValidators()) {
			
			try {
				
				//Here is the key bit:
				//pass true as the 2nd arg to indicate that the test should not fail
				//if an error is recorded.
				ok = mv.initTest(this, true);
				if (! ok) return false;
				
			} catch (Exception e) {
				log.error("Unable to initiate the test: " + mv.getClass(), e);
				return false;
			}

		}
		
		return true;
	}
}
