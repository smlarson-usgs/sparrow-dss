package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Immutable implementation of Model, which is a Domain Object representing a SPARROW Model.
 */
@Immutable
public class ModelImm implements Model, Serializable {
	private static final long serialVersionUID = 4501741342624409074L;
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
	private final List<Source> _sources;

	/*
	private ModelImm() {
		//some tools need a no-arg constructor.  Not really usable w/o reflection.
	}
	*/
	
	/**
	 * Constructs an immutable Model instance.
	 * 
	 * Note:  To be truely immmutable, the Sources in the passed list must be
	 * immutable - this is the caller's responsibility.  The passed list does
	 * not need to be immutable b/c the values will be copied out.
	 * 
	 * @param id
	 * @param approved
	 * @param isPublic
	 * @param archived
	 * @param name
	 * @param description
	 * @param url
	 * @param dateAdded
	 * @param contactId
	 * @param enhNetworkId
	 * @param northBound
	 * @param eastBound
	 * @param southBound
	 * @param westBound
	 * @param sources
	 */
	public ModelImm(Long id, boolean approved, boolean isPublic, boolean archived,
				String name, String description, String url, Date dateAdded,
				Long contactId, Long enhNetworkId,
				Double northBound, Double eastBound, Double southBound, Double westBound,
				List<Source> sources) {
			
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
		
		//copy out the sources into an immutable list
		if (sources != null) {
			ArrayList<Source> list = new ArrayList<Source>(sources);
			_sources = Collections.unmodifiableList(list);
		} else {
			_sources = (List<Source>)Collections.EMPTY_LIST;
		}
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
	
	public List<Source> getSources() {
		return _sources;
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
