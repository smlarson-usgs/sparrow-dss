package gov.usgswim.sparrow.datatable;

public interface NamedEnum<E extends Enum<E>> {
	
	/**
	 *  Should be consistent w/ the toString value.
	 */
	E fromString(String name);
	
	/**
	 *  Should be consistent w/ the toString value.
	 */
	E fromStringIgnoreCase(String name);
	E getDefault();
	String getDescription();
}
