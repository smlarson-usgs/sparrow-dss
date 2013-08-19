package gov.usgswim.sparrow.datatable;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.impl.SimpleDataTable;

/**
 * Contains sparse columns of reach areas for a particular model
 * @author cschroed
 */
public class ModelReachAreaDataTable extends SimpleDataTable{
	private final String totalUpstreamAreaColumnName;
	private final String totalContributingAreaColumnName;
	private final String incrementalAreaColumnName;
	//the parameterized columns have such specific types to minimize errors
	//from mis-ordering parameters
	public ModelReachAreaDataTable(TotalUpstreamAreaColumnDataWritable totalUpstreamArea,
					TotalContributingAreaColumnDataWritable totalContributingArea,
					IncrementalAreaColumnDataWritable incrementalArea){
		super(new ColumnData[]{totalUpstreamArea, totalContributingArea, incrementalArea});
		totalUpstreamAreaColumnName = totalUpstreamArea.getName();
		totalContributingAreaColumnName = totalContributingArea.getName();
		incrementalAreaColumnName = incrementalArea.getName();
	}
	public ColumnData getTotalUpstreamAreaColumn(){
		return this.getColumn(this.getColumnByName(totalUpstreamAreaColumnName));
	}
	public ColumnData getTotalContributingAreaColumn(){
		return this.getColumn(this.getColumnByName(totalContributingAreaColumnName));
	}
	public ColumnData getIncrementalAreaColumn(){
		return this.getColumn(this.getColumnByName(incrementalAreaColumnName));
	}
}
