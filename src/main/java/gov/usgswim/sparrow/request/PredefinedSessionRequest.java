package gov.usgswim.sparrow.request;

import gov.usgswim.sparrow.domain.PredefinedSessionType;

import java.io.Serializable;

public class PredefinedSessionRequest implements Serializable {
	private final Long modelId;
	private final Boolean approved;
	private final PredefinedSessionType predefinedSessionType;
	private final String groupName;
	private final String uniqueCode;
	
	public PredefinedSessionRequest(Long modelId) {
		this.modelId = modelId;
		this.approved = null;
		this.predefinedSessionType = null;
		this.groupName = null;
		this.uniqueCode = null;
	}
	
	public PredefinedSessionRequest(String uniqueCode) {
		this.modelId = null;
		this.approved = null;
		this.predefinedSessionType = null;
		this.groupName = null;
		this.uniqueCode = uniqueCode;
	}
	
	public PredefinedSessionRequest(Long modelId, Boolean approved) {
		this.modelId = modelId;
		this.approved = approved;
		this.predefinedSessionType = null;
		this.groupName = null;
		this.uniqueCode = null;
	}
	
	public PredefinedSessionRequest(Long modelId, Boolean approved, PredefinedSessionType predefinedSessionType) {
		this.modelId = modelId;
		this.approved = approved;
		this.predefinedSessionType = predefinedSessionType;
		this.groupName = null;
		this.uniqueCode = null;
	}
	
	public PredefinedSessionRequest(Long modelId, Boolean approved, String groupName) {
		this.modelId = modelId;
		this.approved = approved;
		this.predefinedSessionType = null;
		this.groupName = groupName;
		this.uniqueCode = null;
	}
	
	public PredefinedSessionRequest(Long modelId, Boolean approved, PredefinedSessionType predefinedSessionType, String groupName) {
		this.modelId = modelId;
		this.approved = approved;
		this.predefinedSessionType = null;
		this.groupName = groupName;
		this.uniqueCode = null;
	}
	

	
	/**
	 * @return the modelId
	 */
	public Long getModelId() {
		return modelId;
	}
	/**
	 * @return the approved
	 */
	public Boolean getApproved() {
		return approved;
	}
	/**
	 * @return the predefinedSessionType
	 */
	public PredefinedSessionType getPredefinedSessionType() {
		return predefinedSessionType;
	}
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * @return the uniqueCode
	 */
	public String getUniqueCode() {
		return uniqueCode;
	}

}
