package gov.usgswim.sparrow.cachefactory;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class ReachesByHUCRequest {
	private final String huc;
	private final long modelID;
	
	public ReachesByHUCRequest(String huc, long modelID) {
		this.huc = huc;
		this.modelID = modelID;
	}
	
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
  public boolean equals(Object obj) {
	  if (obj instanceof ReachesByHUCRequest) {
	  	return obj.hashCode() == hashCode();
	  } else {
	  	return false;
	  }
  }

	@Override
	public synchronized int hashCode() {

		HashCodeBuilder hash = new HashCodeBuilder(12497, 134343);
		hash.append(huc);
		hash.append(modelID);

		return hash.toHashCode();
	}

	public String getHuc() {
  	return huc;
  }

	public long getModelID() {
  	return modelID;
  }
	
	public int getHUCLevel() {
		return huc.length();
	}
}
