package gov.usgswim.sparrow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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

}
