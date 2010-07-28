package gov.usgswim.sparrow.datatable;

import gov.usgswim.ImmutableBuilder;

import java.util.Map;
import java.util.Set;

/**
 * The non-dynamic attributes of a column.
 * 
 * @author eeverman
 */
public interface ColumnAttribs extends ImmutableBuilder<ColumnAttribs> {
	//TODO: Property values do not properly support defaults.  Will require merging the values in some cases.
	
	public String getProperty(String key);
	
	/**
	 * Returns the specified property if it exists, or returns the passed
	 * default value.  If the entire set of properties is null, the default is
	 * returned.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getProperty(String key, String defaultValue);

	public abstract Set<String> getPropertyNames();
	
	/**
	 * Returns the property set, which will never be null.
	 * 
	 * Mutable implementations may return the actual underlying properties.
	 * @return
	 */
	public Map<String, String> getProperties();

	/**
	 * Returns true only if the properties collection is null.
	 * @return
	 */
	public boolean isPropertiesNull();

	/**
	 * returns true if the properties are empty or null.
	 * @return
	 */
	public boolean isPropertiesEmpty();

	/**
	 * Returns true ONLY if the properties collection is initialized and empty.
	 * 
	 * In the case of an uninitialized props collection, FALSE is returned.
	 * @return
	 */
	public boolean isPropertiesEmptyAndNotNull();

	public String getDescription();
	
	/**
	 * Returns the description, or the passed default if null.
	 * 
	 * @param defaultValue
	 * @return
	 */
	public String getDescription(String defaultValue);

	public String getUnits();

	/**
	 * Returns the units, or the passed default if null.
	 * 
	 * @param defaultValue
	 * @return
	 */
	public String getUnits(String defaultValue);
	
	public String getName();

	/**
	 * Returns the name, or the passed default if null.
	 * 
	 * @param defaultValue
	 * @return
	 */
	public String getName(String defaultValue);
}