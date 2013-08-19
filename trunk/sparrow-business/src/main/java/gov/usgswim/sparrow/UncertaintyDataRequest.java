package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Encapsulates all required info to load a series of uncertainty data.
 *
 * An UncertaintyDataRequest is passed to the UncertaintyDataFactory which
 * loads the data into an UncertaintyData instance.  The UncertaintyData can
 * be used with a PredictionResult from the same model to generate an
 * UncertaintyResult.
 *
 * This class provides .equals equality between two identical
 * requests so that multiple requests that equate to the same data set are
 * handled together.
 *
 * This class is immutable and is intended to be used as a cache hash key.
 */
@Immutable
public class UncertaintyDataRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long _modelId;
	private final UncertaintySeries _uncertaintySeries;
	private final Integer _sourceId;
	private final Integer hash;


	/**
	 * Creates a new request with all required data.
	 *
	 * Note that the sourceNumber is the zero based source index as defined in
	 * PredictData.getSourceIdForSourceIndex().
	 *
	 * @param modelId
	 * @param uncertaintySeries
	 * @param sourceId
	 */
	public UncertaintyDataRequest(Long modelId, UncertaintySeries uncertaintySeries, Integer sourceId) {
		_modelId = modelId;
		_uncertaintySeries = uncertaintySeries;
		_sourceId = sourceId;

		if (_uncertaintySeries.isSourceSpecific()) {
			if (_sourceId == null || _sourceId < 0) {
				throw new IllegalArgumentException(
					"A source number is required for source specific uncertainty series.");
			}
		} else {
			if (_sourceId != null) {
				throw new IllegalArgumentException(
					"A source number is not allowed for Total type uncertainty series.");
			}
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
	 * Returns the UncertaintySeries to be loaded.
	 * Source specific series are incomplete without a source specification.
	 *
	 * @return The UncertaintySeries to be loaded.
	 */
	public UncertaintySeries getUncertaintySeries() {
		return _uncertaintySeries;
	}

	/**
	 * The source for which we want uncertainty data.
	 *
	 * This only applies to UncertaintySeries that are source specific.
	 * Note that the sourceId is a per-model id for the source.  Typically
	 * models will number sources 1-x as defined in
	 * PredictData.getSourceIdForSourceIndex().
	 *
	 * @return The specified source id.
	 */
	public Integer getSourceId() {
		return _sourceId;
	}


	@Override
	public boolean equals(Object object) {
		if (object instanceof UncertaintyDataRequest) {
			return this.hashCode() == object.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hash != null) {
			return hash;
		}
		//Only accessed from the constructor
		int newHash = new HashCodeBuilder(97317, 1169401).
		append(_modelId).
		append(_uncertaintySeries).
		append(_sourceId).
		toHashCode();
		return newHash;

	}

}

