package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.BaseQueryValidator;
import gov.usgs.cida.sparrow.validation.framework.Comparator;

/**
 *
 * @author eeverman
 */
public class FailableDbTests extends BaseQueryValidator {
	
	public FailableDbTests(Comparator comparator, boolean failedTestIsOnlyAWarning) {
		super(comparator, failedTestIsOnlyAWarning);
	}
		
	public FailableDbTests(Comparator comparator) {
		super(comparator, false);
	}
}
