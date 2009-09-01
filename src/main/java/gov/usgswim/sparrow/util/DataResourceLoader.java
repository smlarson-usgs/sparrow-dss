package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DataResourceLoader {
	public static final String SOURCE_METADATA_FILE = "src_metadata.txt";
	public static final String TOPO_FILE = "topo.txt";
	public static final String SOURCE_COEF_FILE = "source_coef.txt";
	public static final String SOURCE_VALUES_FILE = "source_values.txt";
	public static final String DECAY_COEF_FILE = "decay_coef.txt";
	public static final List<String> SOURCE_META_HEADINGS = Collections.unmodifiableList(
			Arrays.asList("SOURCE_ID", "NAME", "DISPLAY_NAME", "DESCRIPTION", "CONSTITUENT", "UNITS", "PRECISION", "IS_POINT_SOURCE"));


	public static DataTableWritable makeSourceMetaStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		Class<?>[] types= {Long.class, String.class, String.class, String.class, String.class, String.class, Integer.class, String.class};
		return new SimpleDataTableWritable(SOURCE_META_HEADINGS.toArray(new String[] {}), null, types);
	}

	public static DataTableWritable loadSourceMetadata(long modelId){
		String sourceMetaFolder = SparrowResourceUtils.getModelResourceFilePath(modelId, SOURCE_METADATA_FILE);
		DataTableWritable sourceMeta = makeSourceMetaStructure();
		return DataTableUtils.fill(sourceMeta, sourceMetaFolder, false, "\t", true);
		// TODO May need to go through and clean the data first
	}

	public static DataTableWritable loadTopo(long modelId) throws SQLException,
	IOException {
		String topoFile = SparrowResourceUtils.getModelResourceFilePath(modelId, TOPO_FILE);
		DataTableWritable topo = makeTopoStructure();
		DataTableUtils.fill(topo, topoFile, false, "\t", true);
		return topo;
	}

	public static DataTableWritable loadSourceReachCoef(long modelId, DataTable sourceMetaData) {
		final int NAME_COL = 0;
		final String UNITS = null;
		SimpleDataTableWritable result = new SimpleDataTableWritable();
		for (int srcIndex = 0; srcIndex < sourceMetaData.getRowCount(); srcIndex++) {
			result.addColumn(new StandardNumberColumnDataWritable<Double>(sourceMetaData.getString(srcIndex, NAME_COL), UNITS));
			// may need to add names
		}
		// may need to add id
		String coefFile = SparrowResourceUtils.getModelResourceFilePath(modelId, SOURCE_COEF_FILE);
		return DataTableUtils.fill(result, coefFile, false, "\t", true);
	}

	public static DataTableWritable loadDecay(long modelId) {
		String decayFile = SparrowResourceUtils.getModelResourceFilePath(modelId, DECAY_COEF_FILE);
		DataTableWritable decay = makeDecayStructure();
		DataTableUtils.fill(decay, decayFile, false, "\t", true);
		return decay;

	}

	public static DataTableWritable makeDecayStructure() {
		// TODO Auto-generated method stub
		return null;
	}

	public static DataTableWritable makeTopoStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		String[] headings = {"reachID", "fnode", "tnode", "iftran", "hydseq"}; // TODO check name of reach id
		Class<?>[] types= {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
		DataTableWritable result = new SimpleDataTableWritable(headings, null, types);
		return result;
	}



	public static DataTableWritable loadSourceValues(int testModel) {
		// TODO Auto-generated method stub
		return null;
	}

}
