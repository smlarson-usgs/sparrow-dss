/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgswim.sparrow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author eeverman
 */
public class ServletUtil {

	/**
	 * Loads the named text chunk from the properties file in the same package
	 * and with the same name as the class. E.g: pkg.MyClass should have a
	 * properties file as: pkg/MyClass.properties.
	 * <br><br>
	 * params are passed in serial pairs as {"name1", "value1", "name2",
	 * "value2"}. toString is called on each item, so it is OK to pass in
	 * autobox numerics. See the DataLoader.properties file for the names of the
	 * parameters available for the requested query.
	 *
	 * @param propName
	 *            Name of the query in the properties file
	 * @param clazz
	 * @param params
	 *            An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 *
	 * TODO move this to a utils class of some sort
	 */
	public static String getProperyValueWithSubstitutions(String propName,
					Class<?> clazz, Object... params) throws IOException {

		String sourceText = getProperyValue(propName, clazz);
		sourceText = doParamSubstitutions(sourceText, params);

		return sourceText;
	}
	
	
	/**
	 * Returns the content of the files specified by the specified parameters.
	 * The key parameter is the clazz:  It is assumed that we are looking for a
	 * text file in the same package and similarly named to the class.
	 *
	 * @param clazz Find a text file in the same package, named similar to the class.
	 * @param fileSuffix Tack this bit of text after the class name (prior to a suffix).
	 *	If null, no suffix is added.
	 * @param fileExtension Added at the end with an auto added '.'  If null,
	 *	no extension (or dot) will be added.
	 * @param params The values to replace in the text file.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getAnyFileWithSubstitutions(Class<?> clazz,
					String fileSuffix, String fileExtension, Object... params) throws IOException {
		
		String content = getFileContent(clazz, fileSuffix, fileExtension);
		content = doParamSubstitutions(content, params);
		return content;
	}

	/**
	 * Loads the named property value from a properties file located in the same
	 * package and with the same name as the {@code}clazz{@code} parameter.
	 *
	 * @param propName of text chunk
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public static String getProperyValue(String propName, Class<?> clazz) throws IOException {
		Properties props = new Properties();

		String path = clazz.getName().replace('.', '/') + ".properties";
		InputStream ins = getResourceAsStream(path);
		props.load(ins);

		return props.getProperty(propName);
	}

	/**
	 * Returns the content of the files specified by the specified parameters.
	 * The key parameter is the clazz:  It is assumed that we are looking for a
	 * text file in the same package and similarly named to the class.
	 *
	 * @param clazz Find a text file in the same package, named similar to the class.
	 * @param fileSuffix Tack this bit of text after the class name (prior to a suffix).
	 *	If null, no suffix is added.
	 * @param fileExtension Added at the end with an auto added '.'  If null,
	 *	no extension (or dot) will be added.s
	 *
	 * @return
	 * @throws IOException
	 */
	public static String getFileContent(Class<?> clazz, String fileSuffix, String fileExtension) throws IOException {

		String path = clazz.getName().replace('.', '/');

		if (fileSuffix != null) {
			path = path + fileSuffix;
		}
		if (fileExtension != null) {
			path = path + "." + fileExtension;
		}

		InputStream ins = getResourceAsStream(path);
		String content = readToString(ins);

		return content;
	}

	protected static String doParamSubstitutions(String sourceText, Object... params) {

		for (int i = 0; i < params.length; i += 2) {
			String n = "$" + params[i].toString() + "$";
			String v = params[i + 1].toString();

			sourceText = sourceText.replace(n, v);
		}

		return sourceText;
	}

	protected static InputStream getResourceAsStream(String path) {
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (ins == null) {
			ins = ServletUtil.class.getResourceAsStream(path);
		}
		return ins;
	}

	/**
	 * Convenience method for reading the contents of an input stream to a
	 * String, ignoring errors.
	 *
	 * @param is
	 * @return
	 */
	public static String readToString(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		StringBuffer sb = new StringBuffer();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception ex) {
			ex.getMessage();
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}

	public static String getString(Map params, String name, boolean required) throws Exception {
		String val = getString(params, name);
		if (val == null && required) {
			throw new Exception("The required parameter '" + name + "' was null.");
		} else {
			return val;
		}
	}

	/**
	 * Returns a trimmed to null value from the parameter map for the passed name.
	 *
	 * @param params
	 * @param name
	 */
	protected static String getString(Map params, String name) {
		Object v = params.get(name);
		if (v != null) {
			String[] vs = (String[]) v;
			if (vs.length > 0) {
				return trimToNull(vs[0]);
			}
		}
		return null;
	}

	public static Long getLong(Map params, String name, boolean required) throws Exception {
		Long val = getLong(params, name);
		if (val == null && required) {
			throw new Exception("The required parameter '" + name + "' was null or unreadable as an integer.");
		} else {
			return val;
		}
	}
	/**
	 * Returns a Long value from the parameter map for the passed name.
	 * If unparsable, missing, or null, null is returned.
	 *
	 * @param params
	 * @param name
	 */
	public static Long getLong(Map params, String name) {
		String s = getString(params, name);
		if (s != null) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}

	public static Integer getInteger(Map params, String name, boolean required) throws Exception {
		Integer val = getInteger(params, name);
		if (val == null && required) {
			throw new Exception("The required parameter '" + name + "' was null or unreadable as an integer.");
		} else {
			return val;
		}
	}

	/**
	 * Returns an Integer value from the parameter map for the passed name.
	 * If unparsable, empty, or missing, null is returned.
	 *
	 * @param params
	 * @param name
	 */
	private static Integer getInteger(Map params, String name) {
		String s = getString(params, name);
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}

		public static Double getDouble(Map params, String name, boolean required) throws Exception {
		Double val = getDouble(params, name);
		if (val == null && required) {
			throw new Exception("The required parameter '" + name + "' was null or unreadable as an decimal number.");
		} else {
			return val;
		}
	}

	/**
	 * Returns an Integer value from the parameter map for the passed name.
	 * If unparsable, empty, or missing, null is returned.
	 *
	 * @param params
	 * @param name
	 */
	private static Double getDouble(Map params, String name) {
		String s = getString(params, name);
		if (s != null) {
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}


	public static Boolean getBoolean(Map params, String name, boolean required) throws Exception {
		Boolean val = getBoolean(params, name);
		if (val == null && required) {
			throw new Exception("The required parameter '" + name + "' was null or unreadable as a boolean.");
		} else {
			return val;
		}
	}

	/**
	 * Returns a Boolean value from the parameter map for the passed name.
	 * If empty or missing, null is returned.  Otherwise, values are considered
	 * true if they are 'T' or 'TRUE', case insensitive.
	 *
	 * @param params
	 * @param name
	 */
	protected static Boolean getBoolean(Map params, String key) {
		String s = getString(params, key);
		if (s != null) {
			return ("T".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s));
		}
		return null;
	}

	public static String trimToNull(String val) {
		if (val == null) return null;

		val = val.trim();

		if (! "".equals(val)) {
			return val;
		} else {
			return null;
		}
	}
}
