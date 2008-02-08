package gov.usgswim.sparrow.service;

import gov.usgswim.Immutable;
import gov.usgswim.service.pipeline.PipelineRequest;

import java.awt.Point;

import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * A request for reaches within a model nearest a point, sorted
 * by distance.  Although no bounds is set on the numberOfResults, its primary
 * usage is intended to be for identifying a reach near a point, so values over
 * 10 in areas w/ few reaches may return fewer results.
 */
@Immutable
public class IDByPointRequest implements PipelineRequest{
	private final Point.Double _point;
	private final int _numberOfResults;
	private final Long _modelId;
	
	private Integer hash;	//Not strictly threadsafe, but recalculation is cheap and non-destructive
	protected String mimetype = "xml"; // default is xml
	
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
	
	public boolean equals(Object object) {
		if (object instanceof IDByPointRequest) {
			return this.hashCode() == object.hashCode();
		} else {
			return false;
		}
	}

	public int hashCode() {
		//starts w/ some random numbers just to create unique results
		if (hash == null) {
			hash = new HashCodeBuilder(46517, 40116971).
				append(_modelId).
				append(_point).
				append(_numberOfResults).
				toHashCode();
		}
		
		return hash;
	}
	
	public String getMimeType() {
		return mimetype;
	}

	public void setMimeType(String mimetype) {
		if (mimetype != null) {
			this.mimetype = mimetype;	
		}	
	}
	
	public String getFileName() {
		return "idByPoint";
	}
}
