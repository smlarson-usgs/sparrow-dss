package gov.usgs.cida.sparrow.calculation;

import gov.usgs.cida.sparrow.calculation.calculators.totalContributingAreaCalculator.TotalContributingAreaCalculator;
import gov.usgs.cida.sparrow.calculation.framework.SparrowCalculationRunner;
import org.apache.log4j.Logger;

/**
 * This runs only the DB related tests.  Just run the main method and you will
 * be prompted for input.
 *
 *
 * @author eeverman
 */
public class RunAllCalculations extends SparrowCalculationRunner {

	public static void main(String[] args) throws Exception {
		String myClassName = RunAllCalculations.class.getCanonicalName();

		SparrowCalculationRunner.main(new String[] {myClassName});
	}

	@Override
	public void loadModelCalculators() {
		addCalculation(new TotalContributingAreaCalculator(false, false, false, false));

	}
}
