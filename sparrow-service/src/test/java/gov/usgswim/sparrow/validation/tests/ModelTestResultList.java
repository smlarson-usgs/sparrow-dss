package gov.usgswim.sparrow.validation.tests;

import java.util.ArrayList;

/**
 *
 * @author eeverman
 */
public class ModelTestResultList extends ArrayList<TestResult> {

	Long modelId;
	
	public ModelTestResultList(Long modelId) {
		this.modelId = modelId;
	}
	
	public Long getModelId() {
		return modelId;
	}
	
	
	public boolean isPerfect() {
		for (TestResult result : this) {
			if (! result.isPerfect()) return false;
		}
		
		return true;
	}
	
	public boolean isOk() {
		for (TestResult result : this) {
			if (! result.isOk()) return false;
		}
		
		return true;
	}
	
	public boolean isAnyError() {
		for (TestResult result : this) {
			if (result.isAnyError()) return true;
		}
		
		return false;
	}
	
	public boolean isFatal() {
		for (TestResult result : this) {
			if (result.isFatal()) return true;
		}
		
		return false;
	}
	
	public boolean isConfigError() {
		for (TestResult result : this) {
			if (result.isConfigError()) return true;
		}
		
		return false;
	}
	
	public boolean isError() {
		return getErrorCount() > 0;
	}
	
	public boolean isWarn() {
		return getWarnCount() > 0;
	}
	
	public boolean isErrorsAsWarn() {
		return getErrorsAsWarnCount() > 0;
	}
	
	public int getErrorCount() {
		int cnt = 0;
		for (TestResult result : this) {
			if (result.isError()) cnt++;
		}
		
		return cnt;
	}
	
	public int getWarnCount() {
		int cnt = 0;
		for (TestResult result : this) {
			if (result.isWarn()) cnt++;
		}
		
		return cnt;
	}
	
	public int getErrorsAsWarnCount() {
		int cnt = 0;
		for (TestResult result : this) {
			if (result.isErrorsAsWarn()) cnt++;
		}
		
		return cnt;
	}
	
}
