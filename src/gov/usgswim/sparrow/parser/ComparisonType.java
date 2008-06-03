/**
 * Option for the prediction-context schema for:
 * prediction-context/analysis/select/nominal-comparison@type
 */
package gov.usgswim.sparrow.parser;

public enum ComparisonType {
	none,
	percent,
	absolute;
	
	public boolean isNone() {
		return this.name().equals("none");
	}
}