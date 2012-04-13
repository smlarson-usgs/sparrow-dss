package gov.usgswim.sparrow.domain;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import gov.usgswim.Immutable;

/**
 * A segment of geometry data for any type of entity (reach, catchment, huc...).
 * 
 * One piece of geometry may have multiple sections - separate islands that make
 * up one entity.  The Segment class is one 'island' of a geometry.
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
 * leaving this mutable.  If you use an instance, don't go muting it, OK?
 * @author eeverman
 */
@XStreamAlias("Segment")
public class Segment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@XStreamAlias("coords")
	@XStreamConverter(FloatArrayXStreamConverter.class)
	private final float[] coordinates;
	
	@XStreamAsAttribute
	private final boolean linear;
	
	public Segment(float[] coordinates, boolean linear) {
		this.coordinates = coordinates;
		this.linear = linear;
	}

	public float[] getCoordinates() {
		return coordinates;
	}

	/**
	 * True if the geometry is a series of points that make up a non-closed shape.
	 * @return
	 */
	public boolean isLinear() {
		return linear;
	}
	
	/**
	 * True if the geometry is a series of points that are a closed shape.
	 * @return
	 */
	public boolean isPolygon() {
		return ! linear;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Segment) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(coordinates).append(linear).toHashCode();
		
		return hash;
	}
	
}
