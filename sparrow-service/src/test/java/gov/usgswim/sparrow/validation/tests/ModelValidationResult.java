package gov.usgswim.sparrow.validation.tests;

/**
 *
 * @author eeverman
 */
public class ModelValidationResult {
	public int modelsRun;
	public int modelsFailed;
	public int testsRun;
	public int testsFailed;
	public int warnings;
	public int warningsWereFails;
	
	public boolean runError = false;
	public boolean configError = false;

	public void addModelToTotal(ModelValidationResult r) {
		this.modelsRun += r.modelsRun;
		this.modelsFailed += r.modelsFailed;
		this.runError = this.runError || r.runError;
		this.configError = this.configError || r.configError;
	}
	
	public void addTestToTotal(ModelValidationResult r) {
		this.testsRun += r.testsRun;
		this.testsFailed += r.testsFailed;
		this.warnings += r.warnings;
		this.warningsWereFails += r.warningsWereFails;
		
		this.runError = this.runError || r.runError;
		this.configError = this.configError || r.configError;
	}
	
	public void addTestToSingleModelTotal(ModelValidationResult r) {
		this.modelsRun = 1;
		this.testsRun += r.testsRun;
		this.testsFailed += r.testsFailed;
		
		if (this.testsFailed > 0) this.modelsFailed = 1;
		
		this.runError = this.runError || r.runError;
		this.configError = this.configError || r.configError;
	}
}
