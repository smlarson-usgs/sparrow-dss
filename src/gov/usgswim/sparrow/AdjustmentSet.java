package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Map;

import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * A collection of Adjustments to be applied to sources.
 */
@Immutable
public class AdjustmentSet {

	protected TreeSet<Adjustment> adjustments;
	
	public AdjustmentSet(TreeSet<Adjustment> adjs) {
		adjustments = adjs;
	}
	
	public AdjustmentSet(Adjustment[] adjs) {
		adjustments = new TreeSet<Adjustment>();
		for (Adjustment a : adjs) {
			adjustments.add(a);
		}
	}
	

	/**
	 * Modifies the passed source by creating a coef-view using the same underlying
	 * data, but adding coefficients.  This strategy allows the underlying data
	 * to be cached and not modified.
	 * 
	 * If no adjustments are made, the passed data is returned and no view is created,
	 * so DO NOT count on the returned data being a view - check using ==.
	 * 
	 * @param source
	 * @return
	 */
	public Data2D adjustSources(Data2D source) {

		if (adjustments != null) {
			
			
			Data2DColumnCoefView view = new Data2DColumnCoefView(source);
			
			for (Adjustment a: adjustments) {
				//TODO This assumes we need a Data2DColumnCoefView.  How to handle other types?
				a.adjust(view);
			}
			

			return view;
			
		} else {
			
			return source;	//no adjustment
		}
		
	}
	
	public Adjustment[] getAdjustments() {
		return (Adjustment[]) adjustments.toArray();
	}
	

}
