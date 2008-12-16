package gov.usgswim.sparrow.cachefactory;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gov.usgswim.Immutable;

/**
 * Simple bean class to hold reach identification that was identified by a user
 * lat/long location. This class serves as a key to cached Reach.
 * 
 * @author eeverman
 * 
 */
@Immutable
public class ReachID {
	private final long modelID;
	private final int reachID;

	public ReachID(long modelID, int reachID) {
		this.modelID = modelID;
		this.reachID = reachID;
	}

	public long getModelID() {
		return modelID;
	}

	public int getReachID() {
		return reachID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReachID) {
			return obj.hashCode() == hashCode();
		} else {
			return false;
		}
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
