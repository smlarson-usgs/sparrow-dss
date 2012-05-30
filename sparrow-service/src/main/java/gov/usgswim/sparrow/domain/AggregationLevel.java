package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;


public enum AggregationLevel implements NamedEnum<AggregationLevel> {
	NONE(null, "NA", "Not Defined", false, false, false),
	REACH(null, "reach", "No aggregation - individual reaches", false, false, true),
	STATE(null, "state", "Aggregate all reaches in a State", false, true, false),
	HUC2(HucLevel.HUC2.getLevel(), HucLevel.HUC2.getName(), HucLevel.HUC2.getDescription(), true, false, false),
	HUC4(HucLevel.HUC4.getLevel(), HucLevel.HUC4.getName(), HucLevel.HUC4.getDescription(), true, false, false),
	HUC6(HucLevel.HUC6.getLevel(), HucLevel.HUC6.getName(), HucLevel.HUC6.getDescription(), true, false, false),
	HUC8(HucLevel.HUC8.getLevel(), HucLevel.HUC8.getName(), HucLevel.HUC8.getDescription(), true, false, false);
	
	private final Integer hucLevel;
	private final String name;
	private final String description;

	private final boolean _isHuc;
	private final boolean _isPolitical;
	private final boolean _isReach;
	
	AggregationLevel(Integer hucLevel, String name, String description, boolean isAHuc, boolean isAPoliticalRegion, boolean isAReach) {
		this.hucLevel = hucLevel;
		this.name = name;
		this.description = description;
		
		//Classifiers
		this._isHuc = isAHuc;
		this._isPolitical = isAPoliticalRegion;
		this._isReach = isAReach;
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
	
	public boolean isHuc() {
		return _isHuc;
	}

	public boolean isPolitical() {
		return _isPolitical;
	}

	public boolean isReach() {
		return _isReach;
	}



}
