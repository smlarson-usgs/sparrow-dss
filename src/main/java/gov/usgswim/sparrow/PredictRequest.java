package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Encapsulates all required info for a prediction request.
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
public class PredictRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Long _modelId;
	private final AdjustmentSet _adjSet2;
	private final Integer hash;

	/**
	 * @param modelId
	 * @param adjSet
	 */
	public PredictRequest(Long modelId, AdjustmentSet adjSet2) {
		_modelId = modelId;

		if (adjSet2 != null) {
			_adjSet2 = adjSet2;
		} else {
			_adjSet2 = AdjustmentSet.EMPTY_ADJUSTMENTSET;
		}

		hash = hashCode();
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
		assert(_adjSet2 != null);
		return _adjSet2;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof PredictRequest) {
			return this.hashCode() == object.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hash != null) {
			return hash;
		} else {
			//only accessed via constructor
			int newHash = new HashCodeBuilder(97317, 1169401).
			append(_modelId).
			append(_adjSet2).
			toHashCode();
			return newHash;
		}
	}

}

