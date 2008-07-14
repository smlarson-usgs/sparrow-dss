package gov.usgswim.sparrow.cachefactory;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for break-points in a set of data.
 * 
 * TODO:  Should include an enumeration to specify equal count or equal range.
 * @author eeverman
 *
 */
public class BinningRequest {
	private final Integer contextID;
	private final int binCount;
	
	public BinningRequest(Integer contextID, int binCount) {
		this.contextID = contextID;
		this.binCount = binCount;
		
	}

	public Integer getContextID() {
  	return contextID;
  }

	public Integer getBinCount() {
  	return binCount;
  }

	
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
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

		return hash.toHashCode();
	}
}
