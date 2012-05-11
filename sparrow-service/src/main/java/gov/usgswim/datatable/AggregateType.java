package gov.usgswim.datatable;

/**
 * Allowed aggregate type classification of whether a column of PredictResult is a sum of other columns
 *
 * @author ilinkuo
 *
 */
public enum AggregateType {
	avg("Average", "The values in this column are the average of other values in the same row."),
	sum("Sum", "The values in this column are the sum of other values in the same row."),
	max("Maximum", "The values in this column are the maximum value of other values in the same row."),
	min("Minimum", "The values in this column are the minimum value of other values in the same row."),
	none("None", "The values in this column are not aggregate values.");

	
	private String fullName;
	private String description;
	
	public final static String XML_ATTRIB_NAME = "agg-type";
	
	private AggregateType(String fullName, String description) {
		this.fullName = fullName;
		this.description = description;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public String getDescription() {
		return description;
	}
	
	
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
