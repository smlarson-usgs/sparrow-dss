package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.tests.BaseQueryValidator;

/**
 *
 * @author eeverman
 */
public class WarningOnlyDbTests extends BaseQueryValidator {

	protected boolean isAFailedTestOnlyAWarning() {
		return true;
	}
		
}
