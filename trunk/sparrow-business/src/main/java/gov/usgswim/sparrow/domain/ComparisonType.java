/**
 * Option for the PredictionContext schema for:
 * PredictionContext/analysis/select/nominalComparison@type
 */
package gov.usgswim.sparrow.domain;

public enum ComparisonType {
	none,
	percent,
	percent_change,
	absolute;

	public boolean isNone() {
		return this.equals(none);
	}
}