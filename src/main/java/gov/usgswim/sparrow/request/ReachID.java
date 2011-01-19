package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple bean class to hold reach identification that was identified by a user
 * lat/long location. This class serves as a key to cached Reach.
 * 
 * @author eeverman
 * 
 */
@Immutable
public class ReachID implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long modelID;
	private final long reachID;

	public ReachID(long modelID, long reachID) {
		this.modelID = modelID;
		this.reachID = reachID;
	}

	public long getModelID() {
		return modelID;
	}

	public long getReachID() {
		return reachID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReachID) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(13, 161).
		append(modelID).
		append(reachID).
		toHashCode();
		return hash;
	}

}
