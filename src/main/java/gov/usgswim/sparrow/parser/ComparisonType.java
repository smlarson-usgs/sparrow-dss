/**
 * Option for the PredictionContext schema for:
 * PredictionContext/analysis/select/nominal-comparison@type
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