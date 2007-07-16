package gov.usgswim.sparrow.service;

import gov.usgswim.Immutable;

import java.awt.Point;


/**
 * A request for reaches within a model nearest a point, sorted
 * by distance.  Although no bounds is set on the numberOfResults, its primary
 * usage is intended to be for identifying a reach near a point, so values over
 * 10 in areas w/ few reaches may return fewer results.
 */
@Immutable
public class IDByPointRequest {
	private final Point.Double _point;
	private final int _numberOfResults;
	private final Long _modelId;
	
	/**
	 * Constructs a new request instance.
	 * 
	 * Values less then 1 for numberOfResults are converted to 1.
	 * @param modelId
	 * @param point
	 * @param numberOfResults
	 */
	public IDByPointRequest(Long modelId, Point.Double point, int numberOfResults) {
		_modelId = modelId;
		_point = point;
		_numberOfResults = (numberOfResults < 1)?1:numberOfResults;
	}

	public int getNumberOfResults() {
		return _numberOfResults;
	}

	public Long getModelId() {
		return _modelId;
	}
	
	public Point.Double getPoint() {
		return _point;
	}
}
