package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgs.cida.sparrow.validation.framework.BaseQueryValidator;

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
