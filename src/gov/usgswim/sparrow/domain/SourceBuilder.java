package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import gov.usgswim.NotThreadSafe;

import gov.usgswim.sparrow.ImmutableBuilder;

import java.io.Serializable;

/**
 * Builder implementation of Model, which is a Domain Object representing a SPARROW Model.
 * 
 * This class can be used to construct a model instance in a single thread,
 * which can then be copied to an immutable instance via getImmutable()
 */
@NotThreadSafe
public class SourceBuilder implements Source, ImmutableBuilder {
	private Long _id;
	private int _identifier;
	private String _name;
	private String _displayName;
	private String _description;
	private int _sortOrder;
	private Long _modelId;
	
	
	public SourceBuilder() {
	}
	
	public SourceBuilder(long id) {
		_id = id;
	}
	
	public <Source>Object getImmutable() throws IllegalStateException {
		return new SourceImm(_id, _identifier, _name, _displayName,
			_description, _sortOrder, _modelId);
	}

	public Long getId() {
		return _id;
	}

	public int getIdentifier() {
		return _identifier;
	}

	public String getName() {
		return _name;
	}

	public String getDisplayName() {
		return _displayName;
	}

	public String getDescription() {
		return _description;
	}

	public int getSortOrder() {
		return _sortOrder;
	}

	public Long getModelId() {
		return _modelId;
	}

	public void setId(Long id) {
		this._id = id;
	}

	public void setIdentifier(int identifier) {
		this._identifier = identifier;
	}

	public void setName(String name) {
		this._name = name;
	}

	public void setDisplayName(String displayName) {
		this._displayName = displayName;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	public void setSortOrder(int sortOrder) {
		this._sortOrder = sortOrder;
	}

	public void setModelId(Long modelId) {
		this._modelId = modelId;
	}
}
