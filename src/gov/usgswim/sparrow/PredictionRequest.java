package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Encapsilates all required info for a prediction request.
 * 
 * Note that this class only contains the info needed to run a single prediction.
 * It does not contain any info about what portion of it should be returned to
 * the user.
 *
 * This class is intended to provide equality between two identical requests
 * so that multiple requests that equate to the same calculation are handled
 * together.
 *
 * This class is inteded to be immutable and will be cached.  To ensure
 * proper immutability, always use an immutable AdjustmetSet in the constructor.
 */
@Immutable
public class PredictionRequest {
	private final Long _modelId;
	private final AdjustmentSet _adjSet;
	private Integer hash;	//Not strictly threadsafe, but recalculation is cheap and non-destructive
	
	public PredictionRequest(Long modelId, AdjustmentSet adjSet) {
		_modelId = modelId;
		_adjSet = adjSet;
	}
	
	/**
	 * Returns the model ID (db id) associated w/ this request.
	 * @return
	 */
	public Long getModelId() {
		return _modelId;
	}
	
	/**
	 * Returns the AdjustmentSet associated w/ this request.
	 * @return
	 */
	public AdjustmentSet getAdjustmentSet() {
		return _adjSet;
	}

	public boolean equals(Object object) {
		if (object instanceof PredictionRequest) {
			PredictionRequest that = (PredictionRequest) object;
			return this.hashCode() == that.hashCode();
		} else {
			return false;
		}
	}

	public int hashCode() {
		//starts w/ some random numbers just to create unique results
		if (hash == null) {
			hash = new HashCodeBuilder(97317, 1169401).
				append(_modelId).
				append(_adjSet).
				toHashCode();
		}
		
		return hash;
	}

}
