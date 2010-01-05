package gov.usgswim.sparrow.revised.steps;

import gov.usgswim.sparrow.revised.EngineInstructions;

public class CompareStep extends Step {

	public final EngineInstructions basePlan;
	public final EngineInstructions comparingPlan;
	public final Object compareOptions;

	public CompareStep(EngineInstructions basePlan,
			EngineInstructions comparingPlan, Object compareOptions) {
		super(Type.compare, null);
		this.basePlan = basePlan;
		this.comparingPlan = comparingPlan;
		this.compareOptions = compareOptions;
	}

}
