package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.sparrow.validation.tests.ModelValidator;
import gov.usgs.cida.sparrow.validation.tests.CalculatedWaterShedAreaShouldEqualLoadedValue;

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
		
		//1st Argument:  The allowed variantion b/f a comparison is considered an error.  .1 == 10&
		//2nd Argument:  true == Use fractioned calculations for watershed areas.  false == Use cumulative areas (add them up, but don't fraction)
		addValidator(new CalculatedWaterShedAreaShouldEqualLoadedValue(.1D, false, false)); //This one does calculation comparisons
		//addValidator(new SparrowModelFractionedWatershedAreaInvestigation());
		//addValidator(new WarningOnlyDbTests());	//Does cumulative vs total comparison for shore reaches
		
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
