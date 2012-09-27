package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationRunner;
import gov.usgs.cida.sparrow.validation.framework.BasicComparator;
import gov.usgs.cida.sparrow.validation.tests.WarningOnlyDbTests;
import gov.usgs.cida.sparrow.validation.tests.CalculatedWaterShedAreaShouldEqualLoadedValue;
import gov.usgs.cida.sparrow.validation.tests.FailableDbTests;
import gov.usgs.cida.sparrow.validation.tests.FracValuesShouldTotalToOne;
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
		
		
		//Configure a comparator used to decide if the expected value matches the
		//actual value.
		//Note that comparison value must meet the fractional and absolute comparison requirements.
		BasicComparator tightComparator = new BasicComparator();
		
		//Fractional comparisons based on: (expected - actual) / expected
		tightComparator.setAllowedFractionalVarianceForValuesLessThan10(.0001d);
		tightComparator.setAllowedFractionalVarianceForValuesLessThan1K(.0001d);
		tightComparator.setAllowedFractionalVarianceForValuesLessThan100K(.0001d);
		tightComparator.setAllowedFractionalVariance(.0001d);	//any larger value
		
		//Absolute comparsions based on: expected - actual
		tightComparator.setMaxAbsVarianceForValuesLessThanOne(.00001d);
		tightComparator.setMaxAbsVariance(10d);
		
		
		
		BasicComparator wideComparator = new BasicComparator();
		wideComparator.setAllowedFractionalVarianceForValuesLessThan10(.005d);
		wideComparator.setAllowedFractionalVarianceForValuesLessThan1K(.005d);
		wideComparator.setAllowedFractionalVarianceForValuesLessThan100K(.005d);
		wideComparator.setAllowedFractionalVariance(.0001d);	//any larger value
		wideComparator.setMaxAbsVarianceForValuesLessThanOne(.001d);
		wideComparator.setMaxAbsVariance(1000d);
		
		
		
		
		addValidator(new FailableDbTests(tightComparator));
		
		//Arg 1:  Comparator for standard reaches
		//Arg 2:  Comparator for shoreline reaches (they should match exactly)
		addValidator(new CalculatedWaterShedAreaShouldEqualLoadedValue(wideComparator, tightComparator));
		
		addValidator(new WarningOnlyDbTests(tightComparator));
		addValidator(new FracValuesShouldTotalToOne(tightComparator));
		
	}
}
