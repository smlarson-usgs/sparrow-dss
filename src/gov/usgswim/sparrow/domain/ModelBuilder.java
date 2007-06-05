package gov.usgswim.sparrow.domain;

import gov.usgswim.NotThreadSafe;
import gov.usgswim.sparrow.ImmutableBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
	protected List<Source> _sources;
	
	
	public ModelBuilder() {
	}
	
	public ModelBuilder(long id) {
		_id = id;
	}
	
	public Model getImmutable() throws IllegalStateException {
	
		List<Source> tmpList = null;
		
		//Need a list of immutable sources
		if (_sources != null) {
			tmpList = new ArrayList<Source>(_sources.size());
			
			for (int i = 0; i < _sources.size(); i++)  {
				Source s = _sources.get(i);
				if (s instanceof ImmutableBuilder) {
					s = ((ImmutableBuilder<Source>)s).getImmutable();
				}
				tmpList.add(s);
			}
		}
	
		return new ModelImm(
			_id, _approved, _public, _archived, _name, _description, _url,
			_dateAdded, _contactId, _enhNetworkId,
			_northBound, _eastBound, _southBound, _westBound, tmpList);
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

	/**
	 * This method does no checking for source ordering and is reserved for
	 * Hibernate use.
	 * 
	 * @param sources
	 */
	private void setSources(List<Source> sources) {
		_sources = sources;
	}
	
	/**
	 * Adds a source and ensures that the ordering of the set is correct.
	 * 
	 * @param s
	 */
	public void addSource(Source s) {
		if (_sources == null) {
			_sources = new ArrayList<Source>(7);
			_sources.add(s);
		} else {
		
			//insert source at proper index.  Assuming this is loaded from the db
			//in order, it will usually go at the end of the list.
			int sortIndex = s.getSortOrder();
			for (int i = _sources.size() - 1; i >= 0; i--)  {
				if (_sources.get(i).getSortOrder() < sortIndex) {
					_sources.add(i + 1, s);
					return;
				}
			}
			
			//Didn't find a place to insert, so the Source must belong at the start of the list
			_sources.add(0, s);

		}
				
	}
	
	public List<Source> getSources() {
		if (_sources != null) {
			return _sources;
		} else {
			return (List<Source>)Collections.EMPTY_LIST;
		}
	}
	
	public Source getSource(int identifier) {
		if (_sources != null) {
		
			for (int i = 0; i < _sources.size(); i++)  {
				Source s = _sources.get(i);
				if (s.getIdentifier() == identifier) return s;
			}
			
			return null;	//not found
		} else {
			return null;	//no sources
		}
	}
}
