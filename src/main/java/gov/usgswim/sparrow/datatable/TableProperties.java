package gov.usgswim.sparrow.datatable;

public enum TableProperties implements NamedEnum<TableProperties>{
	ROW_LEVEL("row_level", HucLevel.class, "Each row in the table represents a reach, a huc2/4/6/8, other?"),
	CONSTITUENT("constituent", null, "Name of the thing being measured.");
	
	private String name;
	private String description;
	private Class<? extends Enum<?>> valueEnum;
	
	<E extends Enum<?>> TableProperties(String name, Class<E> valueEnum, String description) {
		this.name = name;
		this.description = description;
		this.valueEnum = valueEnum;
	}

	@Override
	public TableProperties fromString(String name) {
		for (TableProperties val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public TableProperties fromStringIgnoreCase(String name) {
		for (TableProperties val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}

	@Override
	public TableProperties getDefault() {
		return null;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Class<? extends Enum<?>> getValueEnum() {
		return valueEnum;
	}
}