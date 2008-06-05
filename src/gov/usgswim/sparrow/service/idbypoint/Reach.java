package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.Immutable;

/**
 * Simple bean class to hold a reach that was identified by a user lat/long location.
 * @author eeverman
 *
 */
@Immutable
public class Reach {
	private final int id;
	private final String name;
	private final int distInMeters;
	
	private final double minLong;
	private final double minLat;
	private final double maxLong;
	private final double maxLat;
	
	public Reach(int id, String name, int distInMeters,
			double minLong, double minLat, double maxLong, double maxLat) {
		this.id = id;
		this.name = name;
		this.distInMeters = distInMeters;
		this.minLong = minLong;
		this.minLat = minLat;
		this.maxLong = maxLong;
		this.maxLat = maxLat;
	}

	public int getId() {
  	return id;
  }

	public int getDistanceInMeters() {
  	return distInMeters;
  }

	public String getName() {
  	return name;
  }

	public double getMinLong() {
  	return minLong;
  }

	public double getMinLat() {
  	return minLat;
  }

	public double getMaxLong() {
  	return maxLong;
  }

	public double getMaxLat() {
  	return maxLat;
  }
	
	
}
