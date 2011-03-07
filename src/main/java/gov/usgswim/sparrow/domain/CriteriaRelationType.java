package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;

/**
 * The type of relation of the criteria to the value, used to select reaches
 * in a Criteria instance.
 * 
 * @author eeverman
 */
public enum CriteriaRelationType implements NamedEnum<CriteriaRelationType> {
	
	IN("in", "within the region"),
	UPSTREAM("upstream", "upstream of the stream"),
	UNKNOWN("unknown", "The LogicalSetCriteriaType is unspecified");
	
	private String name;
	private String description;
	
	CriteriaRelationType(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Will return UNKNOWN if a match cannot be found.
	 */
	@Override
	public CriteriaRelationType fromString(String name) {
		for (CriteriaRelationType val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return UNKNOWN;
	}
	
	/**
	 * Will return UNKNOWN if a match cannot be found.
	 */
	@Override
	public CriteriaRelationType fromStringIgnoreCase(String name) {
		for (CriteriaRelationType val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return UNKNOWN;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public CriteriaRelationType getDefault() {
		return UNKNOWN;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

}
