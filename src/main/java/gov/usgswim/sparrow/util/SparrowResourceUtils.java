package gov.usgswim.sparrow.util;

import gov.usgs.webservices.framework.utils.ResourceLoaderUtils;
import gov.usgswim.sparrow.service.help.Model;

import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.NotImplementedException;

public abstract class SparrowResourceUtils {
	public static final String HELP_FILE = "help.xml";
	public static final String SESSIONS_FILE = "sessions.properties";
	private static final Properties modelIndex = ResourceLoaderUtils.loadResourceAsProperties("models/modelIndex.txt");
	private static final String XML_File = "model.xml";

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

	public static Set<Entry<Object, Object>> retrieveSavedSessions(String model) {
		Long modelID = lookupModelID(model);
		Properties props = ResourceLoaderUtils.loadResourceAsProperties(getModelResourceFilePath(modelID, SESSIONS_FILE));
		return props.entrySet();
	}

	public static String retrieveHelp(String model, String helpItem) {
		Long modelID = lookupModelID(model);
		String resourceFilePath = getModelResourceFilePath(modelID, HELP_FILE);
		Model help = ResourceLoaderUtils.loadResourceXMLFileAsObject(resourceFilePath, Model.class, "SparrowModel");
		String item = help.getOne(helpItem.getClass());
		// TODO later handle maps/multiple retrievals using help.getMany()?
		return item;
	}

	/**
	 * Returns the id of the model, as model input string may be either an id or a name.
	 * @param model
	 * @return
	 */
	public static Long lookupModelID(String model) {
		if (model == null) return null;
		try {
			return Long.parseLong(model);
		} catch (Exception e) {
			try {
				return Long.parseLong(modelIndex.get(model).toString());
			} catch (Exception ex) { /* do nothing. Let someone else deal with bad lookup value */ }
		}
		return null;
	}

	public static String lookupModelName(String model) {
		if (model == null) return null;
		try {
			Long id = Long.parseLong(model);
			return ""; // TODO use bimap to lookup by value
		} catch (Exception e) {
			return model;
		}

	}

	public static String lookupModelName(Long modelID) {
		// TODO implement this
		throw new NotImplementedException();
	}

	private SparrowResourceUtils() {/* private constructor to prevent instantiation */ }

}
