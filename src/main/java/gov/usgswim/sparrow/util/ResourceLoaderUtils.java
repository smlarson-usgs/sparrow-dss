package gov.usgswim.sparrow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

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
		} catch (IOException e) {
			e.printStackTrace();
			try {
				inStream.close();
			} catch (IOException e1) {
				// don't do anything
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
				try {
					stream.close();
				} catch (IOException e1) {
					// do nothing
				}
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
		String query = ResourceLoaderUtils.loadProperty(resourceFilePath, key);

		for (int i=0; i<params.length; i+=2) {
			String n = "$" + params[i].toString() + "$";
			String v = params[i+1].toString();

			query = StringUtils.replace(query, n, v);
		}

		return query;
	}
}
