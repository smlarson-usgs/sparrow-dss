package gov.usgswim.sparrow.revised.steps;

import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction.Levels;

public class AggregatePreviousResultStep extends Step {
	public final Levels agLevel;
	public final AggregationFunction agFunct;

	public AggregatePreviousResultStep(Levels agLevel, AggregationFunction agFunct) {
		super(Type.aggregate, null);
		this.agLevel = agLevel;
		this.agFunct = agFunct;
	}
}
