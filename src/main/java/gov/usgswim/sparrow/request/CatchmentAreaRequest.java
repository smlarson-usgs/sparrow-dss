package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.datatable.HucLevel;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for a catchment area table.
 * 
 * The request indicates which model and an aggregation type.
 * 
 * @author klangsto
 * 
 */
@Immutable
public class CatchmentAreaRequest implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private final long modelID;
	private final HucLevel hucLevel;
	private final boolean cumulative;

	public CatchmentAreaRequest(long modelID, HucLevel hucLevel, boolean cumulative) {
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
		if (obj instanceof CatchmentAreaRequest) {
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
