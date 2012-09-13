package gov.usgswim.sparrow.validation.tests;

import gov.usgswim.sparrow.validation.tests.BaseQueryValidator;

/**
 *
 * @author eeverman
 */
public class WarningOnlyDbTests extends BaseQueryValidator {

	protected boolean isAFailedTestOnlyAWarning() {
		return true;
	}
		
}
