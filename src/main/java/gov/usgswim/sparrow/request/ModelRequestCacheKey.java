package gov.usgswim.sparrow.request;

import gov.usgswim.Immutable;

import org.apache.commons.lang.builder.HashCodeBuilder;

@Immutable
public class ModelRequestCacheKey {
	
	private final boolean _public;
	private final boolean _approved;
	private final boolean _archived;
	private final Long _modelId;
	
	public ModelRequestCacheKey(Long modelId, boolean isPublic, boolean isApproved, boolean isArchived) {
		this._public = isPublic;
		this._approved = isApproved;
		this._archived = isArchived;
		this._modelId = modelId;
	}
	
	public boolean isPublic() {
		return this._public;
	}

	public boolean isApproved() {
		return this._approved;
	}

	public boolean isArchived() {
		return this._archived;
	}

	public Long getModelId() {
		return this._modelId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ModelRequestCacheKey) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder result = new HashCodeBuilder();
		
		result.append(isPublic());
		result.append(isApproved());
		result.append(isArchived());
		result.append(getModelId());
		
		return result.toHashCode();
	}
}
