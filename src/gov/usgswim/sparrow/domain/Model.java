package gov.usgswim.sparrow.domain;

import java.util.Date;

public interface Model {
	public long getId();

	public boolean isApproved();

	public boolean isPublic();

	public boolean isArchived();

	public String getName();

	public String getDescription();

	public String getUrl();

	public Date getDateAdded();

	public long getContactId();

	public long getEnhNetworkId();
}
