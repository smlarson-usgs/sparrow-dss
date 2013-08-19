package gov.usgswim.sparrow.datatable;

import gov.usgswim.sparrow.AreaType;

/**
 *
 * @author cschroed
 */
public class IncrementalAreaColumnDataWritable extends AreaColumnDataWritable {
	public IncrementalAreaColumnDataWritable(int rowCount){
		super(AreaType.INCREMENTAL, rowCount);
	}
	public IncrementalAreaColumnDataWritable(){
		this(0);
	}
}
