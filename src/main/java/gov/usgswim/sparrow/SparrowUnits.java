package gov.usgswim.sparrow;

public enum SparrowUnits {
	METERS("m", "meters"),
	CFS("ft³⋅sec⁻¹", "feet³ ⋅ second⁻¹"),
	FPS("ft⋅sec⁻¹", "feet ⋅ second⁻¹"),
	KG_PER_YEAR("kg⋅year⁻¹", "kg ⋅ year⁻¹"),
	MG_PER_L("mg⋅L⁻¹", "mg ⋅ L⁻¹"),
	SQR_KM("km²", "km²"),
	KG_PER_SQR_KM_PER_YEAR("kg⋅km⁻²⋅yr⁻¹", "kg ⋅ km⁻² ⋅ year⁻¹"),
	PERCENT("%", "percent"),
	FRACTION("(fraction)", "fraction");

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
