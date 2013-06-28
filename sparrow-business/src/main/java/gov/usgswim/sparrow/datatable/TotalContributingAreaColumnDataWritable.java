package gov.usgswim.sparrow.datatable;

import gov.usgswim.sparrow.AreaType;

/**
 *
 * @author cschroed
 */
public class TotalContributingAreaColumnDataWritable extends AreaColumnDataWritable {
	public TotalContributingAreaColumnDataWritable(int rowCount){
		super(AreaType.TOTAL_CONTRIBUTING, rowCount);
	}
	public TotalContributingAreaColumnDataWritable(){
		this(0);
	}
}
