package gov.usgswim.sparrow.request;


import gov.usgswim.Immutable;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request used by CalcFractionedWatershedArea to calculate the watershed area
 * for a single reach.
 * 
 * @author eeverman
 */
@Immutable
public class FractionedWatershedAreaRequest extends ReachAreaFractionMapRequest {
	
	/** Instance w/ all default values and a null reach */
	public static final FractionedWatershedAreaRequest DEFAULT_NULL_INSTANCE = new FractionedWatershedAreaRequest(null);

	private static final long serialVersionUID = 1L;
	
	/** If true, ignore the fraction and just add up the area.  Mostly for debugging. */
	private final boolean forceNonFractionedResult;

	public FractionedWatershedAreaRequest(
			ReachID reachId,
			boolean forceUncorrectedFracValues, 
			boolean forceIgnoreIfTran, 
			boolean forceNonFractionedResult) {
		
		super(reachId, forceUncorrectedFracValues, forceIgnoreIfTran);
		this.forceNonFractionedResult = forceNonFractionedResult;
	}
	
	/**
	 * All default flag values.
	 * 
	 * @param reachId 
	 */
	public FractionedWatershedAreaRequest(ReachID reachId) {
		super(reachId, false, false);
		this.forceNonFractionedResult = false;
	}
	
	public boolean isForceNonFractionedResult() {
		return forceNonFractionedResult;
	}

	/**
	 * Creates the related ReachAreaFractionMapRequest.
	 * This creates a new instance so that the equals and hashcode are not overridden
	 * for that instance.
	 * @return 
	 */
	public ReachAreaFractionMapRequest buildReachAreaFractionMapRequest() {
		return new ReachAreaFractionMapRequest(getReachId(), isForceUncorrectedFracValues(), isForceIgnoreIfTran());
	}
	
	/**
	 * Clones all the options for this instance into a new instance w/ the specified reachID.
	 * @param reachId
	 * @return 
	 */
	public FractionedWatershedAreaRequest cloneForReachId(ReachID newReachId) {
		return new FractionedWatershedAreaRequest(newReachId, 
				isForceUncorrectedFracValues(), isForceIgnoreIfTran(), forceNonFractionedResult);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FractionedWatershedAreaRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(24457, 1433).
		append(super.hashCode()).
		append(forceNonFractionedResult).
		toHashCode();
		return hash;
	}

}
