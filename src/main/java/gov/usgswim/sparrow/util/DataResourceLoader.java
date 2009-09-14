package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.service.SharedApplication;

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
	public static final int SOURCE_ID_COL = 0;

	public static PredictData loadModelData(long modelId) {
		PredictDataBuilder dataSet = new PredictDataBuilder();
		{
			dataSet.setSrcMetadata( loadSourceMetadata(modelId));
			dataSet.setTopo( loadTopo(modelId) );
			dataSet.setCoef( loadSourceReachCoef(modelId, dataSet.getSrcMetadata()) );
			dataSet.setDecay( loadDecay( modelId) );
			dataSet.setSrc( loadSourceValues( modelId, dataSet.getSrcMetadata(), dataSet.getTopo()) );
		}
		return dataSet.toImmutable();
	}

	public static DataTableWritable makeSourceMetaStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		Class<?>[] types= {Long.class, String.class, String.class, String.class, String.class, String.class, Integer.class, String.class};
		return new SimpleDataTableWritable(SOURCE_META_HEADINGS.toArray(new String[] {}), null, types);
	}

	public static DataTableWritable loadSourceMetadata(long modelId){
		String sourceMetaFolder = SparrowResourceUtils.getModelResourceFilePath(modelId, SOURCE_METADATA_FILE);
		DataTableWritable sourceMeta = makeSourceMetaStructure();
		DataTableWritable result = DataTableUtils.fill(sourceMeta, sourceMetaFolder, false, "\t", true);
		// first column of sources is ids. Must set
		long[] ids = DataTableUtils.getLongColumn(result, 0);
		return DataTableUtils.setIds(result, ids);
		// TODO May need to go through and clean the data first
	}

	public static DataTableWritable loadTopo(long modelId) {
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
			StandardNumberColumnDataWritable<Double> newCol = new StandardNumberColumnDataWritable<Double>(sourceMetaData.getString(srcIndex, NAME_COL), UNITS);
			newCol.setType(Double.class);
			result.addColumn(newCol);
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
		final String UNITS = null;
		SimpleDataTableWritable decayStructure = new SimpleDataTableWritable();
		{
			// Decay structure has only two columns: instream decay and upstream decay
			StandardNumberColumnDataWritable<Double> newCol = new StandardNumberColumnDataWritable<Double>("instreamDecay", UNITS);
			newCol.setType(Double.class);
			decayStructure.addColumn(newCol);
			newCol = new StandardNumberColumnDataWritable<Double>("upstreamDecay", UNITS);
			newCol.setType(Double.class);
			decayStructure.addColumn(newCol);
		}
		return decayStructure;
	}

	public static DataTableWritable makeTopoStructure() {
		// Note that the topo.txt file from the modelers does not have the reach id
		String[] headings = {"reachID", "fnode", "tnode", "iftran", "hydseq"}; // TODO check name of reach id
		Class<?>[] types= {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
		DataTableWritable result = new SimpleDataTableWritable(headings, null, types);
		return result;
	}


	public static DataTableWritable loadSourceValues(long modelId, DataTable sourceMetaData, DataTable topo) {
		int sourceCount = sourceMetaData.getRowCount();
		if (sourceCount == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}

		// Load column headings using the source display names
		// TODO need to add display name to source_metadata to eliminate the magic number of 2
		Integer display_name_col = sourceMetaData.getColumnByName("display_name");
		display_name_col = (display_name_col == null)? 2: display_name_col;
		String[] headings = DataTableUtils.getStringColumn(sourceMetaData, display_name_col);

		DataTableWritable sourceValues = new SimpleDataTableWritable();

		{	// use identifiers from topo to set source value ids
			int size = topo.getRowCount();
			for (int i=0; i<size; i++) {
				sourceValues.setRowId(topo.getLong(i, 0), i);
			}
		}

		// Create columns
		sourceValues = makeSourceValueStructure(sourceMetaData,  headings,	sourceValues);
		String sourceValuesFile = SparrowResourceUtils.getModelResourceFilePath(modelId, SOURCE_VALUES_FILE);
		DataTableUtils.fill(sourceValues, sourceValuesFile, false, "\t", true);

		return sourceValues;
	}

	private static DataTableWritable makeSourceValueStructure(DataTable sourceMetaData,
			String[] headings, DataTableWritable sourceValues) {
		int sourceCount = sourceMetaData.getRowCount();
		for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++) {
			String constituent = sourceMetaData.getString(srcIndex, sourceMetaData.getColumnByName("CONSTITUENT"));
			String units = sourceMetaData.getString(srcIndex, sourceMetaData.getColumnByName("UNITS"));
			String precision = sourceMetaData.getString(srcIndex, sourceMetaData.getColumnByName("PRECISION"));

			StandardNumberColumnDataWritable<Double> column = new StandardNumberColumnDataWritable<Double>(headings[srcIndex], units);
			column.setProperty("constituent", constituent);
			column.setProperty("precision", precision);
			column.setType(Double.class);
			sourceValues.addColumn(column);
		}
		return sourceValues;
	}

}
