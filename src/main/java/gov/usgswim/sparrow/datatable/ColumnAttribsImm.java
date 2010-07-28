package gov.usgswim.sparrow.datatable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A builder for ColumnAttribs
 * 
 * @author eeverman
 */
public class ColumnAttribsImm implements ColumnAttribs {

	protected String description;
	protected String units;
	protected String name;
	protected Map<String, String> properties;

	public ColumnAttribsImm() {
		//empty default constructor allow easy null instance creation.
	}
	
	public ColumnAttribsImm(String description, String units, String name,
			Map<String, String> properties) {
		this.description = description;
		this.units = units;
		this.name = name;
		
		//Copy props for safety
		if (properties != null) {
			this.properties = new HashMap<String, String>(properties.size(), 1);
			this.properties.putAll(properties);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		if (properties != null) {
			return properties.get(key);
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		if (properties != null && properties.containsKey(key)) {
			return properties.get(key);
		} else {
			return defaultValue;
		}
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getPropertyNames()
	 */
	@Override
	public Set<String> getPropertyNames() {
		if (properties != null) {
			return properties.keySet();
		} else {
			Set<String> s = Collections.emptySet();
			return s;
		}
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#isPropertiesNull()
	 */
	@Override
	public boolean isPropertiesNull() {
		return properties == null;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#isPropertiesEmpty()
	 */
	@Override
	public boolean isPropertiesEmpty() {
		return (properties == null || properties.isEmpty());
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#isPropertiesEmptyAndNotNull()
	 */
	@Override
	public boolean isPropertiesEmptyAndNotNull() {
		return (properties != null && properties.isEmpty());
	}
	
	/**
	 * Returns the actual underlying property set, which may be null.
	 * @return
	 */
	@Override
	public Map<String, String> getProperties() {
		Map<String, String> tmp = null;
		
		if (properties != null) {
			tmp = new HashMap<String, String>(properties.size(), 1);
			tmp.putAll(properties);
		} else {
			tmp = Collections.emptyMap();
		}
		return tmp;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getDescription()
	 */
	@Override
	public String getDescription(String defaultValue) {
		if (description != null) {
			return description;
		} else {
			return defaultValue;
		}
	}

	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getUnits()
	 */
	@Override
	public String getUnits() {
		return units;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getUnits()
	 */
	@Override
	public String getUnits(String defaultValue) {
		if (units != null) {
			return units;
		} else {
			return defaultValue;
		}
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getName()
	 */
	@Override
	public String getName(String defaultValue) {
		if (name != null) {
			return name;
		} else {
			return defaultValue;
		}
	}
	
	@Override
	public ColumnAttribs toImmutable() throws IllegalStateException {
		return this;
	}

}
