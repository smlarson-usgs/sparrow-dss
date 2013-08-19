package gov.usgswim.sparrow.datatable;

import gov.usgswim.sparrow.AreaType;

/**
 *
 * @author cschroed
 */
public class TotalUpstreamAreaColumnDataWritable extends AreaColumnDataWritable {
	public TotalUpstreamAreaColumnDataWritable(int rowCount){
		super(AreaType.TOTAL_UPSTREAM, rowCount);
	}
	public TotalUpstreamAreaColumnDataWritable(){
		this(0);
	}
}
