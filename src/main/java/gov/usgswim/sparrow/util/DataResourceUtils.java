package gov.usgswim.sparrow.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;

public abstract class DataResourceUtils {
	public static final DataTable modelIndex = initModelIndex(); // TODO Might change this to properties?
	private static final Map<Integer, Properties> modelProperties = new HashMap<Integer, Properties>();

	private DataResourceUtils() {/* private constructor to prevent instances */ }

	public static String getModelResourceFilePath(long modelId, String fileName) {
		String modelFolder = "models/" + modelId + "/";
		return modelFolder + fileName;
	}

	public static void loadResourceStreamAsProperties(InputStream inStream, Integer modelID) {
		Properties modelProperty  = new Properties();
		try {
			modelProperty.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		modelProperties.put(modelID, modelProperty);
	}

	public static String loadModelResource(Integer modelID, String itemKey) {
		Properties modelProperty = modelProperties.get(modelID);
		if (modelProperty == null) {
			String modelResourceFile = getModelResourceFilePath(modelID, "");
//			modelProperty = new Properties().load(inStream)
		}
		return null;
	}

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
