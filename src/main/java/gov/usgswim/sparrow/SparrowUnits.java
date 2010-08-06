package gov.usgswim.sparrow;

public enum SparrowUnits {
	CFS("cu ft/sec", "cubic feet / second"),
	KG_PER_YEAR("kg/year", "kg / year"),
	MG_PER_L("mg/L", "mg / L"),
	SQR_KM("sqr km", "square km"),
	KG_PER_SQR_KM_PER_YEAR("kg/sqr km/yr", "kg / square km / year"),
	PERCENT("%", "percent");

	private String name;
	private String description;
	
	SparrowUnits(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getUserName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean isSame(Object compareMe) {
		if (compareMe == null) {
			return false;
		}
		
		if (compareMe instanceof SparrowUnits) {
			return this.equals((SparrowUnits) compareMe);
		}
		
		return name.equalsIgnoreCase(compareMe.toString());
	}
}
