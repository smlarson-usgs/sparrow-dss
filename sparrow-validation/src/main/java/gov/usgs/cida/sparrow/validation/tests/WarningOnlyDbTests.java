package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.Comparator;
import gov.usgs.cida.sparrow.validation.tests.BaseQueryValidator;

/**
 *
 * @author eeverman
 */
public class WarningOnlyDbTests extends BaseQueryValidator {

	protected boolean isAFailedTestOnlyAWarning() {
		return true;
	}
	
	public WarningOnlyDbTests(Comparator comparator) {
		super(comparator);
	}
		
}
