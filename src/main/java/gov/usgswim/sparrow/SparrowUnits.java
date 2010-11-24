package gov.usgswim.sparrow;

public enum SparrowUnits {
	/* Note:  all names must be in UPPER CASE to insure the parse method works. */
	METERS("m", "meters"),
	CFS("ft³⋅sec⁻¹", "feet³ ⋅ second⁻¹"),
	FPS("ft⋅sec⁻¹", "feet ⋅ second⁻¹"),
	KG_PER_YEAR("kg⋅year⁻¹", "kg ⋅ year⁻¹"),
	MG_PER_L("mg⋅L⁻¹", "mg ⋅ L⁻¹"),
	SQR_KM("km²", "km²"),
	SQR_M("m²", "m²"),
	HECTARE("hectare", "hectare"),
	KG_PER_SQR_KM_PER_YEAR("kg⋅km⁻²⋅yr⁻¹", "kg ⋅ km⁻² ⋅ year⁻¹"),
	PERCENT("%", "percent"),
	FRACTION("(fraction)", "fraction"),
	CAPITA("capita", "capita (persons)"),
	PPM_PER_SQR_KM("ppm⋅km²", "parts per million ⋅ km²"),
	UNKNOWN("unknown", "unknown"),
	UNSPECIFIED("unspecified", "unspecified")
	;

	private String userName;
	private String description;
	
	SparrowUnits(String name, String description) {
		this.userName = name;
		this.description = description;
	}
	
	public String getUserName() {
		return userName;
	}
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return userName;
	}
	
	public boolean isSame(Object compareMe) {
		if (compareMe == null) {
			return false;
		}
		
		if (compareMe instanceof SparrowUnits) {
			return this.equals((SparrowUnits) compareMe);
		}
		
		return userName.equalsIgnoreCase(compareMe.toString());
	}
	
	/**
	 * Parses the passed string into a SparrowUnit if it can.
	 * If it cannot, the type UNKNOWN is returned.
	 * If the passed value is null, the type UNSPECIFIED is returned.
	 * The conversion is not case sensitive for matching the enum name.  If
	 * the enum name is not found, the userNames will be checked (this check IS
	 * case sensitive).
	 * @param s
	 * @return
	 */
	public static SparrowUnits parse(String s) {
		SparrowUnits unit = null;
		
		if (s == null) return UNSPECIFIED;
		
		String us = s.toUpperCase();
		
		try {
			unit = valueOf(SparrowUnits.class, us);
		} catch (Exception e) {
			//Error is thrown if not found
			
			unit = parseUserName(s);
		}
		return unit;
	}
	
	/**
	 * Searches for the SparrowUnit that has the userName specified.
	 * The search is case sensitive and will return UNKNOWN if the userName
	 * cannot be found or is null.
	 * @param userName
	 * @return
	 */
	public static SparrowUnits parseUserName(String userName) {

		if (userName == null) return UNSPECIFIED;
		
		SparrowUnits[] values = SparrowUnits.values();
		for (SparrowUnits u : values) {
			if (u.getUserName().equals(userName)) return u;
		}
		
		return UNKNOWN;
	}
}
