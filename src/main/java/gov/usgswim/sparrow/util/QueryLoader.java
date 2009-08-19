package gov.usgswim.sparrow.util;

import gov.usgs.webservices.framework.utils.ResourceLoaderUtils;

import java.io.IOException;

public class QueryLoader {
	private String location;
	private Object[] defaults;

	public QueryLoader(String propFilePath) {
		this.location = propFilePath;
	}

	/**
	 * @param propFilePath
	 * @param defaultParams substitution parameters added to every retrieved property
	 */
	public QueryLoader(String propFilePath, Object... defaultParams) {
		this.location = propFilePath;
		this.defaults = defaultParams;
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
	public String getParametrizedQuery(String name, Object... params) throws IOException {
		String result = ResourceLoaderUtils.loadParametrizedProperty(location, name, params);
		if (defaults != null) {
			// apply the defaults
			for (int i=0; i<defaults.length; i+=2) {
				result = ResourceLoaderUtils.replaceParam(result, defaults[i].toString(), defaults[i+1].toString());
			}
		}

		return result;
	}

	public String getParametrizedQuery(String name, String... params) throws IOException {
		String result = ResourceLoaderUtils.loadParametrizedProperty(location, name, params);
		if (defaults != null) {
			// apply the defaults
			for (int i=0; i<defaults.length; i+=2) {
				result = ResourceLoaderUtils.replaceParam(result, defaults[i].toString(), defaults[i+1].toString());
			}
		}

		return result;
	}

}
