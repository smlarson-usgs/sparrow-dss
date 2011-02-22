package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.service.ServiceResponseEntityList;

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
	private final HUCType hucType;
	private final Geometry geometry;
	
	public HUC(String hucCode, String name, HUCType hucType, Geometry geometry) {
		this.hucCode = hucCode;
		this.name = name;
		this.hucType = hucType;
		this.geometry = geometry;
	}
	
	public String getHucCode() {
		return hucCode;
	}
	
	public String getName() {
		return name;
	}

	public HUCType getHucType() {
		return hucType;
	}

	public Geometry getGeometry() {
		return geometry;
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
