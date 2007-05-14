package gov.usgswim.sparrow.domain;

import java.util.Date;

/**
 * Implementation of Model, which is a Domain Object representing a SPARROW Model.
 */
public class ModelImp implements Model {
	protected long _id;
	protected boolean _approved;
	protected boolean _public;
	protected boolean _archived;
	protected String _name;
	protected String _description;
	protected String _url;
	protected Date _dateAdded;
	protected long _contactId;
	protected long _enhNetworkId;
	
	
	public ModelImp(long id) {
		this._id = id;
	}

	public long getId() {
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

	public void setContactId(long contactId) {
		this._contactId = contactId;
	}

	public long getContactId() {
		return _contactId;
	}

	public void setEnhNetworkId(long enhNetworkId) {
		this._enhNetworkId = enhNetworkId;
	}

	public long getEnhNetworkId() {
		return _enhNetworkId;
	}
}
