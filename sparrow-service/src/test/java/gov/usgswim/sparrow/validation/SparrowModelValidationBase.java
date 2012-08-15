package gov.usgswim.sparrow.validation;

import gov.usgswim.sparrow.service.SharedApplication;
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
	
	
	public final static String ID_COL_KEY = "id_col";	//Table property of the key column

	final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = Integer.MAX_VALUE;
	final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = Integer.MAX_VALUE;
	
	protected boolean logDetailHeaderWritten = false;

	protected int individualFailures = 0;
	protected int individualWarnings = 0;
	
	private Logger testLog;

	@Override
	public boolean initTest(SparrowModelValidationRunner runner) throws Exception {
		this.runner = runner;
		return true;
	}
	
	@Override
	public void beforeEachTest(Long modelId) {
		recordTrace(modelId, "Begging test");
		SharedApplication.getInstance().clearAllCaches();
	}
	
	@Override
	public void afterEachTest(Long modelId) {
		this.writeDetailLogFooter(modelId);
		recordTrace(modelId, "Test Complete");
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
		individualFailures++;
		writeDetailLogHeader(modelId);
		getLogger().error(" | " + modelId + " | " + reachId + " | " + rowNumber + " | FAIL | " + msg);
	}
	
	@Override
	public void recordRowErrorDebug(Long modelId, Long reachId, Integer rowNumber, String msg) {
		individualFailures++;
		
		if (getLogger().isDebugEnabled()) {
			writeDetailLogHeader(modelId);
			getLogger().debug(" | " + modelId + " | " + reachId + " | " + rowNumber + " | ERROR DEBUG | " + msg);
		}
	}
	
	@Override
	public void recordRowWarn(Long modelId, Long reachId, Integer rowNumber, String msg) {
		individualWarnings++;
		
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
	public void recordError(Long modelId, Exception exception, String msg) {
		individualFailures++;
		writeDetailLogHeader(modelId);
		getLogger().error(" | " + modelId + " | NA | NA | FAIL | " + msg, exception);
	}
	
	@Override
	public void recordError(Long modelId, String msg) {
		individualFailures++;
		writeDetailLogHeader(modelId);
		getLogger().error(" | " + modelId + " | NA | NA | FAIL | " + msg);
	}
	
	@Override
	public void recordWarn(Long modelId, String msg) {
		individualWarnings++;
		writeDetailLogHeader(modelId);
		getLogger().warn(" | " + modelId + " | NA | NA | WARN | " + msg);
	}
	
	@Override
	public void recordTrace(Long modelId, String msg) {
		if (getLogger().isEnabledFor(Level.TRACE)) {
			writeDetailLogHeader(modelId);
			getLogger().trace("Model " + modelId + ": " + msg);
		}
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
		
		log.info("** Test detail messages from " + this.getClass().getName() + " has at lease some detail out for model " + modelId);
		log.info("** Format on next line: ");
		log.info(" | model ID | reach ID | reach row number in model | Message Type | validation error message");
		
		log.setLevel(orgLevel);
	}
	
	protected void doWriteDetailLogFooter(Long modelId) {
		Logger log = getLogger();
		Level orgLevel = log.getLevel();
		log.setLevel(Level.INFO);
		
		log.info("** /End test detail for " + this.getClass().getName() + " for model " + modelId);
		
		log.setLevel(orgLevel);
	}
	
	public int getIndividualFailures() {
		return individualFailures;
	}

	public int getIndividualWarnings() {
		return individualWarnings;
	}
	
	public Logger getLogger() {
		if (testLog == null) {
			testLog = Logger.getLogger(this.getClass());
			testLog.setLevel(runner.getTestLogLevel());
		}
		
		return testLog;
	}
	
}

