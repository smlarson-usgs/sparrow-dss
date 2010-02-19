package gov.usgswim.sparrow.datatable;

public interface NamedEnum<E extends Enum<E>> {
	E fromString(String name);
	E fromStringIgnoreCase(String name);
	E getDefault();
	String getDescription();
}
