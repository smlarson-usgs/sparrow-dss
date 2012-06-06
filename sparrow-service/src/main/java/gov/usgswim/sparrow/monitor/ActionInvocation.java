package gov.usgswim.sparrow.monitor;

import java.util.ArrayList;

/**
 *
 * @author eeverman
 */
public class ActionInvocation extends Invocation {

	//A list of validation messages.  Never null.
	private volatile String[] validationErrors = null;
	
	public ActionInvocation(Class target) {
		super(target);
	}
		
	public ActionInvocation(Class target, Object request, String requestAsString) {
		super(target, request, requestAsString);
	}
	
	
	public void setValidationErrors(String[] errors) {
		if (errors != null && errors.length > 0) {
			validationErrors = errors;
		}
	}
	public String[] getValidationErrors() {
		return validationErrors;
	}
	
	/**
	 * Returns true if there are validation errors.
	 * @return
	 */
	public boolean hasValidationErrors() {
		return validationErrors != null;
	}
	
}
