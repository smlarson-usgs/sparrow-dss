package gov.usgswim.sparrow.request;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.PredictionContext;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for one or more PredictionContexts.
 * 
 * This request can be one of three types, based on the constructor:
 * <ul>
 * <li>Fetch one PredictionContext from the db based on context ID
 * <li>Save the PC to the db
 * <li>Fetch all PC's from the db if their timestamp is more recent than the specified time.
 * @author eeverman
 *
 */
@Immutable
public class PredictionContextRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Long contextId;
	private final PredictionContext context;
	private final Long timeSince;
	
	public PredictionContextRequest(Long contextId) {
		this.contextId = contextId;
		this.context = null;
		this.timeSince = null;
	}
	
	/**
	 * Convienence construrtor b/c we are using Ints for context IDs.
	 * @param contextId
	 */
	public PredictionContextRequest(Integer contextId) {
		this.contextId = new Long(contextId);
		this.context = null;
		this.timeSince = null;
	}
	
	public PredictionContextRequest(PredictionContext context) {
		this.contextId = null;
		this.context = context;
		this.timeSince = null;
	}
	
	public PredictionContextRequest(Date timeSince) {
		this.contextId = null;
		this.context = null;
		this.timeSince = timeSince.getTime();
	}

	

	/**
	 * @return the contextId
	 */
	public Long getContextId() {
		return contextId;
	}

	/**
	 * @return the context
	 */
	public PredictionContext getContext() {
		return context;
	}

	/**
	 * Returns the time, as specified by java.util.Date.getTime(), for which
	 * records newer than this time should be returned.
	 * @return the timeSince
	 */
	public Long getTimeSince() {
		return timeSince;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PredictionContextRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(197, 1343);
		hash.append(contextId);
		hash.append(context);
		hash.append(timeSince);
		return hash.toHashCode();
	}

}
