package gov.usgswim.sparrow.domain;

import java.util.Date;

/**
 * Domain Object representing a SPARROW Model.
 */
public interface Model {
	public Long getId();

	public boolean isApproved();

	public boolean isPublic();

	public boolean isArchived();

	public String getName();

	public String getDescription();

	public String getUrl();

	public Date getDateAdded();

	public Long getContactId();

	public Long getEnhNetworkId();
	
	public Double getNorthBound();

	public Double getEastBound();

	public Double getSouthBound();

	public Double getWestBound();
}
