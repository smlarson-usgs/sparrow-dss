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
public class HUCDataRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long modelID;
	private final UnitAreaType hucLevel;

	public HUCDataRequest(long modelID, UnitAreaType hucLevel) {
		this.modelID = modelID;
		this.hucLevel = hucLevel;
	}

	public long getModelID() {
		return modelID;
	}

	public UnitAreaType getHucLevel() {
		return hucLevel;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HUCDataRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2459, 141).
		append(modelID).
		append(hucLevel).
		append("HUCDataClass").
		toHashCode();
		return hash;
	}

}
