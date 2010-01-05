package gov.usgswim.sparrow.revised.steps.aggregation;

public class AggregationFunction {
	public static enum Levels{
		HUC2, HUC4, HUC6, HUC8
	}

	public static enum AggregationFunctionTypes{
		AVERAGE, MIN, MAX, AREA_WEIGHTED_AVERAGE, MEDIAN, SUM;

		public String[] options;

		public AggregationFunction get(String... options) {
			this.options = options;
			return new AggregationFunction();
		}
	}
}
