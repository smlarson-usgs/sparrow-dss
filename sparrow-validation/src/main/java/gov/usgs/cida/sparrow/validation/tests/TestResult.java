package gov.usgs.cida.sparrow.validation.tests;

/**
 *
 * @author eeverman
 */
public class TestResult {

//	public int modelsRun;
//	public int modelsFailed;
//	public int testsRun;
//	public int testsFailed;
	
	private Long modelId;
	private String testName;
	
	private int fatalError = 0;
	private int configError = 0;
	private int errors;
	private int warnings;
	private int warningsWereFails;
	

	public TestResult(Long modelId, String testName) {
		this.modelId = modelId;
		this.testName = testName;
	}
	
//	public void addModelToTotal(TestResult r) {
//		this.modelsRun += r.modelsRun;
//		this.modelsFailed += r.modelsFailed;
//		this.fatalError = this.fatalError || r.fatalError;
//		this.configError = this.configError || r.configError;
//	}
//	
//	public void addTestToTotal(TestResult r) {
//		this.testsRun += r.testsRun;
//		this.testsFailed += r.testsFailed;
//		this.warnings += r.warnings;
//		this.warningsWereFails += r.warningsWereFails;
//		
//		this.fatalError = this.fatalError || r.fatalError;
//		this.configError = this.configError || r.configError;
//	}
//	
//	public void addTestToSingleModelTotal(TestResult r) {
//		this.modelsRun = 1;
//		this.testsRun += r.testsRun;
//		this.testsFailed += r.testsFailed;
//		
//		if (this.testsFailed > 0) this.modelsFailed = 1;
//		
//		this.fatalError = this.fatalError || r.fatalError;
//		this.configError = this.configError || r.configError;
//	}
	
	
	public Long getModelId() {
		return modelId;
	}

	public String getTestName() {
		return testName;
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
