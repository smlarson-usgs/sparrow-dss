package gov.usgswim.sparrow.cachefactory;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for break-points in a set of data.
 * 
 * @author eeverman
 */
public class BinningRequest {
    
    /**
     * The types of bins that may be requested.  
     */
	public enum BIN_TYPE {
		EQUAL_COUNT,
		EQUAL_RANGE
	}

	/** Context id of the request. */
	private final Integer contextID;
	
	/** Number of bins. */
	private final int binCount;
	
	/** Type of bin. */
	private final BIN_TYPE binType;
	
	/**
	 * Constructs a new instance of {@code BinningRequest}.
	 * 
	 * @param contextID Unique identifier for the request.
	 * @param binCount Number of bins to divide the data into.
	 * @param binType Type of bins to divide the data into.
	 */
	public BinningRequest(Integer contextID, int binCount, BIN_TYPE binType) {
	    if (binCount < 1) {
	        throw new IllegalArgumentException("Bin count must be greater than 0.");
	    }
	    
		this.contextID = contextID;
		this.binCount = binCount;
		this.binType = binType;
	}

	public Integer getContextID() {
	    return contextID;
	}

	public Integer getBinCount() {
	    return binCount;
	}
	
	public BIN_TYPE getBinType() {
        return binType;
    }

	
	/**
	 * Consider two instances the same if they have the same calculated hashcodes.
	 */
	@Override
	public boolean equals(Object obj) {
        if (obj instanceof BinningRequest) {
            return obj.hashCode() == hashCode();
        } else {
            return false;
        }
    }

	@Override
	public synchronized int hashCode() {

		HashCodeBuilder hash = new HashCodeBuilder(197, 1343);
		hash.append(contextID);
		hash.append(binCount);
		hash.append(binType);

		return hash.toHashCode();
	}
}
