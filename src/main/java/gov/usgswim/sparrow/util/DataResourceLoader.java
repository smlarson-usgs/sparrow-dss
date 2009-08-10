package gov.usgswim.sparrow.util;

import java.io.IOException;
import java.sql.SQLException;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;

public class DataResourceLoader {

	public static String getModelBasePath(long modelId, String fileName) {
		String modelFolder = "models/" + modelId + "/";
		return modelFolder + fileName;
	}

	public static DataTableWritable makeSourceMetaStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		String[] headings = {"SOURCE_ID", "NAME", "DISPLAY_NAME", "DESCRIPTION", "CONSTITUENT", "UNITS", "PRECISION", "IS_POINT_SOURCE"};
	
		Class<?>[] types= {Long.class, String.class, String.class, String.class, String.class, String.class, Integer.class, Integer.class};
		return new SimpleDataTableWritable(headings, null, types);
	}

	public static DataTableWritable loadSourceMetadata(long modelId){
		String sourceMetaFolder = getModelBasePath(modelId, "src_metadata.txt");
		DataTableWritable sourceMeta = makeSourceMetaStructure();
		return DataTableUtils.fill(sourceMeta, sourceMetaFolder, false, "\t", true);
	}

	public static DataTableWritable loadTopo(long modelId) throws SQLException,
	IOException {
		String topoFile = getModelBasePath(modelId, "topo.txt");
		DataTableWritable topo = makeTopoStructure();
		DataTableUtils.fill(topo, topoFile, false, "\t", true);
		return topo;
	}

	public static DataTableWritable loadDecay(long modelId) {
		// This is really a part of coef
		// Only two columns, instream and total decay
		return null;
	
	}

	public static DataTableWritable loadSourceReachCoef(long modelId) {
	
		return null;
	}

	public static DataTableWritable makeTopoStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		String[] headings = {"reachID", "fnode", "tnode", "iftran", "hydseq"}; // TODO check name of reach id
		Class<?>[] types= {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
		DataTableWritable result = new SimpleDataTableWritable(headings, null, types);
		return result;
	}

	public static DataTableWritable loadSourceReachCoef(long modelId, DataTable sources) {
		SimpleDataTableWritable result = new SimpleDataTableWritable();
		for (int srcIndex = 0; srcIndex < sources.getRowCount(); srcIndex++) {
			result.addColumn(new StandardNumberColumnDataWritable<Double>());
			// may need to add names
		}
		// may need to add id
		String coefFile = getModelBasePath(modelId, "coef.txt");
		return DataTableUtils.fill(result, coefFile, false, "\t", true);
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

	public static final DataTable modelIndex = initModelIndex(); // TODO Might change this to properties?

}
