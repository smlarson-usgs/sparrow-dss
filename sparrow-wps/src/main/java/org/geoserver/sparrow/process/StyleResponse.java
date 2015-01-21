package org.geoserver.sparrow.process;

/**
 *
 * @author eeverman
 */
public class StyleResponse {

	private String workspaceName;
	private String styleName;

	
	StyleResponse() {

	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	/**
	 * The style name w/o workspace info
	 * @return 
	 */
	public String getStyleName() {
		return styleName;
	}

	/**
	 * The style name w/o workspace info
	 * @param styleName 
	 */
	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

}
