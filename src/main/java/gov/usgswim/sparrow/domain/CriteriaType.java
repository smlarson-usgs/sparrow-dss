package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;

/**
 * The type of criterias that can be used for logical sets of reaches.
 * 
 * Note:  The enum name and name property match the column name that these are
 * pulled from: don't change either, i.e., HUC2 or huc2.
 * 
 * @author eeverman
 */
public enum CriteriaType implements NamedEnum<CriteriaType> {
	
	HUC2("huc2", "HUC Level 2", true),
	HUC4("huc4", "HUC Level 4", true),
	HUC6("huc6", "HUC Level 6", true),
	HUC8("huc8", "HUC Level 8", true),
	UPSTREAM("upstream", "This reach and all reaches upstream", false),
	UNKNOWN("unknown", "The LogicalSetCriteriaType is unspecified", false);
	
	private String name;
	private String description;
	private boolean hucCriteria;
	
	CriteriaType(String name, String description, boolean isHucCriteria) {
		this.name = name;
		this.description = description;
		this.hucCriteria = isHucCriteria;
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Will return UNKNOWN if a match cannot be found.
	 */
	@Override
	public CriteriaType fromString(String name) {
		for (CriteriaType val : values()) {
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
	public CriteriaType fromStringIgnoreCase(String name) {
		for (CriteriaType val : values()) {
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
	public CriteriaType getDefault() {
		return UNKNOWN;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	public boolean isHucCriteria() {
		return hucCriteria;
	}


}
