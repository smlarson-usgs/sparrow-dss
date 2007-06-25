package gov.usgswim.sparrow;

import gov.usgswim.Immutable;


import java.util.TreeSet;

import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * A collection of Adjustments to be applied to sources.
 * 
 * The equals() and hashCode() methods have been overridden to indicate that
 * instances w/ identical adjustments are equal.  Note that this can only be
 * done w/ this immutable implementation since the collection of Ajustments
 * can change in a non-immutable class (thus causing the hashcode to change).
 */
@Immutable
public class AdjustmentSetImm implements AdjustmentSet {

	protected final TreeSet<Adjustment> adjustments;
	private Integer hash;	//Not strictly threadsafe, but recalculation is cheap and non-destructive
	
	public AdjustmentSetImm() {
		//this instance will never have any adjustments
		adjustments = null;
	}
	
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
	 * Creates a new Data2D source by creating a coef-view using the same underlying
	 * data w/ coefficients on top.  This strategy allows the underlying data
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
		if (adjustments != null) {
			return adjustments.size();
		} else {
			return 0;
		}
	}
	
	public boolean hasAdjustments() {
		return adjustments != null && adjustments.size() > 0;
	}
	
	public Adjustment[] getAdjustments() {
		if (adjustments != null) {
			return adjustments.toArray(new Adjustment[adjustments.size()]);
		} else {
			return new Adjustment[0];
		}
	}


	public boolean equals(Object object) {
		if (object instanceof AdjustmentSet) {
		
			return hashCode() == object.hashCode();
			
		} else {
			return false;	//not an AdjustmentSet
		}
	}
	
	public int hashCode() {
		
		if (hash == null) {
			//starts w/ some random numbers just to create unique results
			HashCodeBuilder hcb = new HashCodeBuilder(798641, 68431);
			
			if (adjustments != null) {
				Adjustment[] adjs = getAdjustments();
				
				for (Adjustment a : adjs) {
					hcb.append(a.hashCode());
				}
			}
			
			hash = hcb.toHashCode();
		}
		
		return hash;
	}
}
