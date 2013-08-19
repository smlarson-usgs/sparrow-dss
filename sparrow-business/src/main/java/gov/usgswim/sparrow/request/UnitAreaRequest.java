package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.AreaType;


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

	private static final long serialVersionUID = 2L;

	private final long modelID;
	private final AreaType areaCalculationType;

	public UnitAreaRequest(long modelID, AreaType areaCalculationType) {
		this.modelID = modelID;
		this.areaCalculationType = areaCalculationType;
	}

	public long getModelID() {
		return modelID;
	}

	public AreaType getAreaCalculationType() {
		return areaCalculationType;
	}

	@Override
	public boolean equals(Object obj) {
		Boolean objectsAreEqual = false;
		if (obj instanceof UnitAreaRequest) {
			UnitAreaRequest otherObj = (UnitAreaRequest)obj;
			if(otherObj.hashCode() == hashCode()){
				objectsAreEqual =
					otherObj.modelID == this.modelID &&
					otherObj.areaCalculationType == this.areaCalculationType;
			}
		}
		return objectsAreEqual;
	}

	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(modelID).
		append(areaCalculationType).
		toHashCode();
		return hash;
	}

}
