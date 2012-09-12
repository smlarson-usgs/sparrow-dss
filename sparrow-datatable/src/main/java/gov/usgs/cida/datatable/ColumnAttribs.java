package gov.usgs.cida.datatable;

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

	/**
	 * Returns the combined property names of the local properties and those
	 * passed in.
	 * 
	 * @param baseValues A set of property names from a base class - may be null.
	 * @return
	 */
	public abstract Set<String> getPropertyNames(Set<String> baseValues);
	
	/**
	 * Returns the property map, which will never be null.
	 * 
	 * The local values of properties take precedence over base values.
	 * The returned reference is a copied, disconnected version of the properties.
	 * 
	 * @param baseValues A set of property values from a base class - may be null.
	 * @return
	 */
	public Map<String, String> getProperties(Map<String, String> baseValues);


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