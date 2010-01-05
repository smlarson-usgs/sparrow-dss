package gov.usgswim.sparrow.action;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for loading reach-huc associations
 *
 * @author eeverman
 */
public class LoadReachHucsRequest implements Serializable {

	private static final long serialVersionUID = 1L;


	/** Model id of the request. */
	private final Long modelId;

	private final Integer hucLevel;

	/**
	 * Constructs a new instance of {@code BinningRequest}.
	 *
	 * @param contextID Unique identifier for the request.
	 * @param binCount Number of bins to divide the data into.
	 * @param binType Type of bins to divide the data into.
	 */
	public LoadReachHucsRequest(Long modelId, Integer hucLevel) {
	    if (hucLevel.equals(2) || hucLevel.equals(4) || hucLevel.equals(6) || hucLevel.equals(8)) {
			this.modelId = modelId;
			this.hucLevel = hucLevel;
	        
	    } else {
	    	throw new IllegalArgumentException("Huc level must be 2, 4, 6, or 8");
	    }


	}

	public Long getModelID() {
	    return modelId;
	}

	public Integer getHucLevel() {
	    return hucLevel;
	}


	/**
	 * Consider two instances the same if they have the same calculated hashcodes.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LoadReachHucsRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {

		HashCodeBuilder hash = new HashCodeBuilder(197, 1343);
		hash.append(modelId);
		hash.append(hucLevel);

		return hash.toHashCode();
	}
}
