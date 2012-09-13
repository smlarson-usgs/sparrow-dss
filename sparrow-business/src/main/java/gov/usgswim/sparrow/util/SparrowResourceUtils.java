package gov.usgswim.sparrow.util;

import gov.usgs.webservices.framework.utils.ResourceLoaderUtils;
import gov.usgs.webservices.framework.utils.SmartXMLProperties;

import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

public abstract class SparrowResourceUtils {
	public static final String HELP_FILE = "model.xml";
	public static final String SESSIONS_FILE = "sessions.properties";
	private static final Properties modelIndex = ResourceLoaderUtils.loadResourceAsProperties("models/modelIndex.txt");

	public static String getModelResourceFilePath(Long modelId, String fileName) {
		if (modelId == null || fileName == null) return null;
		String modelFolder = "models/" + modelId + "/";
		return modelFolder + fileName;
	}

	public static String getResourceFilePath(String fileName) {
		String modelFolder = "models/";
		return modelFolder + fileName;
	}


	/**
	 * Looks up an item in the help documentation and replaced named parameters.
	 * 
	 * Parameters are passed in name-value pairs, so there should always be an
	 * even number of params.
	 * 
	 * @param model
	 * @param helpItem
	 * @param wrapXMLElement
	 * @param params
	 * @return
	 */
	public static String lookupMergedHelp(String model, String helpItem, String wrapXMLElement, Object... params) {
		
		String text = lookupMergedHelp(model, helpItem, wrapXMLElement);
		
		if (params != null) {
			for (int i=0; i<params.length; i+=2) {
				String n = "$" + params[i].toString() + "$";
				String v = null;
				if (params[i+1] != null) {
					v = params[i+1].toString();
				} else {
					v = "[Unknown]";
				}
	
				text = StringUtils.replace(text, n, v);
			}
		}

		return text;
		
	}
	public static String lookupMergedHelp(String model, String helpItem, String wrapXMLElement) {
		String genHelp = lookupGeneralHelp(helpItem);
		String modelHelp = lookupModelHelp(model, helpItem);
		
		StringBuffer merged = new StringBuffer();
		if (genHelp != null) {
			merged.append(genHelp);
		}
		
		if (modelHelp != null) {
			merged.append(modelHelp);
		}
		
		if (merged.length() > 0) {
			if (wrapXMLElement != null && wrapXMLElement.length() > 0) {
				return "<" + wrapXMLElement + ">" + merged.toString() + "</" + wrapXMLElement + ">";
			} else {
				return merged.toString();
			}
		} else {
			return null;
		}
	}
	
	public static String lookupGeneralHelp(String helpItem) {
		SmartXMLProperties help = retrieveGeneralHelp();
		return help.get(helpItem);
	}
	
	public static String lookupModelHelp(String model, String helpItem) {
		SmartXMLProperties help = retrieveModelHelp(model);
		return help.get(helpItem);
	}

	public static SmartXMLProperties retrieveModelHelp(String model) {
		Long modelID = lookupModelID(model);
		String resourceFilePath = getModelResourceFilePath(modelID, HELP_FILE);
		return ResourceLoaderUtils.loadResourceAsSmartXML(resourceFilePath);
	}
	
	public static SmartXMLProperties retrieveGeneralHelp() {
		String resourceFilePath = getResourceFilePath(HELP_FILE);
		return ResourceLoaderUtils.loadResourceAsSmartXML(resourceFilePath);
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
