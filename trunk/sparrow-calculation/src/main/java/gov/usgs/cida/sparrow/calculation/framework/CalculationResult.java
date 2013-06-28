package gov.usgs.cida.sparrow.calculation.framework;

/**
 *
 * @author eeverman
 */
public class CalculationResult {

//	public int modelsRun;
//	public int modelsFailed;
//	public int testsRun;
//	public int testsFailed;

	private Long modelId;
	private String calcName;

	private int fatalError = 0;
	private int configError = 0;
	private int errors;
	private int warnings;
	private int warningsWereFails;


	public CalculationResult(Long modelId, String calcName) {
		this.modelId = modelId;
		this.calcName = calcName;
	}

	public Long getModelId() {
		return modelId;
	}

	public String getCalcName() {
		return calcName;
	}

	public void addFatalError() { fatalError++; }
	public void addConfigError() { configError++; }

	public void addError() { errors++; }
	public void addWarn() { warnings++; }
	public void addErrorAsWarn() { warningsWereFails++; warnings++; }


	public boolean isPerfect() {
		return isOk() && ! isWarn();
	}

	public boolean isOk() {
		return !isFatal() && ! isConfigError() && ! isError();
	}

	public boolean isAnyError() {
		return ! isOk();
	}

	public boolean isFatal() {
		return fatalError > 0;
	}

	public boolean isConfigError() {
		return configError > 0;
	}

	public boolean isError() {
		return errors > 0;
	}

	public boolean isWarn() {
		return warnings > 0;
	}

	public boolean isErrorsAsWarn() {
		return warningsWereFails > 0;
	}

	public int getErrorCount() {
		return errors;
	}

	public int getWarnCount() {
		return warnings;
	}

	public int getErrorsAsWarnCount() {
		return warningsWereFails;
	}


}
