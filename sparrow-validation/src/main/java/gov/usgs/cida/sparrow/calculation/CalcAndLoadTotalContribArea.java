package gov.usgs.cida.sparrow.calculation;

import gov.usgs.cida.sparrow.calculation.calculators.totalContributingAreaCalculator.TotalContributingAreaCalculator;
import gov.usgs.cida.sparrow.calculation.framework.SparrowCalculationRunner;

/**
 * This runs only the DB related tests.  Just run the main method and you will
 * be prompted for input.
 *
 *
 * @author eeverman
 */
public class CalcAndLoadTotalContribArea extends SparrowCalculationRunner {

	public static void main(String[] args) throws Exception {
		String myClassName = CalcAndLoadTotalContribArea.class.getCanonicalName();

		
		System.out.println("*** This runner will calculate the Total Contributing Area for each reach and load it to the database column MODEL_REACH_ATTRIB.TOT_CONTRIB_AREA***");
		SparrowCalculationRunner.main(new String[] {myClassName});
	}

	@Override
	public void loadModelCalculators() {
		addCalculation(new TotalContributingAreaCalculator(false, false, true));

	}
}
