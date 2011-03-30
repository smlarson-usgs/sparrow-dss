package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * The upstream area of a reach in a model, including geometry.
 * 
 * @author eeverman
 */
@Immutable
@XStreamInclude({Geometry.class})
@XStreamAlias("ReachGeometry")
public class ReachGeometry implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Long id;
	private final Long modelId;
	private final Geometry geometry;
	private final Geometry simpleGeometry;
	private final Geometry convexGeometry;
	
	public ReachGeometry(Long reachId, Long modelId,
			Geometry geometry, Geometry simpleGeometry, Geometry convexGeometry) {
		this.id = reachId;
		this.modelId = modelId;
		this.geometry = geometry;
		this.simpleGeometry = simpleGeometry;
		this.convexGeometry = convexGeometry;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReachGeometry) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(id).append(modelId).append(geometry).toHashCode();
		
		return hash;
	}

	public Long getId() {
		return id;
	}

	public Long getModelId() {
		return modelId;
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
}
