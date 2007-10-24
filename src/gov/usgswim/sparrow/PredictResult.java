package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

import java.util.HashMap;

import java.util.LinkedList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * A direct subclass of  Double2DImm.
 * 
 * New features will be added...
 */
@Immutable
public class PredictResult extends Double2DImm {
	
	public PredictResult(Double2DImm data, int[] reachIds) {
		//TODO This is inefficent.  How can we avoid copying this data?
		//Perhaps the PredictRunner could return a mutable dataset so we can just
		//take the actual data out of it w/o duplicating it?
		super(data.getDoubleData(), data.getHeadings(), -1, reachIds);
	}
	
	public PredictResult(double[][] data, String[] headings, int[] reachIds) {
		super(data, headings, -1, reachIds);
	}
	
}
