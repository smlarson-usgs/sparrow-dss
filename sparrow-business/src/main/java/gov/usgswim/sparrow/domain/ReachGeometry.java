package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
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
	
	@XStreamAsAttribute
	private final Long id;
	
	@XStreamAsAttribute
	private final Long modelId;
	
	private final Geometry basin;
	
	public ReachGeometry(Long reachId, Long modelId, Geometry basinGeometry) {
		this.id = reachId;
		this.modelId = modelId;
		this.basin = basinGeometry;
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
		append(id).append(modelId).append(basin).toHashCode();
		
		return hash;
	}

	public Long getId() {
		return id;
	}

	public Long getModelId() {
		return modelId;
	}

	public Geometry getBasin() {
		return basin;
	}

}
