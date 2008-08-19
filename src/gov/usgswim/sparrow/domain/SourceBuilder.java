package gov.usgswim.sparrow.domain;

import gov.usgswim.ImmutableBuilder;
import gov.usgswim.NotThreadSafe;

/**
 * Builder implementation of Source, which is a domain object representing a
 * source from a SPARROW Model.
 * 
 * This class can be used to construct a model instance in a single thread,
 * which can then be copied to an immutable instance via getImmutable()
 */
@NotThreadSafe
public class SourceBuilder implements Source, ImmutableBuilder<Source> {
	private Long _id;
	private int _identifier;
	private String _name;
	private String _displayName;
	private String _description;
	private int _sortOrder;
	private Long _modelId;
	private String _constituent;
	private String _units;	
	
	public SourceBuilder() {
	}
	
	public SourceBuilder(long id) {
		_id = id;
	}
	
	public Source toImmutable() throws IllegalStateException {
		return new SourceImm(_id, _identifier, _name, _displayName,
			_description, _sortOrder, _modelId, _constituent, _units);
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
	
	public String getConstituent() {
	    return _constituent;
	}
	
	public String getUnits() {
	    return _units;
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
	
	public void setConstituent(String constituent) {
	    this._constituent = constituent;
	}
	
	public void setUnits(String units) {
	    this._units = units;
	}
}
