package gov.usgs.cida.datatable;

/**
 * An enum that can be added as a table property to specify how the values in
 * a column are related to other values in the same column.
 * 
 * @author eeverman
 *
 */
public enum RelationType {
	rel_percent("Relative Percent",
			"Within this column, each value is the percentage of this row to sum total "
			+ "of all other rows, for some defined column."),
	rel_fraction("Relative Fraction",
			"Within this column, each value is the fraction of this row to sum total "
			+ "of all other rows, for some defined column."),
	independant("Independant Value", 
			"Each value in this column is an independant value, not related to other column."),
	none("No Relation", "The relation of the each value in this column to other values is not defined, thus by default is independant.");

	private String fullName;
	private String description;
	
	public final static String XML_ATTRIB_NAME = "rel-type";
	
	private RelationType(String fullName, String description) {
		this.fullName = fullName;
		this.description = description;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static RelationType parse(String name) {
		if (name == null) {
			return none;
		}
		try {
			return Enum.valueOf(RelationType.class, name);
		} catch (Exception e) {
			return none;
		}
	}
	
	
}
