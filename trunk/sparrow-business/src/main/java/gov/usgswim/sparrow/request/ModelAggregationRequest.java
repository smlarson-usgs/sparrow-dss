package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.AggregationLevel;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for any Action that rolls up values to an AggregationLevel
 * 
 * 
 * @author eeverman
 * 
 */
@Immutable
public class ModelAggregationRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long modelID;
	private final AggregationLevel aggLevel;

	public ModelAggregationRequest(long modelID, AggregationLevel hucLevel) {
		this.modelID = modelID;
		this.aggLevel = hucLevel;
	}

	public long getModelID() {
		return modelID;
	}

	public AggregationLevel getAggLevel() {
		return aggLevel;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ModelAggregationRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(modelID).
		append(aggLevel).
		toHashCode();
		return hash;
	}

}
