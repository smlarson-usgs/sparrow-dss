package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;
import gov.usgs.cida.sparrow.service.util.ServiceResponseEntityList;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * A set HUC, including geometry.
 * 
 * @author eeverman
 */
@Immutable
@XStreamInclude({Geometry.class})
@XStreamAlias("HUC")
public class HUC implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String hucCode;
	private final String name;
	private final HucLevel hucType;
	private final Geometry geometry;
	private final Geometry simpleGeometry;
	private final Geometry convexGeometry;
	
	public HUC(String hucCode, String name, HucLevel hucType,
			Geometry geometry, Geometry simpleGeometry, Geometry convexGeometry) {
		this.hucCode = hucCode;
		this.name = name;
		this.hucType = hucType;
		this.geometry = geometry;
		this.simpleGeometry = simpleGeometry;
		this.convexGeometry = convexGeometry;
	}
	
	public String getHucCode() {
		return hucCode;
	}
	
	public String getName() {
		return name;
	}

	public HucLevel getHucType() {
		return hucType;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public Geometry getSimpleGeometry() {
		return simpleGeometry;
	}

	public Geometry getConvexGeometry() {
		return convexGeometry;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HUC) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(hucCode).append(hucType).append(geometry).toHashCode();
		
		return hash;
	}
}
