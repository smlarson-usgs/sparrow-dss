/**
 * 
 */
package gov.usgswim.sparrow.revised;

import gov.usgswim.sparrow.revised.steps.Step;

public class CalculationStage{
	public final Step step;
	public String cacheName;
	public Object cacheKey;
	private CalculationResult result;

	public CalculationStage(Step step) {
		this.step = step;
	}

	public CalculationStage cacheStepResultIn(String cacheName) {
		this.cacheName = cacheName;
		return this;
	}

	public CalculationStage withCacheKey(Object key) {
		this.cacheKey = key;
		return this;
	}
	
	public void setResult(CalculationResult result) {
		this.result = result;
	}
	
	public CalculationResult getResult() {
		return result;
	}
}