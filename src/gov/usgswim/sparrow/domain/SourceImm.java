package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import java.io.Serializable;

/**
 * Immutable implementation of Source, which is a single source (used by many reaches) for a SPARROW Model.
 */
@Immutable
public class SourceImm implements Source, Serializable {
	private static final long serialVersionUID = 1L;
	private final Long _id;
	private final int _identifier;
	private final String _name;
	private final String _displayName;
	private final String _description;
	private final int _sortOrder;
	private final Long _modelId;
	private final String _constituent;
	private final String _units;
	
	/*
	private SourceImm() {
		//some tools need a no-arg constructor.  Not really usable w/o reflection.
	}
	*/
	
	public SourceImm(Long id, int identifier, String name, String displayName,
			String description, int sortOrder, Long modelId, String constituent,
			String units) {
	
	
		_id = id;
		_identifier = identifier;
		_name = name;
		_displayName = displayName;
		_description = description;
		_sortOrder = sortOrder;
		_modelId = modelId;
		_constituent = constituent;
		_units = units;
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
}
