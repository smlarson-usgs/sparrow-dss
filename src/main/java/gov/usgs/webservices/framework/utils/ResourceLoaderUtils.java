package gov.usgs.webservices.framework.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * A convenient but inefficient class for accessing properties.
 * TODO Create a memoized version of this which remembers the ones it has loaded so as not to go to the file system every time.
 * @author ilinkuo
 *
 */
public class ResourceLoaderUtils {

	/**
	 * Loads the resource as an instance of the given class
	 * @param <T>
	 * @param resourceFilePath
	 * @param clazz the desired return class
	 * @param objectElementName
	 * @return
	 */
	public static <T> T loadResourceXMLFileAsObject(String resourceFilePath, Class<T> clazz, String objectElementName) {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilePath);
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		return ResourceLoaderUtils.loadResourceReaderAsObject(in, clazz, objectElementName);
	}

	public static <T> T loadResourceReaderAsObject(Reader inStream, Class<T> clazz, String objectElementName) {
		XStream xstream = new XStream(new DomDriver());
		xstream.alias(objectElementName, clazz);
		return clazz.cast(xstream.fromXML(inStream));
	}


	public static SmartXMLProperties loadResourceAsSmartXML(String resourceFilePath) {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilePath);
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		return ResourceLoaderUtils.loadResourceAsSmartXML(in);
	}

	public static SmartXMLProperties loadResourceAsSmartXML(Reader inStream) {

		return null;
	}

	/**
	 * Convenience method for loading the indicated resource as a Properties
	 * object, or an empty Properties object if errors, avoiding a try-catch
	 *
	 * @param inStream
	 * @return Properties, possibly empty
	 */
	public static Properties loadResourceAsProperties(Reader inStream) {
		Properties modelProperty  = new Properties();
		try {
			modelProperty.load(inStream);
		} catch (Exception e) {
			e.printStackTrace();
			if (inStream != null) {
				try {inStream.close();} catch (IOException e1) { /* do nothing */}
			}
		}
		return modelProperty;
	}

	/**
	 * Convenience method for loading the indicated resource file as a Properties
	 * object, or an empty Properties object if errors, avoiding a try-catch
	 *
	 * @param inStream
	 * @return Properties, possibly empty
	 */
	public static Properties loadResourceAsProperties(String resourceFilePath) {
		InputStream stream = null;
		try {
			stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilePath);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			return loadResourceAsProperties(in);
		} catch (Exception e) {
			e.printStackTrace();
			if (stream != null) {
				try {stream.close();} catch (IOException e1) { /* do nothing */}
			}
			return new Properties(); // return an empty Properties
		}
	}


	public static String loadProperty(String resourceFilePath, String key) {
		Properties props = loadResourceAsProperties(resourceFilePath);
		return props.getProperty(key);
	}

	/**
	 * Loads the named text chunk from the properties file and inserts the named values passed in params.
	 *
	 * params are passed in serial pairs as {"name1", "value1", "name2", "value2"}.
	 * toString() is called on each item, so it is OK to pass in autobox numerics.
	 *
	 * @param name	Name of the query in the properties file
	 * @param params	An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 */
	public static String loadParametrizedProperty(String resourceFilePath, String key, Object... params) {
		return loadParametrizedProperty(resourceFilePath, key, pairsToMap(params));
	}

	/**
	 * @see ResourceLoaderUtils.loadParametrizedProperty()
	 * @param resourceFilePath
	 * @param key
	 * @param substitutions
	 * @return
	 */
	public static String loadParametrizedProperty(String resourceFilePath, String key, Map<Object, Object> substitutions) {
		String query = ResourceLoaderUtils.loadProperty(resourceFilePath, key);

		for (Entry<Object, Object> entry: substitutions.entrySet()) {
			query = replaceParam(query, entry.getKey().toString(), entry.getValue().toString());
		}

		return query;
	}

	public static String replaceParam(String value, String paramName, String paramValue) {
		if (paramName.charAt(0) != '\\') paramName = "\\$" + paramName + "\\$";
		return value.replaceAll(paramName, paramValue);
//		return StringUtils.replace(value, paramName, paramValue);
	}

	public static <T> Map<T, T> pairsToMap(T[] params){
		Map<T, T> map = new HashMap<T, T>();
		for (int i=1; i<params.length; i+=2) {
			T value = params[i];
			if (value != null) {
				map.put(params[i-1], value);
			}
		}
		return map;
	}
}
