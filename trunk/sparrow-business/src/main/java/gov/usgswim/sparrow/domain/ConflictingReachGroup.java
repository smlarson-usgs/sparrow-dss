package gov.usgswim.sparrow.domain;

public class ConflictingReachGroup {
	private String type;
	private String value;
	private String groupName;
	
	public ConflictingReachGroup (String type, String groupName, String value){
		this.type = type;
		this.groupName = groupName;
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	
}
