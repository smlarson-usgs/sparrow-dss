package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgs.cida.sparrow.validation.framework.BaseQueryValidator;

/**
 *
 * @author eeverman
 */
public class WarningOnlyDbTests extends BaseQueryValidator {

	boolean reportErrorsAsOnlyWarnings = true;
	
	protected boolean isAFailedTestOnlyAWarning() {
		return reportErrorsAsOnlyWarnings;
	}
	
	public WarningOnlyDbTests(Comparator comparator, boolean reportErrorsAsOnlyWarnings) {
		super(comparator);
		this.reportErrorsAsOnlyWarnings = reportErrorsAsOnlyWarnings;
	}
		
}
