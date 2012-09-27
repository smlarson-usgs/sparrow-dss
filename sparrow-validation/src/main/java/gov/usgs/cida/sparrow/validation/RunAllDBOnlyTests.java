package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationRunner;
import gov.usgs.cida.sparrow.validation.framework.BasicComparator;
import gov.usgs.cida.sparrow.validation.tests.*;
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
		
		
		/*
		 * Configure a comparator used to decide if the expected value matches the
		 * actual value.
		 * For fractional values, .01 would allow a variation of 1 for a value of 100.
		 * 'D' indicates that it is a Double precision number (required).
		 * Note that comparison value must meet the fractional and absolute comparison requirements.
		 */
		BasicComparator tightComparator = new BasicComparator();
		
		//Fractional comparisons based on: (expected - actual) / expected
		tightComparator.setAllowedFractionalVarianceForValuesLessThan10(.0001d);
		tightComparator.setAllowedFractionalVarianceForValuesLessThan1K(.0001d);
		tightComparator.setAllowedFractionalVarianceForValuesLessThan100K(.0001d);
		tightComparator.setAllowedFractionalVariance(.0001d);	//any larger value
		
		//Absolute comparsions based on: expected - actual
		tightComparator.setMaxAbsVarianceForValuesLessThanOne(.00001d);
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
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan10(.000001d);
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan1K(.000001d);
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan100K(.000001d);
		preciseComparator.setAllowedFractionalVariance(.000001d);	//any larger value
		preciseComparator.setMaxAbsVarianceForValuesLessThanOne(.00000001d);
		preciseComparator.setMaxAbsVariance(.00000001d);
		
		
		/*
		 * Arg1:  Comparator for standard reaches
		 * Arg2:  Comparator for Shore Reaches (they should be exactly equal)
		 * Arg3:	Set true to force non-fractioned watershed area calcs.
		 *				Production will always have this as false, but can be toggled here
		 *				for testing.  This takes precidence over Arg 3.
		 * Arg4:	Set true to force FRAC values totalling to 1 be not corrected.
		 *				Production uses false.
		 */
		addValidator(new CalculatedWaterShedAreaShouldEqualLoadedValue(wideComparator, preciseComparator, false, false));
		
		addValidator(new FailableDbTests(tightComparator));
		addValidator(new FracValuesShouldTotalToOne(tightComparator));
		addValidator(new TotalLoadEqualsIncLoadForShoreReachesInDb(preciseComparator));
		
		/*
		 * Arg1:  Comparator for standard reaches
		 * Arg2:	reportErrorsAsOnlyWarnings:  If true, a warning is generated instead of errors.
		 *				If false, errors are reported as errors.
		 */
		addValidator(new WarningOnlyDbTests(tightComparator, false));
		
		
		
	}
}
