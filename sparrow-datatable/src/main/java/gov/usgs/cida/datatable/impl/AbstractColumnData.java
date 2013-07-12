package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.ColumnData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractColumnData implements ColumnData {

	protected final String name;
	protected Map<String, String> properties = new HashMap<String, String>();
	protected final String units;
	protected final Class<?> type;
	protected final String description;
	protected Map<Object, int[]> index;
	protected boolean isValid;
	// Min-max values
	protected transient Double maxDouble;
	protected transient Double minDouble;

	// ===========
	// Constructor
	// ===========
	protected AbstractColumnData(String name, Class<?> type, String units, String desc, Map<String, String> properties, Map<Object, int[]> index) {
		this.type = type;
		this.name = name;
		this.units = units;
		this.description = desc;
		if (properties != null) {
			this.properties.putAll(properties);
		}
		this.index = index;
		// no errors, so assume this is valid
		isValid = true;
	}


	// =========================
	// Standard Instance Methods
	// =========================
	public String getName() {
		return name;
	}
	public String getUnits() {
		return units;
	}
	public Class<?> getDataType() {
		return type;
	}
	public String getDescription() {
		return description;
	}
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Return the underlying primitive array
	 * @return
	 */
	protected abstract Object getValues();

	// =================
	// Property Handlers
	// =================
	public String getProperty(String key) {
		return properties.get(key);
	}
	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	@Override
	public Map<String, String> getProperties() {
		HashMap<String, String> tmp = new HashMap<String, String>();
		tmp.putAll(properties);
		return tmp;
	}


	// ======================
	// Find and Index Methods
	// ======================

	public boolean isIndexed() {
		return index != null;
	}
	public int[] findAll(Object value) {
		int[] result = (index != null)? index.get(value): findAllWithoutIndex(value);
		return (result == null)? new int[0]: result;
	}

	public int findFirst(Object value) {
		if (index!= null) {
			int[] found = index.get(value);
			// return the first found element or -1
			return (found == null)? -1: found[0];
		}
		return findFirstWithoutIndex(value);
	}

	public int findLast(Object value) {
		if (index!= null) {
			int[] found = index.get(value);
			// either return the last found element or -1
			return (found == null)? -1: found[ found.length-1 ];
		}
		return findLastWithoutIndex(value);
	}

	protected int[] findAllWithoutIndex(Object value) {
		return FindHelper.findAllWithoutIndex(value, this);
	}

	protected int findFirstWithoutIndex(Object value) {
		return FindHelper.bruteForceFindFirst(this, value);
	}

	protected int findLastWithoutIndex(Object value) {
		return FindHelper.bruteForceFindLast(this, value);
	}

	// ===============
	// MIN-MAX METHODS
	// ===============
	public Double getMaxDouble() {
		if (maxDouble != null) {
			return maxDouble;
		} else {
			maxDouble = getMaxD();
			return maxDouble;
		}
	}

	public Double getMinDouble() {
		if (minDouble != null) {
			return minDouble;
		} else {
			minDouble = getMinD();
			return minDouble;
		}
	}

	public Integer getMaxInt() {
		Double dbl = getMaxDouble();

		if (dbl != null) {
			return dbl.intValue();
		} else {
			return null;
		}
	}

	public Integer getMinInt() {
		Double dbl = getMinDouble();

		if (dbl != null) {
			return dbl.intValue();
		} else {
			return null;
		}
	}

	protected Double getMaxD() {
		return FindHelper.findMaxDouble(this);
	}

	protected Double getMinD() {
		return FindHelper.findMinDouble(this);
	}

	// ----------------------------------
	// utils
	// ----------------------------------
	protected static Map<Object, int[]> buildIndex(ColumnData data) {
		Map<Object, List<Integer>> preIndex = new HashMap<Object, List<Integer>>();
		for (int i=0; i<data.getRowCount(); i++) {
			Object value = data.getValue(i);
			List<Integer> found = preIndex.get(value);
			if (found == null) {
				found = new ArrayList<Integer>();
				preIndex.put(value, found);
			}
			found.add(i);
		}
		return BuilderHelper.convertIndex(preIndex);
	}
	@Override
	public String toString(){
		return this.name + " [" + this.type + "] " + this.description;
	}
}
