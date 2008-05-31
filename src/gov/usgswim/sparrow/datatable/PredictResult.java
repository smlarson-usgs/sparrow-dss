package gov.usgswim.sparrow.datatable;

import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.Immutable;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;

/**
 * 
 */
@Immutable
//TODO:  [EE]  Add gett'ers that are data specific (getSource())
public class PredictResult extends SimpleDataTable {
	
	public PredictResult(ColumnData[] columns, long[] rowIds) {
		super(columns, "Prediction Data", "Prediction Result Data", Collections.<String, String>emptyMap(), null);
	}

	public static PredictResult buildPredictResult(double[][] data, PredictData predictData) {
		
		ColumnData[] columns = new ColumnData[data[0].length];
		int sourceCount = predictData.getSrc().getColumnCount();
		
		//Create Source Columns
		for (int c=0; c<data[0].length; c++) {
			columns[c] = new ImmutableDoubleColumn(data, c, "name", "units", "description", null);
		}
		
		for (int i = 0; i < sourceCount; i++)  {

			String name = StringUtils.trimToNull(predictData.getSrc().getName(i));

			if (name == null) name = "Source " + i;
			
			columns[i] = new ImmutableDoubleColumn(data, i, name + " Inc. Addition", "units", "description", null);
			columns[i + sourceCount] = new ImmutableDoubleColumn(data, i + sourceCount, name + " Total (w/ upstream, decayed)", "units", "description", null);
		}
		

		columns[2 * sourceCount] = new ImmutableDoubleColumn(data, 2 * sourceCount, "Total Inc. (not decayed)", "units", "description", null);
		columns[(2 * sourceCount) + 1] = new ImmutableDoubleColumn(data, (2 * sourceCount) + 1, "Grand Total (measurable)", "units", "description", null);
		
		
		if (predictData.getSys() != null) {
			long[] ids = TemporaryHelper.getRowIds(predictData.getSys());
			return new PredictResult(columns, ids);
		} else {
			return new PredictResult(columns, null);
		}
		
		
		
		
	}

}
