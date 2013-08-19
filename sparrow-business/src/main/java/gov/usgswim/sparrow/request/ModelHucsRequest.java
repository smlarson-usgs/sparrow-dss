package gov.usgswim.sparrow.request;

import gov.usgswim.sparrow.domain.HucLevel;
import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for loading reach-huc associations
 *
 * @author eeverman
 */
public class ModelHucsRequest implements Serializable {

	private static final long serialVersionUID = 2L;


	/** Model id of the request. */
	private final Long modelId;

	private final HucLevel hucLevel;

	/**
	 * Constructs a new instance of {@code BinningRequest}.
	 *
	 * @param contextID Unique identifier for the request.
	 * @param binCount Number of bins to divide the data into.
	 * @param binType Type of bins to divide the data into.
	 */
	public ModelHucsRequest(Long modelId, HucLevel hucLevel) {

			this.modelId = modelId;
			this.hucLevel = hucLevel;

	}

	public Long getModelID() {
	    return modelId;
	}

	public HucLevel getHucLevel() {
	    return hucLevel;
	}


	/**
	 * Consider two instances the same if they have the same calculated hashcodes.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ModelHucsRequest) {
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
