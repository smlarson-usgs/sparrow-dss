package gov.usgswim.sparrow.service.idbypoint;

import java.awt.Point;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gov.usgswim.Immutable;

/**
 * Simple bean class to hold a reach that was identified by a user lat/long location.
 * @author eeverman
 *
 */
@Immutable
public class ModelPoint {
	private final long modelID;
	private final Point.Double point;
	
	public ModelPoint(long modelID, Point.Double point) {
		this.modelID = modelID;
		this.point = point;
	}

	public long getModelID() {
  	return modelID;
  }

	public Point.Double getPoint() {
  	return point;
  }
	
  @Override
public boolean equals(Object obj) {
	  if (obj instanceof ModelPoint) {
	  	return obj.hashCode() == hashCode();
	  }
	  return false;
  }
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(17, 193).
		append(modelID).
		append(point.x).
		append(point.y).
		toHashCode();
		return hash;
	}
	
}
