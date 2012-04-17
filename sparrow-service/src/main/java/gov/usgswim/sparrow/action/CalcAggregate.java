package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.service.predict.aggregator.AggregateType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * This action creates a ColumnData containing the delivery
 * fractions to the set of Target reaches.
 * 
 * @author eeverman
 *
 */
public class CalcAggregate extends Action<SparrowColumnSpecifier> {

	protected SparrowColumnSpecifier data;
	protected DataTable reachHucs;	//as loaded by LoadReachHucs action
	protected AggregateType aggType;
	
	protected String msg = null;	//statefull message for logging
	
	/**
	 * Sets the column of data that is to be aggregated.
	 * 
	 * The passed column data must have row ids that are reach ids.
	 * @param data
	 */
	public void setData(SparrowColumnSpecifier data) {
		this.data = data;
		
		if (! data.getTable().hasRowIds()) {
			throw new IllegalArgumentException("The table in the ColumnData must have row ids.");
		}
	}

	/**
	 * Sets the HUC data to use for the aggregation.  The structure of this
	 * data is defined in the LoadReachHucs action and it must be based on
	 * the network in use for the results passed for the column data. 
	 * @param reachHucs
	 */
	public void setReachHucs(DataTable reachHucs) {
		this.reachHucs = reachHucs;
	}

	/**
	 * The type of aggregation to do.
	 * 
	 * @param aggType
	 */
	public void setAggType(AggregateType aggType) {
		this.aggType = aggType;
	}
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public SparrowColumnSpecifier doAction() throws Exception {
		
		//If none type, return the base data.
		//This could be an error, but its logically consistent.
		if (aggType.equals(AggregateType.none)) {
			return data;
		}
		
		
		DataTable dataTbl = data.getTable();
		TreeMap<Long, AggregateData> aggDataMap = new TreeMap<Long, AggregateData>();
		ArrayList<Long> unmatchedRowIds = new ArrayList<Long>();
		
		for (int r = 0; r < dataTbl.getRowCount(); r++) {
			
			Long rowId = data.getIdForRow(r);
			Double val = data.getDouble(r);
			int reachHucRow = reachHucs.getRowForId(rowId);
			
			if (reachHucRow > -1) {
				String hucString = reachHucs.getString(reachHucRow, 0);
				Long hucId = Long.parseLong(hucString);
				
				AggregateData ad = aggDataMap.get(hucId);
				
				if (ad == null) {
					ad = new AggregateData(aggType, hucId);
					aggDataMap.put(hucId, ad);
				}
				
				ad.add(val);
				
			} else {
				unmatchedRowIds.add(rowId);
			}
			
		}
		
		Iterator<AggregateData> it = aggDataMap.values().iterator();
		ColumnDataWritable column = new StandardNumberColumnDataWritable<Double>().setType(Double.class);
		column.setUnits(data.getUnits());
		column.setName(data.getColumnName() +
				" (Agg'ed to HUC " + data.getTableProperty("huc_level") + ")");
		
		DataTableWritable result = new SimpleDataTableWritable();
		result.addColumn(column);
		int resultRow = 0;
		
		while (it.hasNext()) {
			AggregateData val = it.next();
			result.setValue(val.getValue(), resultRow, 0);
			result.setRowId(val.getAggLevelId(), resultRow);
			
			resultRow++;
		}
		
				
		return new SparrowColumnSpecifier(result, 0, null);

	}
	
	
}
