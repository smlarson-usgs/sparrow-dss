package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;

/**
 * A huc level 2-8.
 * 
 * Note that the enum constant names (i.e., HUC2) match db tables, so do not
 * refactor these names.  Not to be confused w/ the enum 'name' instance variable,
 * which is a more human readable name.
 * 
 * @author eeverman
 *
 */
public enum HUCType implements NamedEnum<HUCType> {
	
	/* See class note above */
	HUC2(2, "huc2", "HUC Level 2"),
	HUC4(4, "huc4", "HUC Level 4"),
	HUC6(6, "huc6", "HUC Level 6"),
	HUC8(8, "huc8", "HUC Level 8");
	
	private Integer level;
	private String name;
	private String description;
	
	HUCType(Integer level, String name, String description) {
		this.level = level;
		this.name = name;
		this.description = description;
	}

	public Integer getLevel() {
		return level;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public HUCType fromString(String name) {
		for (HUCType val : values()) {
			if (val.name.equals(name) || val.name().equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public HUCType fromStringIgnoreCase(String name) {
		for (HUCType val : values()) {
			if (val.name.equalsIgnoreCase(name) || val.name().equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public HUCType getDefault() {
		return null;
	}
	
	@Override
	public String getDescription() {
		return description;
	}


}
