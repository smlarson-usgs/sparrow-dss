package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.action.FractionalReach;
import gov.usgswim.sparrow.action.ReachValue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A lightweight structure for storing values mapped by row ID.
 * 
 * This is currently used for
 * <ul>
 * <li>CalcDeliveryFractionMap - The Delivery Fraction data series calculation
 * <li>CAlcAreaFractionMap - Fraction of incremental area 'counted' towards cumulative area
 * </ul>
 *
 * A Java Map could be used, however, this class allows other strategies to be
 * used for large sets of data if needed.
 * IE., if a large percentage of the reaches in a model are in the
 * map, it may make more sense to use an array for storage instead of a map.
 *
 * @author eeverman
 */
public interface ReachRowValueMap extends Map<Integer, Float> {

	//////////////////////////////
	// Domain Specific Methods
	//////////////////////////////

	/**
	 * Returns true if the passed row number (not an id) exists in the map
	 * @param rowNumber
	 * @return
	 */
	public boolean hasRowNumber(Integer rowNumber);

	/**
	 * Returns the delivery fraction for the passed row number (not an id).
	 *
	 * If the row is not in the map, null is returned.
	 * 
	 * @param rowNumber
	 * @return
	 */
	public Float getFraction(Integer rowNumber);
	
	
	/**
	 * Returns an immutable version of this instance.
	 * @return 
	 */
	public ReachRowValueMap toImmutable();
	
}
