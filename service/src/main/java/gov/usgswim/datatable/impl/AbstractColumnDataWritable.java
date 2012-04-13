package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnDataWritable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO:  The search may fail in these classes in cases where data storage is of
//one type (double) and the search value is of another (int).
public abstract class AbstractColumnDataWritable extends AbstractColumnData implements
		ColumnDataWritable {
	protected String description;
	protected List<Object> values = new ArrayList<Object>();
	protected String tentativeUnits;
	protected String tentativeName;

	// ===========
	// CONSTRUCTOR
	// ===========
	protected AbstractColumnDataWritable(String name, Class<?> type, String units, Map<String, String> properties) {
		super(name, type, units, null, properties, null);
		this.tentativeName = name;
		this.tentativeUnits = units;
	}

	// ================
	// Instance Methods
	// ================
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	@Override
	public String getDescription() {
		// IK: Necessary to override parent implementation, as that points to
		// different final field, tricky little bastard.
		return description;
	}

	public Integer getRowCount() {
		return (values == null)? null: values.size();
	}

	//
	protected void invalidate() {
		properties = null;
		description = null;
	}

	public void setUnits(String units) {
		this.tentativeUnits = units;
	}

	public void setName(String name) {
		this.tentativeName = name;
	}

	@Override
	public String getUnits() {
		return this.tentativeUnits;
	}

	@Override
	public String getName() {
		return this.tentativeName;
	}
	// ====================
	// Find & Index Methods
	// ====================
	protected List<?> getValuesList() {
		return this.values;
	}

	public void buildIndex() {
		Map<Object, List<Integer>> preIndex = new HashMap<Object, List<Integer>>();
		for (int i=0; i<getRowCount(); i++) {
			Object value = getValue(i);
			List<Integer> found = preIndex.get(value);
			if (found == null) {
				found = new ArrayList<Integer>();
				preIndex.put(value, found);
			}
			found.add(i);
		}
		this.index = BuilderHelper.convertIndex(preIndex);
	}

	@Override
	protected int[] findAllWithoutIndex(Object value) {
		List<Integer> preResult = new ArrayList<Integer>();
		for (int i=0; i<getRowCount(); i++) {
			Object currValue = getValue(i);
			if ((value == null && currValue == null) ||
					(value != null && value.equals(currValue))) {
				preResult.add(i);
			}
		}
		return BuilderHelper.toIntArray(preResult);
	}

	@Override
	protected int findFirstWithoutIndex(Object value) {
		for (int i=0; i<getRowCount(); i++) {
			Object currValue = getValue(i);
			if ((value == null && currValue == null ||
					(value != null && value.equals(currValue)))){
				return i;
			}
		}
		return -1;
	}

	@Override
	protected int findLastWithoutIndex(Object value) {
		return getValuesList().lastIndexOf(value); // nice convenience method
	}




}
