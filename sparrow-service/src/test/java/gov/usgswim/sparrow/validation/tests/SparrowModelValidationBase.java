package gov.usgswim.sparrow.validation.tests;

import gov.usgswim.sparrow.validation.tests.ModelValidator;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.validation.SparrowModelValidationRunner;
import org.apache.commons.lang.math.NumberUtils;
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

	protected TestResult result;
	
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
		result = new TestResult(modelId, this.getClass().getSimpleName());
	}
	
	@Override
	public void afterEachTest(Long modelId) {
		
		if (result.isPerfect()) {
			recordTrace(modelId, "Completed Test '" + this.getClass().getName() + "' with no failures or warnings.");
		} else if (result.isOk()) {
			recordTrace(modelId, "Completed Test '" + this.getClass().getName() + "' with SOME WARNINGS.");
		} else {
			recordTrace(modelId, "Completed Test '" + this.getClass().getName() + "' with SOME ERRORS.");
		}
		
		
		//If a test modifies the cache such that it should not be used for other
		//tests, clear it here.
		//SharedApplication.getInstance().clearAllCaches();
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
	
	
	//
	// Event recording
	@Override
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber, String msg) {
		if (failedTestIsOnlyAWarning) {
			result.addErrorAsWarn();
			writeRowLevelMessage(Level.WARN, modelId, reachId, rowNumber,
				null, null, null, null, null, null, true, msg);
			
		} else {
			
			result.addError();
			writeRowLevelMessage(Level.ERROR, modelId, reachId, rowNumber,
				null, null, null, null, null, null, false, msg);
		}
	}
	
	@Override
	public void recordRowWarn(Long modelId, Long reachId, Integer rowNumber, String msg) {
		result.addWarn();
		writeRowLevelMessage(Level.TRACE, modelId, reachId, rowNumber,
				null, null, null, null, null, null, false, msg);
	}
	
	@Override
	public void recordRowDebug(Long modelId, Long reachId, Integer rowNumber, String msg) {
		writeRowLevelMessage(Level.DEBUG, modelId, reachId, rowNumber,
				null, null, null, null, null, null, false, msg);
	}
	
	@Override
	public void recordRowTrace(Long modelId, Long reachId, Integer rowNumber, String msg) {
		writeRowLevelMessage(Level.TRACE, modelId, reachId, rowNumber,
				null, null, null, null, null, null, false, msg);
	}
	
	@Override
	public void recordError(Long modelId, String msg) {
		if (failedTestIsOnlyAWarning) {
			result.addErrorAsWarn();
			writeModelLevelMessage(Level.WARN, modelId, true, msg);
		} else {
			
			result.addError();
			writeModelLevelMessage(Level.ERROR, modelId, false, msg);
		}
	}
	
	@Override
	public void recordWarn(Long modelId, String msg) {
		result.addWarn();
		writeModelLevelMessage(Level.WARN, modelId, false, msg);
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
		result.addFatalError();
		writeModelLevelMessage(Level.FATAL, modelId, true, exception, msg);
	}
	
	
	//Full detail row implementations
	@Override
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber, 
			Object expected, Object actual, String expectName, String actualName, 
			Boolean shoreReach, Boolean ifTran, String msg) {

		if (failedTestIsOnlyAWarning) {
			result.addErrorAsWarn();
			writeRowLevelMessage(Level.WARN, modelId, reachId, rowNumber,
				expected, actual, expectName, actualName,
				shoreReach, ifTran, true, msg);
			
		} else {
			
			result.addError();
			writeRowLevelMessage(Level.ERROR, modelId, reachId, rowNumber,
				expected, actual, expectName, actualName,
				shoreReach, ifTran, false, msg);
		}
	}
		
	@Override
	public void recordRowWarn(Long modelId, Long reachId, Integer rowNumber, 
			Object expected, Object actual, String expectName, String actualName, 
			Boolean shoreReach, Boolean ifTran, String msg) {
		
		result.addWarn();
		writeRowLevelMessage(Level.WARN, modelId, reachId, rowNumber,
			expected, actual, expectName, actualName,
			shoreReach, ifTran, false, msg);
	}
	
	@Override
	public void recordRowDebug(Long modelId, Long reachId, Integer rowNumber, 
			Object expected, Object actual, String expectName, String actualName, 
			Boolean shoreReach, Boolean ifTran, String msg) {
		
		writeRowLevelMessage(Level.DEBUG, modelId, reachId, rowNumber,
			expected, actual, expectName, actualName,
			shoreReach, ifTran, false, msg);
	}
	
	@Override
	public void recordRowTrace(Long modelId, Long reachId, Integer rowNumber, 
			Object expected, Object actual, String expectName, String actualName, 
			Boolean shoreReach, Boolean ifTran, String msg) {
		
		writeRowLevelMessage(Level.TRACE, modelId, reachId, rowNumber,
			expected, actual, expectName, actualName,
			shoreReach, ifTran, false, msg);
	}
	
	/**
	 * Optionally forces the log level to WARN so that the output will for sure be written.
	 * 
	 * @param modelId
	 * @param msg
	 * @param forceOutput 
	 */
	protected void recordWarn(Long modelId, String msg, boolean forceOutput) {
		result.addWarn();
		writeModelLevelMessage(Level.WARN, modelId, forceOutput, msg);
	}
	
	
	private void writeModelLevelMessage(Level level, Long modelId, boolean forceOutput, String msg) {
		writeModelLevelMessage(level, modelId, forceOutput, null, msg);
	}
		
	private void writeModelLevelMessage(Level level, Long modelId, boolean forceOutput, Exception exception, String msg) {

		writeRowLevelMessage(
			level, modelId, null, null,
			null, null, null, null,
			null, null,
			forceOutput, msg);
		
		if (exception != null) {
			getLogger().fatal(msg, exception);
		}
	}
		
	private void writeRowLevelMessage(Level level,
			Long modelId, Long reachId, Integer rowNumber,
			Object expected, Object actual, String expectName, String actualName,
			Boolean shoreReach, Boolean ifTran,
			boolean forceOutput, String msg) {

		Logger log = getLogger();
		Level orgLevel = log.getLevel();
		if (forceOutput) log.setLevel(level);
		
		if (getLogger().isEnabledFor(level)) {
			writeDetailLogHeader(modelId);
			String formattedMsg = " | " + nvl(modelId) + " | " + nvl(reachId) + " | " + nvl(rowNumber) + " | ";
			formattedMsg += nvl(shoreReach) + " | " + nvl(ifTran) + " | " + level.toString() + " | ";
			formattedMsg += nvl(expected) + " | " + nvl(actual) + " | " + variance(expected, actual) + " | ";
			formattedMsg += nvl(expectName) + "/" + nvl(actualName) + " | " + msg + " | ";
					
					
			log.log(level, formattedMsg);
		}
		
		log.setLevel(orgLevel);
	}
	
	private String nvl(Object cleanNullValue) {
		if (cleanNullValue == null) return "";
		return cleanNullValue.toString();
	}
	
	private String variance(Object expected, Object actual) {
		if (expected == null && actual == null) {
			return "";
		} else if (expected == null || actual == null) {
			return "100";
		} else if (expected instanceof Number && actual instanceof Number) {
			Number e = (Number) expected;
			Number a = (Number) actual;
			
			return variance(e.doubleValue(), a.doubleValue()).toString();

		} else if (NumberUtils.isNumber(expected.toString()) && NumberUtils.isNumber(actual.toString())) {
			//Both numbers as strings
			Number e = Double.parseDouble(expected.toString());
			Number a = Double.parseDouble(actual.toString());
			
			return variance(e.doubleValue(), a.doubleValue()).toString();
			
		} else if (NumberUtils.isNumber(expected.toString()) || NumberUtils.isNumber(actual.toString())) {
			//Only one is a number
			return "100";
		} else {
			//Object equals comparison
			if (expected.equals(actual)) {
				return "0";
			} else {
				return "100";
			}
		}
		
	}
	
	private Double variance(Double expected, Double actual) {
		if (expected == null && actual == null) {
			return 0D;
		} else if (expected == null || actual == null) {
			return 100D;
		} else if (expected == 0D && actual != 0D) {
			return 100D;
		} else {
			Double v = Math.abs((expected - actual) / expected) * 100D;
			return v;
		}
	}
	
	/**
	 * Write the footer if the header was written
	 * @param modelId
	 * @param log 
	 */
	private void writeDetailLogHeader(Long modelId) {
		if (! runner.isResultHeaderWritten()) { 
			doWriteDetailLogHeader(modelId);
		}
	}
	
	
	protected void doWriteDetailLogHeader(Long modelId) {
		Logger log = getLogger();
		Level orgLevel = log.getLevel();
		log.setLevel(Level.INFO);
		
		String headFormat = " | model ID | reach ID | row | ";
		headFormat += " Shore? | IfTran? | Msg Type | ";
		headFormat += " Expected Val | Actual Val | Variance % | ";
		headFormat += " Expect Name / Actual Name | Validation Message |";

		log.info(headFormat);
		
		log.setLevel(orgLevel);
		
		runner.setResultHeaderWritten(true);
	}
	
	public Logger getLogger() {
		if (testLog == null) {
			testLog = Logger.getLogger(this.getClass());
			testLog.setLevel(runner.getTestLogLevel());
		}
		
		return testLog;
	}
	
}
