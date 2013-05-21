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

	private static final long serialVersionUID = 1L;
	
	private final Long modelID;
	private final Long reachID;
        private final String reachFullID;
        private final int hash;
        
	public ReachID(long modelID, long reachID) {
		this.modelID = modelID;
		this.reachID = reachID;
                this.reachFullID = null;
                this.hash = this.buildhashCode();
	}
        
        public ReachID(long modelID, String reachFullID) {
		this.modelID = modelID;
		this.reachID = null;
                this.reachFullID = reachFullID;
                this.hash = this.buildhashCode();
	}

        public String getReachFullID() {
                return reachFullID;
        }
	public long getModelID() {
		return modelID;
	}

	public long getReachID() {
		return reachID;
	}

	@Override
	public boolean equals(Object obj) {
                boolean isEqual = false;
		if (obj instanceof ReachID) {
                        ReachID otherRid = (ReachID)obj;
			if(obj.hashCode() == hashCode()){
                                isEqual = (
                                        otherRid.modelID == this.modelID &&
                                        otherRid.reachFullID.equals(this.reachFullID) &&
                                        otherRid.reachID == this.reachID
                                        );
                        }
		}
		return isEqual;
	}
	
	@Override
        public int hashCode() {
                return this.hash;
        }

	private synchronized int buildhashCode() {
		int hash = new HashCodeBuilder(13, 161).
		append(modelID).
		append(reachID).
                append(reachFullID).
		toHashCode();
		return hash;
	}

	@Override
	public String toString() {
		String str = "ModelID: " + modelID ;
                if(this.reachID == null){
                        str += ", Reach Full ID: " + this.reachFullID;
                }
                else{
                        str += ", Reach ID: " + this.reachID;
                }
                return str;
	}

}
