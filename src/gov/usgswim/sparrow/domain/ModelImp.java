package gov.usgswim.sparrow.domain;

import gov.usgswim.GuardedBy;
import gov.usgswim.Immutable;

import java.util.Date;

/**
 * Implementation of Model, which is a Domain Object representing a SPARROW Model.
 */


@Immutable
public class ModelImp implements Model {
	private final Long _id;
	private final boolean _approved;
	private final boolean _public;
	private final boolean _archived;
	private final String _name;
	private final String _description;
	private final String _url;
	private final Date _dateAdded;
	private final Long _contactId;
	private final Long _enhNetworkId;
	private final Double _northBound;
	private final Double _eastBound;
	private final Double _southBound;
	private final Double _westBound;

	
	public ModelImp(Long id, boolean approved, boolean isPublic, boolean archived,
				String name, String description, String url, Date dateAdded,
				Long contactId, Long enhNetworkId,
				Double northBound, Double eastBound, Double southBound, Double westBound) {
			
		_id = id;
		_approved = approved;
		_public = isPublic;
		_archived = archived;
		_name = name;
		_description = description;
		_url = url;
		_dateAdded = dateAdded;
		_contactId = contactId;
		_enhNetworkId = enhNetworkId;
		_northBound = northBound;
		_eastBound = eastBound;
		_southBound = southBound;
		_westBound = westBound;
	
	}

	public Long getId() {
		return _id;
	}

	public boolean isApproved() {
		return _approved;
	}

	public boolean isPublic() {
		return _public;
	}

	public boolean isArchived() {
		return _archived;
	}

	public String getName() {
		return _name;
	}

	public String getDescription() {
		return _description;
	}
	
	public String getUrl() {
		return _url;
	}

	public Date getDateAdded() {
		return _dateAdded;
	}

	public Long getContactId() {
		return _contactId;
	}

	public Long getEnhNetworkId() {
		return _enhNetworkId;
	}

	public Double getNorthBound() {
		return _northBound;
	}

	public Double getEastBound() {
		return _eastBound;
	}

	public Double getSouthBound() {
		return _southBound;
	}

	public Double getWestBound() {
		return _westBound;
	}
}
