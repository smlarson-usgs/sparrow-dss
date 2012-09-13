package gov.usgs.sparrow.validation.tests;

import java.util.ArrayList;

/**
 *
 * @author eeverman
 */
public class ValidationResults extends ArrayList<ModelTestResultList> {

	private boolean fatalError = false;
	private boolean configError = false;
	
	
	public void setFatalError() { fatalError = true; }
	public void setConfigError() { configError = true; }
	
	
	public int getModelCount() {
		return this.size();
	}
	
	
	public boolean isPerfect() {
		if (configError || fatalError) return false;
		
		for (ModelTestResultList result : this) {
			if (! result.isPerfect()) return false;
		}
		
		return true;
	}
	
	public boolean isOk() {
		if (configError || fatalError) return false;
		
		for (ModelTestResultList result : this) {
			if (! result.isOk()) return false;
		}
		
		return true;
	}
	
	public boolean isAnyError() {
		if (configError || fatalError) return true;
		
		for (ModelTestResultList result : this) {
			if (result.isAnyError()) return true;
		}
		
		return false;
	}
	
	public int getModelsWithAnyAnyErrorCount() {
		int cnt = 0;
		
		for (ModelTestResultList result : this) {
			if (result.isAnyError()) cnt++;
		}
		
		return cnt;
	}
	
	public int getModelsWithWarnCount() {
		int cnt = 0;
		
		for (ModelTestResultList result : this) {
			if (result.isWarn()) cnt++;
		}
		
		return cnt;
	}
	
	public int getModelsWithErrorsAsWarnCount() {
		int cnt = 0;
		
		for (ModelTestResultList result : this) {
			if (result.isErrorsAsWarn()) cnt++;
		}
		
		return cnt;
	}
	
	public boolean isFatal() {
		if (fatalError) return true;
		
		for (ModelTestResultList result : this) {
			if (result.isFatal()) return true;
		}
		
		return false;
	}
	
	public boolean isConfigError() {
		if (configError) return true;
		
		for (ModelTestResultList result : this) {
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
		for (ModelTestResultList result : this) {
			cnt += result.getErrorCount();
		}
		
		return cnt;
	}
	
	public int getWarnCount() {
		int cnt = 0;
		for (ModelTestResultList result : this) {
			cnt += result.getWarnCount();
		}
		
		return cnt;
	}
	
	public int getErrorsAsWarnCount() {
		int cnt = 0;
		for (ModelTestResultList result : this) {
			cnt += result.getErrorsAsWarnCount();
		}
		
		return cnt;
	}
}
