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
        none
}
