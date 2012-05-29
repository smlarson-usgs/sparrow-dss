package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;


public enum HucAggregationLevel implements NamedEnum<HucAggregationLevel> {
	HUC_NONE(null, "none", "Not Defined"),
	HUC_REACH(null, "reach", "No huc level - individual reaches"),
	HUC2(HucLevel.HUC2.getLevel(), HucLevel.HUC2.getName(), HucLevel.HUC2.getDescription()),
	HUC4(HucLevel.HUC4.getLevel(), HucLevel.HUC4.getName(), HucLevel.HUC4.getDescription()),
	HUC6(HucLevel.HUC6.getLevel(), HucLevel.HUC6.getName(), HucLevel.HUC6.getDescription()),
	HUC8(HucLevel.HUC8.getLevel(), HucLevel.HUC8.getName(), HucLevel.HUC8.getDescription());
	
	private Integer level;
	private String name;
	private String description;
	
	HucAggregationLevel(Integer level, String name, String description) {
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
	public HucAggregationLevel fromString(String name) {
		for (HucAggregationLevel val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public HucAggregationLevel fromStringIgnoreCase(String name) {
		for (HucAggregationLevel val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}

	@Override
	public HucAggregationLevel getDefault() {
		return HUC_NONE;
	}
	
	@Override
	public String getDescription() {
		return description;
	}


}
