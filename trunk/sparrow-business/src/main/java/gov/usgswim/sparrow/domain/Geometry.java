package gov.usgswim.sparrow.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * A set of geometry data for any type of entity (reach, catchment, huc...).
 * This class does calcs for some basic values of the geom including the
 * bounding box and center point.
 * 
 * This class uses all floats, not doubles.  One quick check of HUC 01 shows
 * that using floats instead of doubles results in a max variation of 5X10^-7
 * (max difference b/t the double value and the float value).  Thats close
 * enough for drawing in SVG.
 * 
 * Immutability:  Unfortunately, this class is not fully immutable b/c it allows
 * access to the ordinates array.  To be immutable, this class would either need
 * to copy on get of the array or use a wrapped or auto-boxed list (which can
 * be read-only).  Since all of these have performance or memory downsides, I'm
 * leaving this mutable.  If you use a Geometry instance, don't go muting it, OK?
 * @author eeverman
 */
@XStreamAlias("Geometry")
@XStreamInclude({Segment.class})
public class Geometry implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//Derivative data
	private Float minLong;
	private Float minLat;
	private Float maxLong;
	private Float maxLat;
	private Float centerLong;
	private Float centerLat;
	
	//Actual raw data
	@XStreamAlias("Segments")
	private final Segment[] segments;
	
	//Flag to mark if the transient state is initialized
	private transient boolean transStateInit = false;
	
	
	
	public Geometry(Segment[] segments) {
		this.segments = segments;
		
		initTransientState();
	}
	
	public Geometry(Segment segments) {
		if (segments != null) {
			this.segments = new Segment[] {segments};
		} else {
			this.segments = null;
		}
		
		initTransientState();
	}
	
	/**
	 * Init the transient state
	 */
	public synchronized void initTransientState() {
		if (!transStateInit) {
			
			boolean found = false;	//only set true if we found a point
			
			if (segments != null && segments.length > 0) {
				
				minLong = Float.POSITIVE_INFINITY;
				minLat = Float.POSITIVE_INFINITY;
				maxLong = Float.NEGATIVE_INFINITY;
				maxLat = Float.NEGATIVE_INFINITY;
				
				
				//Construct the bounding box from min/max values
				for (int s = 0; s < segments.length; s++) {
					
					float[] ordinates = segments[s].getCoordinates();
					
					if (ordinates != null && ordinates.length > 0) {
						found = true;
						
						for (int i = 0; i < ordinates.length; i+=2) {
							float x = ordinates[i];
							float y = ordinates[i + 1];
							
							if (x > maxLong) maxLong = x;
							if (x < minLong) minLong = x;
		
							if (y > maxLat) maxLat = y;
							if (y < minLat) minLat = y;					
							
						}
					}
				
				}
				
				centerLong = (minLong + maxLong) / 2f;
				centerLat = (minLat + maxLat) / 2f;
			}
			
			if (!found) {
				minLong = null;
				minLat = null;
				maxLong = null;
				maxLat = null;
				centerLong = null;
				centerLat = null;
			}
			
			transStateInit = true;
		}
	}

	public Segment[] getSegments() {
		return segments;
	}

	public float getMinLong() {
		if (!transStateInit) initTransientState();
		
		return minLong;
	}

	public float getMinLat() {
		if (!transStateInit) initTransientState();
		
		return minLat;
	}

	public float getMaxLong() {
		if (!transStateInit) initTransientState();
		
		return maxLong;
	}

	public float getMaxLat() {
		if (!transStateInit) initTransientState();
		
		return maxLat;
	}

	public float getCenterLong() {
		if (!transStateInit) initTransientState();
		
		return centerLong;
	}

	public float getCenterLat() {
		if (!transStateInit) initTransientState();
		
		return centerLat;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Geometry) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(segments).toHashCode();
		
		return hash;
	}
	
	private Object readResolve() {
		initTransientState();
		return this;
  }
}
