package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import java.sql.Date;

/**
 * A PredictionContext that has been predefined for users to choose.
 * 
 * PredefinedPredictionContext's are stored in the db and then presented as
 * @author eeverman
 *
 */
@Immutable
public class PredefinedSession implements IPredefinedSession  {

	/**
	 * Database unique ID.
	 */
	private final Long id;

	/**
	 * User specified unique code.  This allows the user to specify a more
	 * memorable id that they could use in a paper or website.
	 */
	private final String uniqueCode;
	
	/**
	 * The model this pre-theme is for.
	 */
	private final Long modelId;
	
	/**
	 * A type, as specified by the enum.  This mostly specifies visibility.
	 */
	private final PredefinedSessionType predefinedSessionType;
	
	/**
	 * If approved, this pre-them is ready for use.  If not approved, this theme
	 * has yet to be approved by the administrator.
	 */
	private final Boolean approved;
	
	/**
	 * Name as it will be listed.
	 */
	private final String name;
	
	/**
	 * A description, possibly w/ some html.
	 */
	private final String description;
	
	/**
	 * A low to high sorting value within a model.
	 */
	private final Integer sortOrder;
	
	/**
	 * The actual text of the predefined theme.
	 */
	private final String contextString;
	
	/**
	 * The date-time the theme was added.
	 */
	private final Date addDate;
	
	/**
	 * The person who added the pre-theme.
	 */
	private final String addBy;
	
	/**
	 * A note by the creator to the approving admin.
	 */
	private final String addNote;
	
	/**
	 * Contact info for the person creating the theme.
	 */
	private final String addContactInfo;
	
	/**
	 * Custom categories can be created to allow themes to be grouped within
	 * a model.
	 */
	private final String groupName;
	
	public PredefinedSession(Long id, String uniqueCode, Long modelId,
			PredefinedSessionType predefinedSessionType, Boolean approved,
			String name, String description, Integer sortOrder,
			String contextString, Date addDate, String addBy, String addNote,
			String addContactInfo, String groupName) {
		
		this.id = id;
		this.uniqueCode = uniqueCode;
		this.modelId = modelId;
		this.predefinedSessionType = predefinedSessionType;
		this.approved = approved;
		this.name = name;
		this.description = description;
		this.sortOrder = sortOrder;
		this.contextString = contextString;
		this.addDate = addDate;
		this.addBy = addBy;
		this.addNote = addNote;
		this.addContactInfo = addContactInfo;
		this.groupName = groupName;
	}
	
	
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getId()
	 */
	public Long getId() {
		return id;
	}

	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getUniqueCode()
	 */
	public String getUniqueCode() {
		return uniqueCode;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getModelId()
	 */
	public Long getModelId() {
		return modelId;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getPredefinedSessionType()
	 */
	public PredefinedSessionType getPredefinedSessionType() {
		return predefinedSessionType;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getApproved()
	 */
	public Boolean getApproved() {
		return approved;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getSortOrder()
	 */
	public Integer getSortOrder() {
		return sortOrder;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getContextString()
	 */
	public String getContextString() {
		return contextString;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddDate()
	 */
	public Date getAddDate() {
		return addDate;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddBy()
	 */
	public String getAddBy() {
		return addBy;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddNote()
	 */
	public String getAddNote() {
		return addNote;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddContactInfo()
	 */
	public String getAddContactInfo() {
		return addContactInfo;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getGroupName()
	 */
	public String getGroupName() {
		return groupName;
	}

}
