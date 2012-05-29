package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;


public enum AggregationLevel implements NamedEnum<AggregationLevel> {
	NONE(null, "NA", "Not Defined"),
	REACH(null, "reach", "No aggregation - individual reaches"),
	STATE(null, "state", "Aggregate all reaches in a State"),
	HUC2(HucLevel.HUC2.getLevel(), HucLevel.HUC2.getName(), HucLevel.HUC2.getDescription()),
	HUC4(HucLevel.HUC4.getLevel(), HucLevel.HUC4.getName(), HucLevel.HUC4.getDescription()),
	HUC6(HucLevel.HUC6.getLevel(), HucLevel.HUC6.getName(), HucLevel.HUC6.getDescription()),
	HUC8(HucLevel.HUC8.getLevel(), HucLevel.HUC8.getName(), HucLevel.HUC8.getDescription());
	
	private Integer hucLevel;
	private String name;
	private String description;
	
	AggregationLevel(Integer hucLevel, String name, String description) {
		this.hucLevel = hucLevel;
		this.name = name;
		this.description = description;
	}

	public Integer getHucLevel() {
		return hucLevel;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public AggregationLevel fromString(String name) {
		for (AggregationLevel val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public AggregationLevel fromStringIgnoreCase(String name) {
		for (AggregationLevel val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}

	@Override
	public AggregationLevel getDefault() {
		return NONE;
	}
	
	@Override
	public String getDescription() {
		return description;
	}


}
