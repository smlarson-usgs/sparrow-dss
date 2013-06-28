package gov.usgswim.sparrow.domain;

import java.io.Serializable;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple bean class to hold a mapping of a system reach id to a client id.
 *
 * @author eeverman
 */
@Immutable
public class ReachFullId implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Long modelId;
	private final String clientId;
	private final Long reachId;
	private final int _hash;

	public ReachFullId(Long modelID, Long reachId, String clientId) {
		
		if (modelID == null) throw new IllegalArgumentException("The modelId cannot be null");
		if (reachId == null) throw new IllegalArgumentException("The reachId cannot be null");
		if (clientId == null) throw new IllegalArgumentException("The clientId cannot be null");
		
		
		this.modelId = modelID;
		this.reachId = reachId;
		this.clientId = clientId;
		this._hash = this.buildhashCode();
	}

	public String getReachClientId() {
		return clientId;
	}
	
	public Long getReachId() {
		return reachId;
	}

	public Long getModelId() {
		return modelId;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof ReachFullId) {
			ReachFullId otherRid = (ReachFullId) obj;
			if (otherRid.hashCode() == hashCode()) {
				isEqual =
						modelId.equals(otherRid.getModelId()) &&
						clientId.equals(otherRid.getReachClientId()) &&
						reachId.equals(otherRid.getReachId());
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
				append(modelId).
				append(clientId).
				append(reachId).
				toHashCode();
		return hash;
	}

	@Override
	public String toString() {
		String str = "ModelID: " + modelId + ", Reach Client ID: " + this.clientId + ", Reach ID: " + reachId;
		return str;
	}
}
