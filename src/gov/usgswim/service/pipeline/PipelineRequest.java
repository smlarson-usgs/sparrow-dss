package gov.usgswim.service.pipeline;

public interface PipelineRequest {
	
	public String getMimeType();
	
	public void setMimeType(String mimetype);
	
	public String getFileName();
	
	public String getXMLRequest();
	public void setXMLRequest(String request);

	public void setEcho(String echo);
	public void setEcho(boolean echo);
	public boolean isEcho();

}
