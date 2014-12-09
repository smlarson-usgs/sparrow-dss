package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.sparrow.validation.framework.SparrowModelValidationRunner;
import gov.usgs.cida.sparrow.validation.framework.BasicComparator;
import gov.usgs.cida.sparrow.validation.framework.ModelValidator;
import gov.usgs.cida.sparrow.validation.tests.*;

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
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan10(.0000001d);
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan1K(.0000001d);
		preciseComparator.setAllowedFractionalVarianceForValuesLessThan100K(.0000001d);
		preciseComparator.setAllowedFractionalVariance(.0000001d);	//any larger value
		preciseComparator.setMaxAbsVarianceForValuesLessThanOne(.000000001d);
		preciseComparator.setMaxAbsVariance(.000000001d);


		/*
		 * Arg1:  Comparator for standard reaches
		 * Arg2:  Comparator for Shore Reaches (they should be exactly equal)
		 * Arg3:	Set to true to force errors to be listed as warnings
		 * Arg4:	Set true to force non-fractioned watershed area calcs.
		 *				Production will always have this as false, but can be toggled here
		 *				for testing.  This takes precidence over Arg 3.
		 */
		addValidator(new ModelMetaDataValidation());
		addValidator(new CalculatedTotalContributingAreaShouldEqualLoadedValue(wideComparator, preciseComparator, true));
		addValidator(new CalculatedTotalUpstreamAreaShouldEqualLoadedValue(wideComparator, preciseComparator, true));

		addValidator(new ReachCoefValuesShouldBeOneForShoreReaches(preciseComparator, true));
		addValidator(new WarningOnlyDbTests(tightComparator, true));

		////////////////////////////////
		// These test doesn't use a comparator.  They are marked as warning only
		// because it looks like there are some cases where failures are just unusual
		// situations that are not errors.
		addValidator(new HydSeqOfUpstreamReachesShouldBeLessThanDownstreamReach(true));
		addValidator(new DiversionsShouldHaveUpsteamReaches(true));

	}

}
