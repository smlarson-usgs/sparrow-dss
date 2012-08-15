package gov.usgswim.sparrow.validation.tests;

import gov.usgswim.sparrow.validation.tests.ModelValidator;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.validation.SparrowModelValidationRunner;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * Implements all the plumbing portion of a ModelValidator.
 * 
 * Subclasses only need to implement:
 * <ul>
 * <li>testModel() - The main test method</li>
 * <li>requiresDb() - Return true if the test requires db access</li>
 * <li>requiresTextFile() - Return true if a text file is needed for comparison</li>
 * </ul>
 * 
 * @author eeverman
 */
public abstract class SparrowModelValidationBase implements ModelValidator {
	
	/**
	 * The required comparison accuracy (expected - actual)/(max(expected, actual))
	 * This value is slightly relaxed for values less than 1.
	 */
	final double REQUIRED_COMPARISON_FRACTION = .001d;	//comp fraction
	
	
	SparrowModelValidationRunner runner;
	boolean failedTestIsOnlyAWarning = false;
	
	
	public final static String ID_COL_KEY = "id_col";	//Table property of the key column

	final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = Integer.MAX_VALUE;
	final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = Integer.MAX_VALUE;
	
	protected boolean logDetailHeaderWritten = false;

	protected int failCnt = 0;
	protected int warnCnt = 0;
	protected int warnWhichWereFailsCnt = 0;
	
	private Logger testLog;

	@Override
	public boolean initTest(SparrowModelValidationRunner runner, boolean failedTestIsOnlyAWarning) throws Exception {
		this.runner = runner;
		this.failedTestIsOnlyAWarning = failedTestIsOnlyAWarning;
		return true;
	}
	
	@Override
	public void beforeEachTest(Long modelId) {
		recordTrace(modelId, "Beginning Test '" + this.getClass().getName() + "'");
		SharedApplication.getInstance().clearAllCaches();
	}
	
	@Override
	public void afterEachTest(Long modelId) {
		this.writeDetailLogFooter(modelId);
		
		if (failCnt > 0) {
			this.getLogger().error("----- Test Completed with " + failCnt + " FAILURES and " + warnCnt + " WARNINGS.");
		} else if (warnCnt > 0) {
			if (failedTestIsOnlyAWarning && warnWhichWereFailsCnt > 0) {
				this.getLogger().error("----- Test Completed with " + warnCnt + " WARNINGS, " +
						"however, " + warnWhichWereFailsCnt + " of those are considered failures, but this test is currently configured to not count a warning as a failure.");
			} else {
				this.getLogger().warn("----- Test Completed with " + warnCnt + " WARNINGS (no failures).");
			}
			
		} else {
			recordTrace(modelId, "Completed Test '" + this.getClass().getName() + "' with no failures or warnings.");
		}
	}
	
	
	
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
	public boolean comp(double expect, double compare) {
		return comp(expect, compare, REQUIRED_COMPARISON_FRACTION);
	}
	
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
	public boolean comp(double expect, double compare, double allowedFractionalVariance) {
		
		if (expect < 0 || compare < 0) {
			return false;
		}
		
		double diff = Math.abs(compare - expect);
		double frac = 0;
		
		if (diff == 0) {
			return true;	//no further comparison required
		}
		
		if (expect < 1d) {
			return (diff < (allowedFractionalVariance * 10d));
		} else {
			frac = diff / expect;	//we are sure at this point that baseValue > 0
		}
		
		if (expect < 10) {
			return frac < (allowedFractionalVariance * 5L);
		} else if (expect < 20) {
			return frac < (allowedFractionalVariance * 2L);
		} else {
			return frac < allowedFractionalVariance;
		}
	}
	
	@Override
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber, String msg) {
		if (failedTestIsOnlyAWarning) {
			recordRowWarn(modelId, reachId, rowNumber, msg, true);
			warnWhichWereFailsCnt++;
		} else {
			failCnt++;
			writeDetailLogHeader(modelId);
			getLogger().error(" | " + modelId + " | " + reachId + " | " + rowNumber + " | FAIL | " + msg);
		}
	}
	
	@Override
	public void recordRowErrorDebug(Long modelId, Long reachId, Integer rowNumber, String msg) {
		if (getLogger().isDebugEnabled()) {
			writeDetailLogHeader(modelId);
			getLogger().debug(" | " + modelId + " | " + reachId + " | " + rowNumber + " | ERROR DEBUG | " + msg);
		}
	}
	
	@Override
	public void recordRowWarn(Long modelId, Long reachId, Integer rowNumber, String msg) {
		warnCnt++;
		
		if (getLogger().isEnabledFor(Level.WARN)) {
			writeDetailLogHeader(modelId);
			getLogger().warn(" | " + modelId + " | " + reachId + " | " + rowNumber + " | WARN | " + msg);
		}
	}
	
	@Override
	public void recordRowTrace(Long modelId, Long reachId, Integer rowNumber, String msg) {
		if (getLogger().isEnabledFor(Level.TRACE)) {
			writeDetailLogHeader(modelId);
			getLogger().trace(" | " + modelId + " | " + reachId + " | " + rowNumber + " | TRACE | " + msg);
		}
	}
	
	@Override
	public void recordError(Long modelId, String msg) {
		if (failedTestIsOnlyAWarning) {
			warnWhichWereFailsCnt++;
			recordWarn(modelId, msg, true);
		} else {
			failCnt++;
			writeDetailLogHeader(modelId);
			getLogger().error(" | " + modelId + " | NA | NA | FAIL | " + msg);
		}
	}
	
	@Override
	public void recordWarn(Long modelId, String msg) {
		warnCnt++;
		writeDetailLogHeader(modelId);
		getLogger().warn(" | " + modelId + " | NA | NA | WARN | " + msg);
	}
	
	/**
	 * This generic trace method does not force the the format header to be written
	 * and should not be used during the test, since its output does not follow
	 * the standard format and will 'break' the output for parsing.
	 * @param modelId
	 * @param msg 
	 */
	@Override
	public void recordTrace(Long modelId, String msg) {
		if (getLogger().isEnabledFor(Level.TRACE)) {
			getLogger().trace("Model " + modelId + ": " + msg);
		}
	}
	
	@Override
	public void recordTestException(Long modelId, Exception exception, String msg) {
		failCnt++;
		writeDetailLogHeader(modelId);
		getLogger().error(" | " + modelId + " | NA | NA | FAIL | " + msg, exception);
	}
	
	
	/**
	 * Optionally forces the log level to WARN so that the output will for sure be written.
	 * 
	 * @param modelId
	 * @param reachId
	 * @param rowNumber
	 * @param msg
	 * @param forceOutput 
	 */
	protected void recordRowWarn(Long modelId, Long reachId, Integer rowNumber, String msg, boolean forceOutput) {
		warnCnt++;
		
		Logger log = getLogger();
		Level orgLevel = log.getLevel();
		if (forceOutput) log.setLevel(Level.WARN);
		
		if (getLogger().isEnabledFor(Level.WARN)) {
			writeDetailLogHeader(modelId);
			getLogger().warn(" | " + modelId + " | " + reachId + " | " + rowNumber + " | WARN | " + msg);
		}
		
		log.setLevel(orgLevel);
	}
	
	/**
	 * Optionally forces the log level to WARN so that the output will for sure be written.
	 * 
	 * @param modelId
	 * @param msg
	 * @param forceOutput 
	 */
	protected void recordWarn(Long modelId, String msg, boolean forceOutput) {
		warnCnt++;
		
		Logger log = getLogger();
		Level orgLevel = log.getLevel();
		if (forceOutput) log.setLevel(Level.WARN);
		
		writeDetailLogHeader(modelId);
		getLogger().warn(" | " + modelId + " | NA | NA | WARN | " + msg);
		
		log.setLevel(orgLevel);
	}
	
	/**
	 * Write the header if it has not been written already
	 * @param modelId
	 * @param log 
	 */
	private void writeDetailLogHeader(Long modelId) {
		if (! logDetailHeaderWritten) { 
			logDetailHeaderWritten = true;
			doWriteDetailLogHeader(modelId);
		}
	}
	
	/**
	 * Write the footer if the header was written
	 * @param modelId
	 * @param log 
	 */
	private void writeDetailLogFooter(Long modelId) {
		if (logDetailHeaderWritten) { 
			doWriteDetailLogFooter(modelId);
		}
	}
	
	
	protected void doWriteDetailLogHeader(Long modelId) {
		Logger log = getLogger();
		Level orgLevel = log.getLevel();
		log.setLevel(Level.INFO);
		
		log.info("***** Detail messages from '" + this.getClass().getName() + "' for model " + modelId);
		log.info("***** Format on next line: ");
		log.info(" | model ID | reach ID | reach row number in model | Message Type | validation error message");
		
		log.setLevel(orgLevel);
	}
	
	protected void doWriteDetailLogFooter(Long modelId) {
		Logger log = getLogger();
		Level orgLevel = log.getLevel();
		log.setLevel(Level.INFO);
		
		log.info("***** End detail messages from " + this.getClass().getName() + " for model " + modelId);
		
		log.setLevel(orgLevel);
	}
	
	public int getIndividualFailures() {
		return failCnt;
	}

	public int getIndividualWarnings() {
		return warnCnt;
	}
	
	public Logger getLogger() {
		if (testLog == null) {
			testLog = Logger.getLogger(this.getClass());
			testLog.setLevel(runner.getTestLogLevel());
		}
		
		return testLog;
	}
	
}

