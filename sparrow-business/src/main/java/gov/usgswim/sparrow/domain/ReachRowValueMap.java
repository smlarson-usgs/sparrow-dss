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
public class ReachRowValueMap implements Map<Integer, Float>{

	private final Map<Integer, Float> map;

	/**
	 * Collaborative immutability constructor.
	 * Do not retain a reference to the passed map - use one of the static constructors.
	 * @param map 
	 */
	public ReachRowValueMap(Map<Integer, Float> map) {
		this.map = map;
	}
	

	/**
	 * static builder / constructor
	 * @param <R>
	 * @param map
	 * @return 
	 */
	public static <R extends ReachValue> ReachRowValueMap build(Map<Integer, R> map) {
		if (map != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(map.size(), 1.1f);

			for (R r : map.values()) {
				m.put(r.getRow(), (float) r.getValue());
			}

			return new ReachRowValueMap(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMap(m);
		}
	}
	
	/**
	 * static builder / constructor
	 * @param <R>
	 * @param set
	 * @return 
	 */
	public static <R extends ReachValue> ReachRowValueMap build(Collection<R> set) {
		if (set != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(set.size(), 1.1f);

			for (R r : set) {
				m.put(r.getRow(), (float) r.getValue());
			}

			return new ReachRowValueMap(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMap(m);
		}
	}

	//////////////////////////////
	// Domain Specific Methods
	//////////////////////////////

	/**
	 * Returns true if the passed row number (not an id) exists in the map
	 * @param rowNumber
	 * @return
	 */
	public boolean hasRowNumber(Integer rowNumber) {
		return map.containsKey(rowNumber);
	}

	/**
	 * Returns the delivery fraction for the passed row number (not an id).
	 *
	 * If the row is not in the map, null is returned.
	 * 
	 * @param rowNumber
	 * @return
	 */
	public Float getFraction(Integer rowNumber) {
		return map.get(rowNumber);
	}



	//////////////////////////////
	// Basic Map getter methods
	//////////////////////////////

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object integer) {
		return map.containsKey(integer);
	}

	@Override
	public boolean containsValue(Object o) {
		return map.containsValue(o);
	}

	@Override
	public Float get(Object o) {
		return map.get(o);
	}

	@Override
	public Set<Integer> keySet() {
		return Collections.unmodifiableSet( map.keySet() );
	}

	@Override
	public Collection<Float> values() {
		return Collections.unmodifiableCollection( map.values() );
	}

	@Override
	public Set<Entry<Integer, Float>> entrySet() {
		return Collections.unmodifiableSet( map.entrySet() );
	}



	//////////////////////////////
	// Disallowed modification methods
	//////////////////////////////
	@Override
	public Float put(Integer k, Float v) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Float remove(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Float> map) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
