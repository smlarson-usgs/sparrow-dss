package gov.usgswim.sparrow.cachefactory;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public abstract class AbstractCacheFactory implements CacheEntryFactory {

	/**
	 * Override to supply a new cached value, calculated from the passed in
	 * key value.
	 */
	public abstract Object createEntry(Object key) throws Exception;
	
	/**
	 * Loads the named text chunk from the properties file and inserts the named values passed in params.
	 * 
	 * This method assumes that the properties are contained in properties file
	 * in the same package and with the same name as the class.  E.g: pkg.MyClass
	 * should have a properties file as: pkg/MyClass.properties.
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
	
	/**
	 * Loads the named text chunk from the properties file.
	 * 
	 * This method assumes that the properties are contained in properties file
	 * in the same package and with the same name as the class.  E.g: pkg.MyClass
	 * should have a properties file as: pkg/MyClass.properties.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public String getText(String name) throws IOException {
		Properties props = new Properties();

		String path = this.getClass().getName().replace('.', '/') + ".properties";
		props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));

		return props.getProperty(name);
	}
	

}
