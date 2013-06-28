package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.action.ReachValue;
import java.util.*;

/**
 * Immutable implementation.
 * @author eeverman
 */
public class ReachRowValueMapImm implements ReachRowValueMap {

	private final Map<Integer, Float> map;

	/**
	 * Collaborative immutability constructor.
	 * This constructor will retain a reference to the passed map - use one of the
	 * static builder methods or the ReachRowValueMapBuilder class instead.
	 * 
	 * @param map 
	 */
	private ReachRowValueMapImm(Map<Integer, Float> map) {
		this.map = map;
	}
	

	/**
	 * Builds a new instance, disconnected from the passed Map.
	 * static builder / constructor
	 * @param <R>
	 * @param map
	 * @return 
	 */
	public static <R extends ReachValue> ReachRowValueMapImm buildFromReachValues(Map<Integer, R> map) {
		if (map != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(map.size(), 1.1f);

			for (R r : map.values()) {
				m.put(r.getRow(), (float) r.getValue());
			}

			return new ReachRowValueMapImm(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMapImm(m);
		}
	}
	
	/**
	 * Builds a new instance, disconnected from the passed Collection.
	 * static builder / constructor
	 * @param <R>
	 * @param set
	 * @return 
	 */
	public static <R extends ReachValue> ReachRowValueMap buildFromReachValues(Collection<R> set) {
		if (set != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(set.size(), 1.1f);

			for (R r : set) {
				m.put(r.getRow(), (float) r.getValue());
			}

			return new ReachRowValueMapImm(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMapImm(m);
		}
	}
	
	/**
	 * Builds a new instance, disconnected from the passed Collection.
	 * static builder / constructor
	 * @param <R>
	 * @param set
	 * @return 
	 */
	public static ReachRowValueMapImm buildFromMap(Map<Integer, Float> copyFromMap) {
		if (copyFromMap != null) {
			HashMap<Integer, Float> m = new HashMap<Integer, Float>(copyFromMap.size(), 1.1f);

			for (Map.Entry<Integer, Float> entry : copyFromMap.entrySet()) {
				if (entry.getValue() != null) {
					m.put(entry.getKey(), entry.getValue());
				}
			}

			return new ReachRowValueMapImm(m);

		} else {
			Map<Integer, Float> m = Collections.emptyMap();
			return new ReachRowValueMapImm(m);
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
		return this;
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
