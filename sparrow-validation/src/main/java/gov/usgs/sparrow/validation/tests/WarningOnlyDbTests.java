package gov.usgs.sparrow.validation.tests;

import gov.usgs.sparrow.validation.tests.BaseQueryValidator;

/**
 *
 * @author eeverman
 */
public class WarningOnlyDbTests extends BaseQueryValidator {

	protected boolean isAFailedTestOnlyAWarning() {
		return true;
	}
		
}
