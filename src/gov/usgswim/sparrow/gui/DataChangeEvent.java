package gov.usgswim.sparrow.gui;

public class DataChangeEvent {
	Object source;
	String dataType;
	Object data;
	
	public DataChangeEvent(Object source, String dataType, Object data) {
		this.source = source;
		this.dataType = dataType;
		this.data = data;
	}

	public Object getSource() {
		return source;
	}

	public String getDataType() {
		return dataType;
	}

	public Object getData() {
		return data;
	}
}
