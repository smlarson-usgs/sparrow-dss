package gov.usgswim.sparrow.util;

import gov.usgs.webservices.framework.utils.ResourceLoaderUtils;
import gov.usgs.webservices.framework.utils.SmartXMLProperties;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
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
	public static String lookupMergedHelp(Long modelId, String helpItem, String wrapXMLElement, Object... params) throws Exception {
		
		String text = lookupMergedHelp(modelId, helpItem, wrapXMLElement);
		
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
	public static String lookupMergedHelp(Long modelId, String helpItem, String wrapXMLElement) throws Exception {
		String genHelp = lookupGeneralHelp(helpItem);
		String modelHelp = lookupModelHelp(modelId, helpItem);
		
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
	
	public static String lookupGeneralHelp(String helpItem) throws Exception {
		SmartXMLProperties help = retrieveGeneralHelp();
		return help.get(helpItem);
	}
	
	public static String lookupModelHelp(Long modelId, String helpItem) throws Exception {
		SmartXMLProperties help = retrieveModelHelp(modelId);
		return help.get(helpItem);
	}

	public static SmartXMLProperties retrieveModelHelp(Long modelId) throws Exception {
		String resourceFilePath = getModelResourceFilePath(modelId, HELP_FILE);
		
		try {
			return ResourceLoaderUtils.loadResourceAsSmartXML(resourceFilePath);
		} catch (Exception e) {
			throw new Exception("The help file for model '" + modelId + "' could not be found.", e);
		}
	}
	
	public static SmartXMLProperties retrieveGeneralHelp() throws Exception {
		String resourceFilePath = getResourceFilePath(HELP_FILE);
		
		try {
			return ResourceLoaderUtils.loadResourceAsSmartXML(resourceFilePath);
		} catch (Exception e) {
			throw new Exception("The general help file could not be found.", e);
		}
		
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
	
	public static String lookupLayerNameForModelID(Long modelID) {
		ModelRequestCacheKey key = new ModelRequestCacheKey(modelID, false, false, false);
		List<SparrowModel> modelMetaDataList = SharedApplication.getInstance().getModelMetadata(key);
		
		if((modelMetaDataList == null) || (modelMetaDataList.size() == 0)) {
			return null;
		}
		
		SparrowModel model = modelMetaDataList.get(0);
		
		if(model == null) {
			return null;
		}
		
		return model.getThemeName();
	}
	
	public static byte[] getFileDataByClass(Class<?> clazz, String extension) throws IOException {
		String path = clazz.getName().replace('.', '/') + extension;
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		
		return IOUtils.toByteArray(ins);
	}
	
	public static byte[] getFileData(String path) throws IOException {
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);		
		return IOUtils.toByteArray(ins);
	}

	private SparrowResourceUtils() {/* private constructor to prevent instantiation */ }

}
