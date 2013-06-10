package gov.usgswim.sparrow.calculation.request;


import gov.usgs.cida.sparrow.calculation.Area;
import gov.usgswim.Immutable;
import gov.usgswim.sparrow.request.ReachID;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request used by CalcFractionedWatershedArea to calculate the watershed area
 * for a single reach.
 *
 * @author eeverman
 */
@Immutable
public class AreaRequest {

	private static final long serialVersionUID = 1L;
	private final Area area;
	private final ReachID reachID;

	public AreaRequest(
			ReachID reachID,
			Area area){
		this.reachID = reachID;
		this.area = area;
	}


	/**
	 * Creates the related ReachAreaFractionMapRequest.
	 * This creates a new instance so that the equals and hashcode are not overridden
	 * for that instance.
	 * @return
	 */
	public AreaRequest buildReachAreaFractionMapRequest() {
		return new AreaRequest(reachID, area);
	}

	/**
	 * Clones all the options for this instance into a new instance w/ the specified reachID.
	 * @param reachId
	 * @return
	 */
	public AreaRequest cloneForReachId(ReachID newReachId) {
		return new AreaRequest(newReachId,
				area);
	}

	@Override
	public boolean equals(Object obj) {
		Boolean objectsAreEqual = false;
		if (obj instanceof AreaRequest) {
			AreaRequest otherReq = (AreaRequest)obj;
			if(obj.hashCode() == hashCode()){
				objectsAreEqual = otherReq.area == this.area &&
					otherReq.reachID.equals(this.reachID);
			}
		}
		return objectsAreEqual;
	}

	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(24457, 1433).
		append(reachID).
		append(area).
		toHashCode();
		return hash;
	}

}
