package gov.usgswim.sparrow.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import gov.usgswim.ImmutableBuilder;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.sql.Date;

/**
 * Mutable PredictionContext that has been predefined for users to choose.
 * 
 * PredefinedPredictionContext's are stored in the db and then presented as
 * @author eeverman
 *
 */
public class PredefinedSessionBuilder implements XMLStreamParserComponent, IPredefinedSession, ImmutableBuilder<PredefinedSession> {

	/**
	 * Database unique ID.
	 */
	private Long id;

	/**
	 * User specified unique code.  This allows the user to specify a more
	 * memorable id that they could use in a paper or website.
	 */
	private String uniqueCode;
	
	/**
	 * The model this pre-theme is for.
	 */
	private Long modelId;
	
	/**
	 * A type, as specified by the enum.  This mostly specifies visibility.
	 */
	private PredefinedSessionType predefinedSessionType;
	
	/**
	 * If approved, this pre-them is ready for use.  If not approved, this theme
	 * has yet to be approved by the administrator.
	 */
	private Boolean approved;
	
	/**
	 * Name as it will be listed.
	 */
	private String name;
	
	/**
	 * A description, possibly w/ some html.
	 */
	private String description;
	
	/**
	 * A low to high sorting value within a model.
	 */
	private Integer sortOrder;
	
	/**
	 * The actual text of the predefined theme.
	 */
	private String contextString;
	
	/**
	 * The date-time the theme was added.
	 */
	private Date addDate;
	
	/**
	 * The person who added the pre-theme.
	 */
	private String addBy;
	
	/**
	 * A note by the creator to the approving admin.
	 */
	private String addNote;
	
	/**
	 * Contact info for the person creating the theme.
	 */
	private String addContactInfo;
	
	/**
	 * Custom categories can be created to allow themes to be grouped within
	 * a model.
	 */
	private String groupName;
	
	public PredefinedSessionBuilder() {
		
	}
	
	public PredefinedSessionBuilder(IPredefinedSession makeMeMutable) {
		this.id = makeMeMutable.getId();
		this.uniqueCode = makeMeMutable.getUniqueCode();
		this.modelId = makeMeMutable.getModelId();
		this.predefinedSessionType = makeMeMutable.getPredefinedSessionType();
		this.approved = makeMeMutable.getApproved();
		this.name = makeMeMutable.getName();
		this.description = makeMeMutable.getDescription();
		this.sortOrder = makeMeMutable.getSortOrder();
		this.contextString = makeMeMutable.getContextString();
		this.addDate = makeMeMutable.getAddDate();
		this.addBy = makeMeMutable.getAddBy();
		this.addNote = makeMeMutable.getAddNote();
		this.addContactInfo = makeMeMutable.getAddContactInfo();
		this.groupName = makeMeMutable.getGroupName();
	}

	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.parser.XMLStreamParserComponent#parse(javax.xml.stream.XMLStreamReader)
	 */
	@Override
	public XMLStreamParserComponent parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.parser.XMLStreamParserComponent#getParseTarget()
	 */
	@Override
	public String getParseTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.parser.XMLStreamParserComponent#isParseTarget(java.lang.String)
	 */
	@Override
	public boolean isParseTarget(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.parser.XMLStreamParserComponent#isValid()
	 */
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.parser.XMLStreamParserComponent#checkValidity()
	 */
	@Override
	public void checkValidity() throws XMLParseValidationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getId()
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getUniqueCode()
	 */
	public String getUniqueCode() {
		return uniqueCode;
	}

	/**
	 * @param uniqueCode the uniqueCode to set
	 */
	public void setUniqueCode(String uniqueCode) {
		this.uniqueCode = uniqueCode;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getModelId()
	 */
	public Long getModelId() {
		return modelId;
	}

	/**
	 * @param modelId the modelId to set
	 */
	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getPredefinedSessionType()
	 */
	public PredefinedSessionType getPredefinedSessionType() {
		return predefinedSessionType;
	}

	/**
	 * @param predefinedSessionType the predefinedSessionType to set
	 */
	public void setPredefinedSessionType(
			PredefinedSessionType predefinedSessionType) {
		this.predefinedSessionType = predefinedSessionType;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getApproved()
	 */
	public Boolean getApproved() {
		return approved;
	}

	/**
	 * @param approved the approved to set
	 */
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getSortOrder()
	 */
	public Integer getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getContextString()
	 */
	public String getContextString() {
		return contextString;
	}

	/**
	 * @param contextString the contextString to set
	 */
	public void setContextString(String contextString) {
		this.contextString = contextString;
	}


	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddDate()
	 */
	public Date getAddDate() {
		return addDate;
	}

	/**
	 * @param addDate the addDate to set
	 */
	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddBy()
	 */
	public String getAddBy() {
		return addBy;
	}

	/**
	 * @param addBy the addBy to set
	 */
	public void setAddBy(String addBy) {
		this.addBy = addBy;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddNote()
	 */
	public String getAddNote() {
		return addNote;
	}

	/**
	 * @param addNote the addNote to set
	 */
	public void setAddNote(String addNote) {
		this.addNote = addNote;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getAddContactInfo()
	 */
	public String getAddContactInfo() {
		return addContactInfo;
	}

	/**
	 * @param addContactInfo the addContactInfo to set
	 */
	public void setAddContactInfo(String addContactInfo) {
		this.addContactInfo = addContactInfo;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.domain.IPredefinedSession#getGroupName()
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param customCategory the customCategory to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public PredefinedSession toImmutable() throws IllegalStateException {
		PredefinedSession s = new PredefinedSession(id, uniqueCode, modelId,
			predefinedSessionType, approved, name, description, sortOrder,
			contextString, addDate, addBy, addNote, addContactInfo, groupName);
		return s;
	}

}
