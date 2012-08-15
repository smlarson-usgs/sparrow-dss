package gov.usgswim.sparrow.validation;

/**
 *
 * @author eeverman
 */
public class WarningOnlyDbTests extends BaseQueryValidator {

	protected boolean isAFailedTestOnlyAWarning() {
		return true;
	}
		
}
