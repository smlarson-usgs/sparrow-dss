package gov.usgswim.sparrow.service.predict.aggregator;

/**
 * Allowed aggregate type classification of whether a column of PredictResult is a sum of other columns
 *
 * @author ilinkuo
 *
 */
public enum AggregateType {
	avg,
	sum,
	max,
	min,
	none;
//TODO:  This really should be specified in the parse pkg, where all the other domain info is.
	//And should be part of the Analysis data.
	public static AggregateType parse(String value) {
		if (value == null) {
			return none;
		}
		try {
			return Enum.valueOf(AggregateType.class, value);
		} catch (Exception e) {
			return none;
		}
	}
}
