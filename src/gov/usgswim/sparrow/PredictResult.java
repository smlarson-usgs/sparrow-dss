package gov.usgswim.sparrow;

import gov.usgswim.Immutable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

/**
 * 
 */
@Immutable
public class PredictResult extends SimpleDataTableWritable {
	
	public PredictResult(double[][] data, String[] headings) {
		super(data, headings);
	}


}
