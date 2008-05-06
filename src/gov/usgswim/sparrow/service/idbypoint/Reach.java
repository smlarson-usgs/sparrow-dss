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
	
	public Reach(int id, String name, int distInMeters) {
		this.id = id;
		this.name = name;
		this.distInMeters = distInMeters;
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
	
	
}
