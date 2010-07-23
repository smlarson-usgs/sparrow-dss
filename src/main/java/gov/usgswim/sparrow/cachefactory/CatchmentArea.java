package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.datatable.HucLevel;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple bean class to hold information for a catchment area
 * request. This class serves as a key to cached CatchmentArea.
 * 
 * @author klangsto
 * 
 */
@Immutable
public class CatchmentArea {
	private final long modelID;
	private final HucLevel hucLevel;
	private final boolean cumulative;

	public CatchmentArea(long modelID, HucLevel hucLevel, boolean cumulative) {
		this.modelID = modelID;
		this.hucLevel = hucLevel;
		this.cumulative = cumulative;
	}

	public long getModelID() {
		return modelID;
	}

	public HucLevel getHucLevel() {
		return hucLevel;
	}
	
	public boolean getCumulative() {
		return cumulative;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CatchmentArea) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(modelID).
		append(hucLevel).
		append(cumulative).
		toHashCode();
		return hash;
	}

}
