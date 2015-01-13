package org.geoserver.sparrow.process;

/**
 *
 * @author eeverman
 */
public class SparrowStyleResponse {

	private String workspaceName;
	private String styleName;

	
	SparrowStyleResponse() {

	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getStyleName() {
		return styleName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

}
