package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.datatable.NamedEnum;
import java.math.BigDecimal;

/**
 * The type of constituent, which affects what detection limit we use for concentration
 * mapping.
 * 
 * @author eeverman
 */
public enum ConstituentType implements NamedEnum<ConstituentType> {
	TOTAL_NITROGEN("Nitrogen", null, new BigDecimal(".05")),
	TOTAL_PHOSPHORUS("Phosphorus", null, new BigDecimal(".01")),
	TOTAL_DISSOLVED_SOLIDS("Total Dissolved Solids", null, new BigDecimal("10")),
	SUSPENDED_SEDIMENT("Suspended Sediment", null, new BigDecimal(".001")),
	ORGANIC_CARBON("Organic Carbon", null, new BigDecimal(".01"));
	


	private final String name;
	private final String description;
	private final BigDecimal concentrationDetectionLimit;
	
	ConstituentType(String name, String description, BigDecimal concentrationDetectionLimit) {
		this.name = name;
		this.description = (description != null)?description:name;
		this.concentrationDetectionLimit = concentrationDetectionLimit;
	}
	

	public String getName() {
		return name;
	}

	@Override
	public ConstituentType fromString(String name) {
		for (ConstituentType val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public ConstituentType fromStringIgnoreCase(String name) {
		for (ConstituentType val : values()) {
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
	public ConstituentType getDefault() {
		return null;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * The lower bound of what should be reported when mapping/reporting
	 * concentrations.
	 * 
	 * May return null if unknown.
	 * 
	 * @return 
	 */
	public BigDecimal getConcentrationDetectionLimit() {
		return concentrationDetectionLimit;
	}
}
