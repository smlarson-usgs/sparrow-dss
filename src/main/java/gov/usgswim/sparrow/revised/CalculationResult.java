package gov.usgswim.sparrow.revised;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;

import java.util.List;

import oracle.mapviewer.share.ext.NSDataSet;

/**
 * A simple struct class to hold the intermediate results of a Calculation Stage
 * @author ikuoikuo
 *
 */
public class CalculationResult {
	public PredictData predictData;
	public DataTable table;
	public ColumnData column;
	public List<ColumnData> weights;
	public CalculationResult prevResult;
	public NSDataSet nsDataSet;

	public void clear() {
		predictData = null;
		table = null;
		column = null;
		weights = null; // Collections.emptyList();
	}
}
