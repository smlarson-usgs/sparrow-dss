package gov.usgswim.sparrow.monitor;

import java.util.ArrayList;

/**
 *
 * @author eeverman
 */
public class ServletInvocation extends Invocation {

	//A list of validation messages.  Never null.
	private volatile String[] validationErrors = null;
	private volatile Integer responseCode = null;
	
	public ServletInvocation(Class target) {
		super(target);
	}
		
	public ServletInvocation(Class target, Object request, String requestAsString) {
		super(target, request, requestAsString);
	}
	
	public String getName() { return "servlet"; }
	
	
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

	public Integer getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}

}
