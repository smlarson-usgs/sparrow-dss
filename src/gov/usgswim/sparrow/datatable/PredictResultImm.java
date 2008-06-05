package gov.usgswim.sparrow.datatable;

import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.Immutable;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import static gov.usgswim.sparrow.service.predict.AggregateType.*;
import static gov.usgswim.sparrow.service.predict.ValueType.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;

/**
 * An immutable implementation of PredictResult.
 * 
 * This will likely be the only implementation unless the PredictRunner code is
 * modified to use a builder instead of arrays.
 * 
 * @author eeverman
 */
@Immutable
public class PredictResultImm extends SimpleDataTable implements PredictResult {
	
	/**
	 * A mapping from a source Identifier to the column number of the column containing
	 * the Incremental value for that source.
	 */
	private final Map<Long, Integer> srcIdIncMap;
	
	/**
	 * A mapping from a source Identifier to the column number of the column containing
	 * the Total value for that source.
	 */
	private final Map<Long, Integer> srcIdTotalMap;

	/**
	 * The column of the total incremental column
	 */
	private final int totalIncCol;
	
	/**
	 * The column of the total Total column
	 */
	private final int totalTotalCol;
	
	/**
	 * The number of sources
	 */
	private final int sourceCount;
	
	
	public PredictResultImm(ColumnData[] columns, long[] rowIds, 
				Map<Long, Integer> srcIdIncMap, Map<Long, Integer> srcIdTotalMap,
				int totalIncCol, int totalTotalCol) {
		
		super(columns, "Prediction Data", "Prediction Result Data", Collections.<String, String>emptyMap(), rowIds);
		

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdIncMap.size() * 2 + 1);
			map.putAll(srcIdIncMap);
			this.srcIdIncMap = map;
		}

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdTotalMap.size() * 2 + 1);
			map.putAll(srcIdTotalMap);
			this.srcIdTotalMap = map;
		}
		
		this.totalIncCol = totalIncCol;
		this.totalTotalCol = totalTotalCol;
		this.sourceCount = srcIdIncMap.size();
	}

	public static PredictResultImm buildPredictResult(double[][] data, PredictData predictData) throws Exception {
		
		ColumnData[] columns = new ColumnData[data[0].length];
		int sourceCount = predictData.getSrc().getColumnCount();
		
		//Same definition as the instance vars
		Map<Long, Integer> srcIdIncMap = new Hashtable<Long, Integer>(13, 2);
		Map<Long, Integer> srcIdTotalMap = new Hashtable<Long, Integer>(13, 2);

		// ------------------------------------------
		// Define the source columns of the DataTable
		// ------------------------------------------
//		DataTable srcMetaData = predictData.getSrcMetadata();
//		boolean hasMetaData = (srcMetaData != null); // only use the metadata if it exists
//		Integer nameCol = (hasMetaData)? srcMetaData.getColumnByName("NAME"): -1;
		
		for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++)  {

			String name = StringUtils.trimToNull(predictData.getSrc().getName(srcIndex));

			if (name == null) name = "Source " + srcIndex;
			
			int srcIncAddIndex = srcIndex;
			int srcTotalIndex = srcIndex + sourceCount;
			
			srcIdIncMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcIncAddIndex); 
			srcIdTotalMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcTotalIndex); 
			
			Map<String, String> incProps = new HashMap<String, String>();
			incProps.put(VALUE_TYPE_PROP, incremental.name());
			
			Map<String, String> totProps = new HashMap<String, String>();
			totProps.put(VALUE_TYPE_PROP, total.name());
			

			columns[srcIncAddIndex] = new ImmutableDoubleColumn(data, srcIncAddIndex, name + " Inc. Addition", "units", "description", incProps);
			columns[srcTotalIndex] = new ImmutableDoubleColumn(data, srcTotalIndex, name + " Total (w/ upstream, decayed)", "units", "description", totProps);
		}
		
		// ------------------------------------------
		// Define the total columns of the DataTable
		// ------------------------------------------
		int totalIncCol = 2 * sourceCount;	//The total inc col comes right after the two sets of source columns
		Map<String, String> totalIncProps = new HashMap<String, String>();
		totalIncProps.put(VALUE_TYPE_PROP, incremental.name());
		totalIncProps.put(AGGREGATE_TYPE_PROP, sum.name());
		
		int totalTotalCol = totalIncCol + 1; //The grand total col comes right after the total incremental col
		Map<String, String> grandTotalProps = new HashMap<String, String>();
		grandTotalProps.put(VALUE_TYPE_PROP, total.name());
		grandTotalProps.put(AGGREGATE_TYPE_PROP, sum.name());
		
		columns[totalIncCol] = new ImmutableDoubleColumn(data, totalIncCol, "Total Inc. (not decayed)", "units", "description", totalIncProps);
		columns[totalTotalCol] = new ImmutableDoubleColumn(data, totalTotalCol, "Grand Total (measurable)", "units", "description", grandTotalProps);
		
		// only get the ids if available
		long[] ids = (predictData.getSys() != null)? TemporaryHelper.getRowIds(predictData.getSys()): null;
		
		return new PredictResultImm(columns, ids, srcIdIncMap, srcIdTotalMap, totalIncCol, totalTotalCol);
	}

	public int getSourceCount() {
	  return sourceCount;
  }
	
	public Double getIncremental(int row) {
	  return getDouble(row, totalIncCol);
  }

	public int getIncrementalCol() {
	  return totalIncCol;
  }
	
	public Double getTotal(int row) {
		return getDouble(row, totalTotalCol);
  }

	public int getTotalCol() {
	  return totalTotalCol;
  }

	public int getIncrementalColForSrc(Long srcId) {
	  return srcIdIncMap.get(srcId);
  }

	public Double getIncrementalForSrc(int row, Long srcId) {
	  return getDouble(row, srcIdIncMap.get(srcId));
  }

	public int getTotalColForSrc(Long srcId) {
		return srcIdTotalMap.get(srcId);
  }

	public Double getTotalForSrc(int row, Long srcId) {
		return getDouble(row, srcIdTotalMap.get(srcId));
  }

}
