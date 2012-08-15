/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgswim.sparrow.validation;

import org.apache.log4j.Logger;

/**
 * Capable of testing a model.
 * 
 * @author eeverman
 */
public interface ModelValidator {
	
	public boolean initTest(SparrowModelValidationRunner runner) throws Exception;
	public void beforeEachTest(Long modelId);
	public void afterEachTest(Long modelId);
	public ModelValidationResult testModel(Long modelId) throws Exception;
	public boolean requiresDb();
	public boolean requiresTextFile();
	
	/**
	 * Compares two values and returns true if they are considered equal.
	 * Note that only positive values are expected.  If a negative value
	 * is received for any value, false is returned.
	 * 
	 * The comparison is done on a sliding scale:  values less than ten require
	 * a bit less accuracy.
	 * 
	 * This method uses the default nominal allowed fractional variance of .001D
	 * 
	 * @param expect
	 * @param compare
	 * @return
	 */
	public boolean comp(double expect, double compare);
	
	/**
	 * Compares two values and returns true if they are considered equal.
	 * Note that only positive values are expected.  If a negative value
	 * is received for any value, false is returned.
	 * 
	 * The comparison is done on a sliding scale:  values less than ten require
	 * a bit less accuracy.
	 * 
	 * @param expect
	 * @param compare
	 * @param allowedFractionalVariance Nominal allowed variance
	 * @return
	 */
	public boolean comp(double expect, double compare, double allowedFractionalVariance);
	

	
	//Test row level logging
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber, String msg);
	public void recordRowErrorDebug(Long modelId, Long reachId, Integer rowNumber, String msg);
	public void recordRowWarn(Long modelId, Long reachId, Integer rowNumber, String msg);
	public void recordRowTrace(Long modelId, Long reachId, Integer rowNumber, String msg);
	
	
	public void recordError(Long modelId, Exception exception, String msg);
	public void recordError(Long modelId, String msg);
	public void recordWarn(Long modelId, String msg);
	public void recordTrace(Long modelId, String msg);

	
	public int getIndividualFailures();

	public int getIndividualWarnings();
	
	public Logger getLogger();
}
