package gov.usgswim.sparrow.validation;

/**
 *
 * @author eeverman
 */
class ModelValidationResult {
	public int modelsRun;
	public int modelsFailed;
	public boolean runError = false;
	public boolean configError = false;

	public void addResultToTotal(ModelValidationResult r) {
		this.modelsRun += r.modelsRun;
		this.modelsFailed += r.modelsFailed;
		this.runError = this.runError || r.runError;
		this.configError = this.configError || r.configError;
	}
}
