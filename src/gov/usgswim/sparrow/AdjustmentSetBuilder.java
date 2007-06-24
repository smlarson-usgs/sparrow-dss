package gov.usgswim.sparrow;

import gov.usgswim.NotThreadSafe;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.Adjustment.AdjustmentType;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Map;

import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * A Builder for AdjustmentSet.
 */
@NotThreadSafe
public class AdjustmentSetBuilder implements ImmutableBuilder<AdjustmentSet> {

	
	protected TreeSet<Adjustment> adjustments;
	
	public AdjustmentSetBuilder() {
		adjustments = new TreeSet<Adjustment>();
	}
	
	
	/**
	 * Sets the adjustments for the given type.
	 * 
	 * Any existing adjustments of the same type are removed and replaces with
	 * the new ones, which are parsed from the passed string.
	 * 
	 * Adjustments are ordered.  Ajustments of the same type may overrite each other
	 * and the last ones wins.  Thus, the order must be preserved.
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
		return (Adjustment[]) adjustments.toArray();
	}
	
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


	public AdjustmentSet getImmutable() {
		return new AdjustmentSet((Adjustment[]) adjustments.toArray());
	}
}
