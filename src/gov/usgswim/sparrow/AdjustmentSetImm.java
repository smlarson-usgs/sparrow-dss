package gov.usgswim.sparrow;

import gov.usgswim.Immutable;


import java.util.TreeSet;


/**
 * A collection of Adjustments to be applied to sources.
 */
@Immutable
public class AdjustmentSetImm implements AdjustmentSet {

	protected final TreeSet<Adjustment> adjustments;
	
	public AdjustmentSetImm(TreeSet<Adjustment> adjs) {
		adjustments = adjs;
	}
	
	public AdjustmentSetImm(Adjustment[] adjs) {
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
	
	public int getAdjustmentCount() {
		return adjustments.size();
	}
	
	public Adjustment[] getAdjustments() {
		return adjustments.toArray(new Adjustment[adjustments.size()]);
	}


	public boolean equals(Object object) {
		if (object instanceof AdjustmentSet) {
		
			AdjustmentSet that = (AdjustmentSet) object;
			
			if (that.getAdjustmentCount() == getAdjustmentCount()) {
				Adjustment[] thisAdjs = getAdjustments();
				Adjustment[] thatAdjs = that.getAdjustments();
				
				
				for (int i = 0; i < thisAdjs.length; i++)  {
					if (! (thisAdjs[i].equals(thatAdjs[i]))) {
						return false;	//Adjustment differs
					}
				}
				
				return true;
				
			} else {
				return false;	//Different number of adjustments
			}
			
		} else {
			return false;	//not an AdjustmentSet
		}
	}
}
