package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;

/**
 * A type of precision for geometry
 * 
 * @author eeverman
 *
 */
public enum GeometryPrecisionType implements NamedEnum<GeometryPrecisionType> {
	
	/* See class note above */
	DOUBLE("double", "The full accuracy from the db as a double"),
	FLOAT("float", "The full accuracy from the db as a float"),
	CONVEX("convex", "A convex approximation of the object using floats.");
	
	private String name;
	private String description;
	
	GeometryPrecisionType(String name, String description) {
		this.name = name;
		this.description = description;
	}

	
	public String getName() {
		return name;
	}

	@Override
	public GeometryPrecisionType fromString(String name) {
		for (GeometryPrecisionType val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public GeometryPrecisionType fromStringIgnoreCase(String name) {
		for (GeometryPrecisionType val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * Not the inverse of fromString - this returns the description.
	 */
	@Override
	public String toString() {
		return description;
	}

	@Override
	public GeometryPrecisionType getDefault() {
		return null;
	}
	
	@Override
	public String getDescription() {
		return description;
	}


}
