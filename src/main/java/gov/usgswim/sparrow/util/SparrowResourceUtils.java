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

public abstract class SparrowResourceUtils {
	public static final DataTable modelIndex = initModelIndex(); // TODO Might change this to properties?
	private static final Map<Long, Properties> modelProperties = new HashMap<Long, Properties>();
	private static final String PROPERTIES_FILE = "model.properties";
	private static final String XML_File = "model.xml";

	private SparrowResourceUtils() {/* private constructor to prevent instances */ }

	public static String getModelResourceFilePath(Long modelId, String fileName) {
		String modelFolder = "models/" + modelId + "/";
		return modelFolder + fileName;
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
	
//	public static Long lookupModel(String model) {
//		Long result = null;
//		try {
//			result = Long.parseLong(model);
//		} catch (Exception e) { /* Do Nothing */};
//		if (result == null) {
//			
//		}
//	}
	
	/**
	 * Loads an index of model aliases and ids from the file system.
	 * @return
	 */
	static DataTable initModelIndex() {
		DataTableWritable table = new SimpleDataTableWritable()
			.addColumn(new StandardStringColumnDataWritable("modelName", null))
			.addColumn(new StandardStringColumnDataWritable("modelID", null));
		return DataTableUtils.fill(table, "models/modelIndex.txt", false, "\t", true);
	}
	



}
