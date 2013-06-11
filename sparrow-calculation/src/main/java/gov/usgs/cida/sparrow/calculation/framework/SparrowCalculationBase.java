package gov.usgs.cida.sparrow.calculation.framework;

import gov.usgs.cida.sparrow.calculation.framework.CalculationResult;
import gov.usgs.cida.sparrow.calculation.framework.SparrowCalculationRunner;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Implements all the plumbing portion of a ModelValidator.
 *
 * Subclasses only need to implement:
 * <ul>
 * <li>calcModel() - The main test method</li>
 * <li>requiresDb() - Return true if the test requires db access</li>
 * <li>requiresTextFile() - Return true if a text file is needed for comparison</li>
 * </ul>
 *
 * @author eeverman
 */
public abstract class SparrowCalculationBase implements Calculator {

	protected SparrowCalculationRunner runner;
	protected boolean failedCalcIsOnlyAWarning = false;


	public final static String ID_COL_KEY = "id_col";	//Table property of the key column

	final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = Integer.MAX_VALUE;
	final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = Integer.MAX_VALUE;

	/** The comparator used to determine values are equal */

	protected CalculationResult result;

	private Logger calcLog;

	/**
	 * The failedCalcIsOnlyAWarning flag indicates that a calc failure should not
	 * mark the calc as failing, only a warning.  This is useful for calculations that
	 * have lots of questionable values (i.e., values are off by 10% for thousands
	 * of values).
	 *
	 * @param failedCalcIsOnlyAWarning If true, a failed calculation will only be counted as a warning.
	 */
	public SparrowCalculationBase(boolean failedCalcIsOnlyAWarning) {
		this.failedCalcIsOnlyAWarning = failedCalcIsOnlyAWarning;
	}

	@Override
	public boolean initCalc(SparrowCalculationRunner runner) throws Exception {
		this.runner = runner;
		return true;
	}

	@Override
	public void beforeEachCalc(Long modelId) {
		recordTrace(modelId, "Beginning Calculation '" + this.getClass().getName() + "'");
		result = new CalculationResult(modelId, this.getClass().getSimpleName());
	}

	@Override
	public void afterEachCalc(Long modelId) {

		if (result.isPerfect()) {
			recordTrace(modelId, "Completed Calculation '" + this.getClass().getName() + "' with no failures or warnings.");
		} else if (result.isOk()) {
			recordTrace(modelId, "Completed Calculation '" + this.getClass().getName() + "' with SOME WARNINGS.");
		} else {
			recordTrace(modelId, "Completed Calculation '" + this.getClass().getName() + "' with SOME ERRORS.");
		}

	}


	//
	// Event recording
	@Override
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber, String msg) {
		if (failedCalcIsOnlyAWarning) {
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
		if (failedCalcIsOnlyAWarning) {
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
	 * and should not be used during the calculation, since its output does not follow
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
	public void recordCalcException(Long modelId, Exception exception, String msg) {
		result.addFatalError();
		writeModelLevelMessage(Level.FATAL, modelId, true, exception, msg);
	}


	//Full detail row implementations
	@Override
	public void recordRowError(Long modelId, Long reachId, Integer rowNumber,
			Object expected, Object actual, String expectName, String actualName,
			Boolean shoreReach, Boolean ifTran, String msg) {

		if (failedCalcIsOnlyAWarning) {
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
	 * Optionally forces the log level to INFO so that the output will for sure be written.
	 *
	 * @param modelId
	 * @param msg
	 * @param forceOutput
	 */
	protected void recordInfo(Long modelId, String msg, boolean forceOutput) {
		writeModelLevelMessage(Level.INFO, modelId, forceOutput, msg);
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


	protected boolean logIsEnabledForError() {
		return logIsEnabledFor(Level.ERROR);
	}

	protected boolean logIsEnabledForWarn() {
		return logIsEnabledFor(Level.WARN);
	}

	protected boolean logIsEnabledForDebug() {
		return logIsEnabledFor(Level.DEBUG);
	}

	protected boolean logIsEnabledForTrace() {
		return logIsEnabledFor(Level.TRACE);
	}

	protected boolean logIsEnabledFor(Level level) {
		return getLogger().isEnabledFor(level);
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
			return "1";
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
			return "1";
		} else {
			//Object equals comparison
			if (expected.equals(actual)) {
				return "0";
			} else {
				return "1";
			}
		}

	}

	private Double variance(Double expected, Double actual) {
		if (expected == null && actual == null) {
			return 0D;
		} else if (expected == null || actual == null) {
			return 1D;
		} else if (expected == 0D && actual != 0D) {
			return 1D;
		} else {
			Double v = Math.abs((expected - actual) / expected);
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
		headFormat += " Expected Val | Actual Val | Variance | ";
		headFormat += " Expect Name / Actual Name | Validation Message |";

		log.info(headFormat);

		log.setLevel(orgLevel);

		runner.setResultHeaderWritten(true);
	}

	public Logger getLogger() {
		if (calcLog == null) {
			calcLog = Logger.getLogger(this.getClass());
			calcLog.setLevel(runner.getCalcLogLevel());
		}

		return calcLog;
	}

}

