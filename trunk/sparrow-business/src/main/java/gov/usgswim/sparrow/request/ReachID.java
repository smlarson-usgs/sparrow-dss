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

	private static final long serialVersionUID = 2L;
	private final Long modelID;
	private final Long reachID;
	private final int _hash;

	public ReachID(Long modelID, Long reachID) {
		
		if (modelID == null) throw new IllegalArgumentException("The ModelId cannot be null");
		if (reachID == null) throw new IllegalArgumentException("The reachlId cannot be null");
		
		this.modelID = modelID;
		this.reachID = reachID;
		this._hash = this.buildhashCode();
	}

	public Long getModelID() {
		return modelID;
	}

	public Long getReachID() {
		return reachID;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof ReachID) {
			ReachID otherRid = (ReachID) obj;
			if (otherRid.hashCode() == hashCode()) {
				if (otherRid.getModelID().equals(getModelID())) {
					isEqual = reachID.equals(otherRid.getReachID());
				} //else not equal
			}
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return this._hash;
	}

	private synchronized int buildhashCode() {
		int hash = new HashCodeBuilder(13, 161).
				append(modelID).
				append(reachID).
				toHashCode();
		return hash;
	}

	@Override
	public String toString() {
		String str = "ModelID: " + modelID + ", Reach ID: " + this.reachID;

		return str;
	}
}
