package gov.usgswim.sparrow.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class PropertyLoaderHelper {
	private String location;

	public PropertyLoaderHelper(String propFilePath) {
		this.location = propFilePath;
	}
	
	/**
	 * Loads the named text chunk from the properties file and inserts the named values passed in params.
	 * 
	 * params are passed in serial pairs as {"name1", "value1", "name2", "value2"}.
	 * toString is called on each item, so it is OK to pass in autobox numerics.
	 * See the DataLoader.properties file for the names of the parameters available
	 * for the requested query.
	 * 
	 * @param name	Name of the query in the properties file
	 * @param params	An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 */
	public String getText(String name, Object[] params) throws IOException {
		String query = getText(name);

		for (int i=0; i<params.length; i+=2) {
			String n = "$" + params[i].toString() + "$";
			String v = params[i+1].toString();

			query = StringUtils.replace(query, n, v);
		}

		return query;
	}
	
	public String getText(String name) throws IOException {
		Properties props = new Properties();

		props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(location));

		return props.getProperty(name);
	}
}
