/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.sparrow.calculation.framework;

import org.apache.log4j.Logger;

/**
 * Capable of testing a model.
 *
 * @author eeverman
 */
public interface Calculator {

	/**
	 * Init the test, passing in the runner which is used to retrieve user spec'ed
	 * startup values.
	 *
	 * @param runner The runner, which can be used to fetch more data about the test.
	 * @return
	 * @throws Exception
	 */
	public boolean initCalc(SparrowCalculationRunner runner) throws Exception;

	public void beforeEachCalc(Long modelId);
	public void afterEachCalc(Long modelId);
	public CalculationResult calculate(Long modelId) throws Exception;
	public boolean requiresDb();
	public boolean requiresTextFile();



	//Test row level logging
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber, String msg);
	public void recordRowWarn(Long modelId, Long reachId, Integer rowNumber, String msg);
	public void recordRowDebug(Long modelId, Long reachId, Integer rowNumber, String msg);
	public void recordRowTrace(Long modelId, Long reachId, Integer rowNumber, String msg);

	//Full detail row level events
	//exceptName and actualName params are the names of what is being
	//compared i.e., db vs calculated values.
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber,
			Object expected, Object actual, String expectName, String actualName,
			Boolean shoreReach, Boolean ifTran, String msg);
	public void recordRowWarn(Long modelId, Long reachId, Integer rowNumber,
			Object expected, Object actual, String expectName, String actualName,
			Boolean shoreReach, Boolean ifTran, String msg);
	public void recordRowDebug(Long modelId, Long reachId, Integer rowNumber,
			Object expected, Object actual, String expectName, String actualName,
			Boolean shoreReach, Boolean ifTran, String msg);
	public void recordRowTrace(Long modelId, Long reachId, Integer rowNumber,
			Object expected, Object actual, String expectName, String actualName,
			Boolean shoreReach, Boolean ifTran, String msg);


	public void recordError(Long modelId, String msg);
	public void recordWarn(Long modelId, String msg);
	public void recordTrace(Long modelId, String msg);

	/**
	 * Used to record an actual unexpected error in running the test.
	 * Counts as a test failure.
	 * @param modelId
	 * @param exception
	 * @param msg
	 */
	public void recordCalcException(Long modelId, Exception exception, String msg);

	public Logger getLogger();
}
