package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.action.ReachValue;
import java.util.*;

/**
 * Builder implementation - mutable.
 * @author eeverman
 */
public class ReachRowValueMapBuilder implements ReachRowValueMap {

	private final Map<Integer, Float> map;

	
	public ReachRowValueMapBuilder() {
		this.map = new HashMap<Integer, Float>();
	}
		
	/**
	 * Collaborative constructor that uses the passed Map for storage.
	 * 
	 * Do not hold a ref to the passed map, or alternately, use one of the
	 * build methods which copies the map or collection.
	 * 
	 * @param map 
	 */
	public ReachRowValueMapBuilder(Map<Integer, Float> map) {
		this.map = map;
	}
	

	/**
	 * The returned instance is detached from the passed map.
	 * static builder / constructor
	 * @param <R>
	 * @param map
	 * @return 
	 */
	public static <R extends ReachValue> ReachRowValueMapBuilder buildFromReachValues(Map<Integer, R> map) {
		if (map != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(map.size(), 1.1f);

			for (R r : map.values()) {
				m.put(r.getRow(), (float) r.getValue());
			}

			return new ReachRowValueMapBuilder(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMapBuilder(m);
		}
	}
	
	/**
	 * The returned instance is detached from the passed map.
	 * @param <R>
	 * @param set
	 * @return 
	 */
	public static <R extends ReachValue> ReachRowValueMapBuilder buildFromReachValues(Collection<R> set) {
		if (set != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(set.size(), 1.1f);

			for (R r : set) {
				m.put(r.getRow(), (float) r.getValue());
			}

			return new ReachRowValueMapBuilder(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMapBuilder(m);
		}
	}
	
	/**
	 * Builds a new instance, disconnected from the passed Collection.
	 * static builder / constructor
	 * @param copyFromMap A map that is copied for the values of this instance.
	 * @return 
	 */
	public static ReachRowValueMapBuilder buildFromMap(Map<Integer, Float> copyFromMap) {
		if (copyFromMap != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(copyFromMap.size(), 1.1f);

			for (Map.Entry<Integer, Float> entry : copyFromMap.entrySet()) {
				if (entry.getValue() != null) {
					m.put(entry.getKey(), entry.getValue());
				}
			}

			return new ReachRowValueMapBuilder(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMapBuilder(m);
		}
	}

	//////////////////////////////
	// Domain Specific Methods
	//////////////////////////////

	@Override
	public boolean hasRowNumber(Integer rowNumber) {
		return map.containsKey(rowNumber);
	}

	@Override
	public Float getFraction(Integer rowNumber) {
		return map.get(rowNumber);
	}
	
	@Override
	public ReachRowValueMap toImmutable() {
		return ReachRowValueMapImm.buildFromMap(map);
	}
	
	
	public void mergeByAddition(ReachRowValueMap toBeAdded) {
		for (Map.Entry<Integer, Float> entry : toBeAdded.entrySet()) {
			Float existing = getFraction(entry.getKey());
			Float addVal = entry.getValue();
			
			if (addVal != null) {
				if (existing != null) addVal += existing;
				map.put(entry.getKey(), addVal);
			}
		}
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
	public Set<Map.Entry<Integer, Float>> entrySet() {
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
