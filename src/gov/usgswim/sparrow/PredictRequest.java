package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Encapsilates all required info for a prediction request.
 * 
 * This class contains the ID of the model to run, adjustment information,
 * and the type of prediction to run.  It does not contain the actual model
 * data (@see PredictData).
 * 
 * Also, this class does not contain any info about what portion of the results
 * to present, how the results should be filtered, or what
 * additional metadata should be returned to the user
 * (@see gov.usgswim.service.PredictServiceRequest).
 *
 * This class provides .equals equality between two identical requests
 * so that multiple requests that equate to the same calculation are handled
 * together.
 *
 * This class is immutable and will be cached.  However, to ensure
 * proper immutability, always use an immutable AdjustmetSet in the constructor.
 */
@Immutable
public class PredictRequest {
	private final Long _modelId;
	private final AdjustmentSet _adjSet;
	private Integer hash;	//Not strictly threadsafe, but recalculation is cheap and non-destructive
	
	public PredictRequest(Long modelId, AdjustmentSet adjSet) {
		_modelId = modelId;
		
		if (adjSet != null) {
			_adjSet = adjSet;
		} else {
			_adjSet = AdjustmentSet.EMPTY_ADJUSTMENTSET;
		}
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
	 * 
	 * This method will never return null - it will just return an empty
	 * AdjustmentSet if there are no adjustments.
	 * @return
	 */
	public AdjustmentSet getAdjustmentSet() {
		return _adjSet;
	}

	public boolean equals(Object object) {
		if (object instanceof PredictRequest) {
			return this.hashCode() == object.hashCode();
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
