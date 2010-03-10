package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Immutable implementation of SparrowModel, which is a Domain Object representing a SPARROW SparrowModel.
 */
@Immutable
public class SparrowModelImm implements SparrowModel, Serializable {

	private static final long serialVersionUID = 2997975797864712530L;
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
	private final String _constituent;
	private final String _units;
	private final List<Source> _sources;
	private final Set<Entry<Object, Object>> _sessions;

	/*
	private SparrowModelImm() {
		//some tools need a no-arg constructor.  Not really usable w/o reflection.
	}
	*/

	/**
	 * Constructs an immutable SparrowModel instance.
	 *
	 * Note:  To be truly immutable, the Sources in the passed list must be
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
	 * @param constituent
	 * @param units
	 * @param sessions
	 * @param sources
	 */
	public SparrowModelImm(Long id, boolean approved, boolean isPublic, boolean archived,
				String name, String description, String url, Date dateAdded,
				Long contactId, Long enhNetworkId,
				Double northBound, Double eastBound, Double southBound, Double westBound,
				String constituent, String units,
				Set<Entry<Object, Object>> sessions, List<Source> sources) {

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
		_constituent = constituent;
		_units = units;
		_sessions = Collections.unmodifiableSet(sessions);

		//copy out the sources into an immutable list
		if (sources != null) {
			_sources = Collections.unmodifiableList(sources);
		} else {
			_sources = Collections.emptyList();
		}
	}

	public Long getId() {return _id;}

	@Override
	public boolean isApproved() {return _approved;}

	@Override
	public boolean isPublic() {return _public;}

	@Override
	public boolean isArchived() {return _archived;}

	@Override
	public String getName() {return _name;}

	@Override
	public String getDescription() {return _description;}

	@Override
	public String getUrl() {return _url;}

	@Override
	public Date getDateAdded() { return _dateAdded;}

	@Override
	public Long getContactId() { return _contactId;}

	@Override
	public Long getEnhNetworkId() { return _enhNetworkId;}

	@Override
	public Double getNorthBound() { return _northBound;}

	@Override
	public Double getEastBound() { return _eastBound;}

	@Override
	public Double getSouthBound() { return _southBound;}

	@Override
	public Double getWestBound() { return _westBound;}

	@Override
	public String getConstituent() { return _constituent;}
	
	@Override
	public String getUnits() { return _units;}

	@Override
	public Set<Entry<Object, Object>> getSessions() { return _sessions;}

	@Override
	public List<Source> getSources() { return _sources;}

	public Source getSource(int identifier) {
		if (_sources != null) {

			for (int i = 0; i < _sources.size(); i++)  {
				Source s = _sources.get(i);
				if (s.getIdentifier() == identifier) return s;
			}

			return null;	//not found
		}
		return null;	//no sources
	}
}
