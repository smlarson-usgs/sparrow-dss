package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple bean class to hold a unique reach identification based on model ID
 * and the full Identifier. This class serves as a key to cached Reach info
 *
 * @author eeverman
 */
@Immutable
public class ReachClientId implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Long modelID;
	private final String clientId;
	private final int _hash;

	public ReachClientId(Long modelID, String clientId) {
		
		if (modelID == null) throw new IllegalArgumentException("The modelId cannot be null");
		if (clientId == null) throw new IllegalArgumentException("The clientId cannot be null");
		
		this.modelID = modelID;
		this.clientId = clientId;
		this._hash = this.buildhashCode();
	}

	public String getReachClientId() {
		return clientId;
	}

	public Long getModelID() {
		return modelID;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof ReachClientId) {
			ReachClientId otherRid = (ReachClientId) obj;
			if (otherRid.hashCode() == hashCode()) {
				if (otherRid.getModelID().equals(getModelID())) {
					isEqual = clientId.equals(otherRid.getReachClientId());
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
				append(clientId).
				toHashCode();
		return hash;
	}

	@Override
	public String toString() {
		String str = "ModelID: " + modelID + ", Reach Client ID: " + this.clientId;
		return str;
	}
}
