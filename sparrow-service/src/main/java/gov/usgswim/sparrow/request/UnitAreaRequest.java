package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.UnitAreaType;

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
public class UnitAreaRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long modelID;
	private final UnitAreaType hucLevel;
	private final boolean cumulative;

	public UnitAreaRequest(long modelID, UnitAreaType hucLevel, boolean cumulative) {
		this.modelID = modelID;
		this.hucLevel = hucLevel;
		this.cumulative = cumulative;
	}

	public long getModelID() {
		return modelID;
	}

	public UnitAreaType getHucLevel() {
		return hucLevel;
	}
	
	public boolean getCumulative() {
		return cumulative;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UnitAreaRequest) {
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
