package gov.usgswim.datatable;

import java.util.HashMap;
import java.util.HashSet;
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
	public Set<String> getPropertyNames(Set<String> baseValues) {
		Set<String> combi = new HashSet<String>();
		
		if (baseValues != null) {
			combi.addAll(baseValues);
		} 
		if (properties != null) {
			combi.addAll(properties.keySet());
		} 
		
		return combi;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.datatable.ColumnAttribs#getProperties(java.util.Map)
	 */
	@Override
	public Map<String, String> getProperties(Map<String, String> baseValues) {
		Map<String, String> combi = new HashMap<String, String>(7);
		
		if (baseValues != null) {
			combi.putAll(baseValues);
		}
		
		if (properties != null) {
			combi.putAll(properties);
		}
		
		return combi;
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
