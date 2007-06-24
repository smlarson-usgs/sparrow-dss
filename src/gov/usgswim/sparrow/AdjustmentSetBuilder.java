package gov.usgswim.sparrow;

import gov.usgswim.NotThreadSafe;

import java.util.ArrayList;

import java.util.List;

import java.util.Map;

import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * A Builder for AdjustmentSet.
 */
@NotThreadSafe
public class AdjustmentSetBuilder implements ImmutableBuilder<AdjustmentSetImm>,
																						 AdjustmentSet {

	
	protected TreeSet<Adjustment> adjustments;
	
	public AdjustmentSetBuilder() {
		adjustments = new TreeSet<Adjustment>();
	}
	
	
	/**
	 * Sets the adjustments, removing any previous.
	 * 
	 * @param adjs A map containing adjustment names and values.
	 */
	public synchronized void setAdjustments(Map adjs) {
	
		for(Adjustment.AdjustmentType type: Adjustment.AdjustmentType.values()) {
			String val = (String) adjs.get( type.getName() );
			
			switch (type) {
				case GROSS_ADJUST:
				
					adjustments.addAll(parseGrossAdj(val));
					
					break;
				default:
					throw new IllegalStateException("Unexpected AdjustmentType.");
			}
		}
	}
	
	
	/**
	 * Adds the adjustment.
	 * 
	 * Adjustments have a normal sort order
	 * 
	 * @param adj  An adjustment to add
	 */
	public synchronized void addAdjustment(Adjustment adj) {
		adjustments.add(adj);
	}

	
	public Adjustment[] getAdjustments() {
		return adjustments.toArray(new Adjustment[adjustments.size()]);
	}
	
	public int getAdjustmentCount() {
		return adjustments.size();
	}
	
	/**
	 * Reads the gross adjustments from a string
	 * @param adj
	 * @return
	 */
	protected List<Adjustment> parseGrossAdj(String adj) {

		adj = StringUtils.trimToNull(adj);
		
		if (adj != null ) {
			String[] adjs = StringUtils.split(adj, ", ;:|[]{}()");
			List<Adjustment> list = new ArrayList<Adjustment>(adjs.length / 2 + 1);
			
			for (int i = 0; i < adjs.length; i+=2)  {
				int col = Integer.parseInt(adjs[i]);
				double coef = Double.parseDouble(adjs[i + 1]);
				list.add(new Adjustment(Adjustment.AdjustmentType.GROSS_ADJUST, col, coef));
			}
			
			return list;
		} else {
			return null;
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
	
	public AdjustmentSetImm getImmutable() {
		return new AdjustmentSetImm((Adjustment[]) adjustments.toArray());
	}
}
