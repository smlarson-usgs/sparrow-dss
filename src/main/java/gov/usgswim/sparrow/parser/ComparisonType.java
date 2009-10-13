/**
 * Option for the PredictionContext schema for:
 * PredictionContext/analysis/select/nominalComparison@type
 */
package gov.usgswim.sparrow.parser;

public enum ComparisonType {
	none,
	percent,
	absolute;

	public boolean isNone() {
		return this.equals(none);
	}
}