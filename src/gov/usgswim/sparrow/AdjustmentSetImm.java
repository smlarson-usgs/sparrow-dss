package gov.usgswim.sparrow;

import gov.usgswim.Immutable;


import java.util.List;
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
	
	public Data2D adjust(Data2D source, Data2D srcIndex, Data2D reachIndex) throws Exception {
		return adjustSources(adjustments, source, srcIndex, reachIndex);
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
	
	/**
	 * Creates a new Data2D source by creating layered views as needed.
	 * 
	 * The underlying data is not modified.
	 * 
	 * If no adjustments are made, the passed data is returned and no view is created,
	 * so DO NOT count on the returned data being a view - check using ==.
	 * 
	 * @param source
	 * @param reachIndex Should return a row number in source for a given reach id.
	 * @return
	 */
	 //TODO:  Rename to doAdjust
	public static Data2D adjustSources(TreeSet<Adjustment> adjs, Data2D source, Data2D srcIndex, Data2D reachIndex) throws Exception {

		if (adjs != null) {
			
			Data2D view = null;

			for (Adjustment a: adjs) {
			
				switch (a.getType()) {
				case GROSS_SRC_ADJUST:
					if (! (view instanceof Data2DColumnCoefView)) view = new Data2DColumnCoefView((view == null)?source:view);
					break;
				case SPECIFIC_ADJUST:
					if (! (view instanceof Data2DViewWriteLocal)) view = new Data2DViewWriteLocal((view == null)?source:view);
					break;
				default:
					throw new Exception("Unsupported Adjustment type '" + a + "'");
				}
				a.adjust(view, srcIndex, reachIndex);
			}
			
			return view;
			
		} else {
			
			return source;	//no adjustment
		}
		
	}
}
