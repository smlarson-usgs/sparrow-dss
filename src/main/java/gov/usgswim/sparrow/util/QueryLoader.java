package gov.usgswim.sparrow.util;

import java.io.IOException;

public class QueryLoader {
	private String location;

	public QueryLoader(String propFilePath) {
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
	public String getParametrizedQuery(String name, Object[] params) throws IOException {
		return ResourceLoaderUtils.loadParametrizedProperty(location, name, params);
	}

}
