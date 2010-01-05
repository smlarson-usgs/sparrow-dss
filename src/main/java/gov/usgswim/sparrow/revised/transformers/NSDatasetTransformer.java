package gov.usgswim.sparrow.revised.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import oracle.mapviewer.share.Field;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.revised.CalculationResult;

public class NSDatasetTransformer extends Transformer {

	@Override
	public CalculationResult transform(CalculationResult result, String source) {
		if (result.table != null) return transformDataTable(result, source);
		if (result.column != null) return transformDataColumn(result);
		if (result.predictData != null) {
			result = transformSource(result, source);
			return result;
		}
		throw new RuntimeException("CalculationResult has nothing in it to be transformed into a NSDataSet!");
	}

	public static CalculationResult transformSource(CalculationResult result,
			String source) {
		// temporarily store the predictData. I'm not really sure this is necessary right now....
		PredictData data = result.predictData;
		result.table = data.getSrc();
		result = transformDataTable(result, source);
		result.predictData = data;
		return result;
	}

	protected static CalculationResult transformDataTable(CalculationResult result, String sourceName) {
		DataTable table = result.table;
		Integer col = table.getColumnByName(sourceName);
		if (col == null) col = findColumnByAnyPropertyValue(table, sourceName);

		assert(col != null) : "Could not find " + sourceName + "  as a source or other colummn property";
		return transformDataTable(table, col);
	}

	/**
	 * Try to find the column by matching any property
	 *
	 * TODO should be moved to DataTable.findColumnByPropertyValue(String propertyName, String propertyValue);
	 * @param sourceName
	 * @param table
	 * @return
	 */
	protected static Integer findColumnByAnyPropertyValue(DataTable table, String sourceName
			) {
		int colCount = table.getColumnCount();
		for ( int colIndex = 0; colIndex < colCount; colIndex++) {
			Set<String> keys = table.getPropertyNames(colIndex);
			for (String key: keys) {
				if (sourceName.equals(table.getProperty(colIndex, key))) {
					return colIndex;
				}
			}
		}
		return null;
	}

	protected static CalculationResult transformDataTable(DataTable table, int col) {
		// TODO There's a problem of whether or not the datatable will have
		// rowids. See NSDataSetBuilder
		// For now, we assume it does.
		// Also, we're differing slightly here, hopefully a good optimization.
		// When the field is null, we're
		// choosing to just not pass it in. Let's see what happens. Note that in
		// the NSDataSetBuilder code, the length of the NSDataSet is equal to
		// the number of reaches. Here, we're only assuming that it's the length
		// passed in.
		assert(table.hasRowIds()): "this method assumes row ids exist. Deal with tables lacking rowIDs later";
		int rowCount = table.getRowCount();
		List<NSRow> rows = new ArrayList<NSRow>(rowCount/8);
		for (int r=0; r<rowCount; r++ ) {
			Double val = table.getDouble(r, col);
			if (val != null) {
				Long id = table.getIdForRow(r);
				Field[] row = new Field[2];

				row[0] = new Field(id);
				row[1] = new Field(val);
				rows.add(new NSRow(row));
			}
		}
		NSRow[] nsRows = new NSRow[rows.size()];
		CalculationResult result = new CalculationResult();
		result.nsDataSet = new NSDataSet(rows.toArray(nsRows));
		return result;
	}

	protected CalculationResult transformDataColumn(CalculationResult result) {
		return null;
	}

}
