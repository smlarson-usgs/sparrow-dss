package gov.usgswim.sparrow.domain;

import gov.usgswim.NotThreadSafe;
import gov.usgswim.sparrow.ImmutableBuilder;
import java.util.Date;

/**
 * Builder implementation of Model, which is a Domain Object representing a SPARROW Model.
 * 
 * This class can be used to construct a model instance in a single thread,
 * which can then be copied to an immutable instance via getImmutable()
 */
@NotThreadSafe
public class ModelBuilder implements Model, ImmutableBuilder<Model> {
	protected Long _id;
	protected boolean _approved;
	protected boolean _public;
	protected boolean _archived;
	protected String _name;
	protected String _description;
	protected String _url;
	protected Date _dateAdded;
	protected Long _contactId;
	protected Long _enhNetworkId;
	protected Double _northBound;
	protected Double _eastBound;
	protected Double _southBound;
	protected Double _westBound;
	
	
	public ModelBuilder() {
	}
	
	public ModelBuilder(long id) {
		_id = id;
	}
	
	public Model getImmutable() throws IllegalStateException {
		return new ModelImm(
			_id, _approved, _public, _archived, _name, _description, _url,
			_dateAdded, _contactId, _enhNetworkId,
			_northBound, _eastBound, _southBound, _westBound);
	}

	public void setId(Long id) {
		_id = id;
	}
	public Long getId() {
		return _id;
	}

	public void setApproved(boolean approved) {
		this._approved = approved;
	}

	public boolean isApproved() {
		return _approved;
	}

	public void setPublic(boolean p) {
		this._public = p;
	}

	public boolean isPublic() {
		return _public;
	}

	public void setArchived(boolean archived) {
		this._archived = archived;
	}

	public boolean isArchived() {
		return _archived;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getName() {
		return _name;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	public String getDescription() {
		return _description;
	}

	public void setUrl(String url) {
		this._url = url;
	}

	public String getUrl() {
		return _url;
	}

	public void setDateAdded(Date dateAdded) {
		this._dateAdded = dateAdded;
	}

	public Date getDateAdded() {
		return _dateAdded;
	}

	public void setContactId(Long contactId) {
		this._contactId = contactId;
	}

	public Long getContactId() {
		return _contactId;
	}

	public void setEnhNetworkId(Long enhNetworkId) {
		this._enhNetworkId = enhNetworkId;
	}

	public Long getEnhNetworkId() {
		return _enhNetworkId;
	}

	public void setNorthBound(Double northBound) {
		this._northBound = northBound;
	}

	public Double getNorthBound() {
		return _northBound;
	}

	public void setEastBound(Double eastBound) {
		this._eastBound = eastBound;
	}

	public Double getEastBound() {
		return _eastBound;
	}

	public void setSouthBound(Double southBound) {
		this._southBound = southBound;
	}

	public Double getSouthBound() {
		return _southBound;
	}

	public void setWestBound(Double westBound) {
		this._westBound = westBound;
	}

	public Double getWestBound() {
		return _westBound;
	}
}
