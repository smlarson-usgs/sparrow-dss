package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.service.help.Model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class SparrowResourceUtils {
	public static final String SESSIONS_FILE = "sessions.properties";
	public static final Properties modelIndex = ResourceLoaderUtils.loadResourceAsProperties("models/modelIndex.txt");
	private static final Map<Long, Properties> modelProperties = new HashMap<Long, Properties>();
	private static final String PROPERTIES_FILE = "model.properties";

	private static final String XML_File = "model.xml";

	private SparrowResourceUtils() {/* private constructor to prevent instances */ }

	public static String getModelResourceFilePath(Long modelId, String fileName) {
		String modelFolder = "models/" + modelId + "/";
		return modelFolder + fileName;
	}

	public static String retrieveSavedSession(String model, String sessionName) {
		Long modelID = lookupModelID(model);
		Properties props = ResourceLoaderUtils.loadResourceAsProperties(getModelResourceFilePath(modelID, SESSIONS_FILE));
		String defaultValue = null; // TODO decide on default value for session not found
		return props.getProperty(sessionName, defaultValue);
	}

	public static Set<Object> retrieveSavedSessions(String model) {
		Long modelID = lookupModelID(model);
		Properties props = ResourceLoaderUtils.loadResourceAsProperties(getModelResourceFilePath(modelID, SESSIONS_FILE));
		return props.keySet();
	}

	public static String retrieveHelp(String model, String helpItem) {
		Long modelID = lookupModelID(model);
		String resourceFilePath = getModelResourceFilePath(modelID, "help.xml");
		Model help = ResourceLoaderUtils.loadResourceXMLFileAsObject(resourceFilePath, Model.class, "Model");
		String item = help.getOne(helpItem.getClass());
		// TODO later handle lists/multiple retrievals using help.getMany()?
		return item;
	}

	/**
	 * Returns the id of the model, as model input string may be either an id or a name.
	 * @param model
	 * @return
	 */
	public static Long lookupModelID(String model) {
		try {
			return Long.parseLong(model);
		} catch (Exception e) {
			try {
				return Long.parseLong(modelIndex.get(model).toString());
			} catch (Exception ex) { /* do nothing. Let someone else deal with bad lookup value */ }
		}
		return null;
	}








	public static String loadModelPropertiesResource(Long modelID, String itemKey) {
		Properties props = modelProperties.get(modelID);
		if (props == null) {
			props = loadResourceAsProperties(modelID, getModelResourceFilePath(modelID, PROPERTIES_FILE));
			modelProperties.put(modelID, props);
		}
		return (props == null)? null: props.getProperty(itemKey);
	}

	public static Properties loadResourceAsProperties(Long modelID, String fileName) {
		String resourceName = getModelResourceFilePath(modelID, fileName);
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		return ResourceLoaderUtils.loadResourceAsProperties(in);
	}

	public static <T> T loadHelp(Long modelId){
		String helpFilePath = getModelResourceFilePath(modelId, "help.xml");
		ResourceLoaderUtils.loadResourceXMLFileAsObject(helpFilePath, Model.class, null);
		return null;
	}




}
