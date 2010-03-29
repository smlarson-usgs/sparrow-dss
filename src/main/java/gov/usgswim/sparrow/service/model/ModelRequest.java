package gov.usgswim.sparrow.service.model;

import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.cachefactory.ModelRequestCacheKey;
import gov.usgswim.sparrow.parser.ResponseFormat;

import org.apache.commons.lang.BooleanUtils;

public class ModelRequest implements PipelineRequest{
	boolean _public = true;
	boolean _approved = true;
	boolean _archived = false;
	Long id;
	private String xmlRequest;
	private ResponseFormat responseFormat;

	public ModelRequest() {
		this.id = null;
	}

	public void setPublic(boolean p) {
		this._public = p;
	}

	public void setPublic(String p) {
		_public = BooleanUtils.toBoolean(p);
	}

	public boolean isPublic() {
		return _public;
	}

	public void setApproved(boolean approved) {
		this._approved = approved;
	}

	public void setApproved(String approved) {
		this._approved = BooleanUtils.toBoolean(approved);
	}

	public boolean isApproved() {
		return _approved;
	}

	public void setArchived(boolean archived) {
		this._archived = archived;
	}

	public void setArchived(String archived) {
		this._archived = BooleanUtils.toBoolean(archived);
	}

	public boolean isArchived() {
		return _archived;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public void setId(String id) {
		this.id = Long.parseLong(id);
	}

	public Long getId() {
		return this.id;
	}

	public String getXMLRequest() {
		return xmlRequest;
	}

	public void setXMLRequest(String request) {
		xmlRequest = request;		
	}

	public void setResponseFormat(ResponseFormat respFormat) {
		this.responseFormat = respFormat;
		responseFormat.fileName = "model";
	}
	
	public ResponseFormat getResponseFormat() {
		if (responseFormat == null) {
			setResponseFormat(new ResponseFormat());
		}
		return responseFormat;
	}
	
	public ModelRequestCacheKey toCacheKey() {
		ModelRequestCacheKey result = new ModelRequestCacheKey(id, _public, _approved, _archived);
		return result;
	}
}
