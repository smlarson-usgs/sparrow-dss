package gov.usgswim.sparrow.request;

import java.io.Serializable;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for a ReachAreaFractionMap.
 * 
 * @author eeverman
 */
@Immutable
public class ReachAreaFractionMapRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final ReachID reachId;
	private final boolean forceUncorrectedFracValues;
	private final boolean forceIgnoreIfTran;

	public ReachAreaFractionMapRequest(ReachID reachId, boolean forceUncorrectedFracValues, boolean forceIgnoreIfTran) {
		this.reachId = reachId;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
		this.forceIgnoreIfTran = forceIgnoreIfTran;
	}
	
	public ReachAreaFractionMapRequest(ReachID reachId) {
		this.reachId = reachId;
		this.forceUncorrectedFracValues = true;
		this.forceIgnoreIfTran = false;
	}

	public ReachID getReachId() {
		return reachId;
	}

	public boolean isForceUncorrectedFracValues() {
		return forceUncorrectedFracValues;
	}

	public boolean isForceIgnoreIfTran() {
		return forceIgnoreIfTran;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReachAreaFractionMapRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(reachId).
		append(forceUncorrectedFracValues).
		append(forceIgnoreIfTran).
		toHashCode();
		return hash;
	}

}
