/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoserver.sparrow.process;

/**
 *
 * @author eeverman
 */
public class SparrowDataLayerResponse {

	private String endpointUrl;
	private String flowLayerName;
	private String catchLayerName;
	
	SparrowDataLayerResponse() {

	}

	/**
	 * @return the endpointUrl
	 */
	public String getEndpointUrl() {
		return endpointUrl;
	}

	/**
	 * @param endpointUrl the endpointUrl to set
	 */
	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	/**
	 * @return the flowLayerName
	 */
	public String getFlowLayerName() {
		return flowLayerName;
	}

	/**
	 * @param flowLayerName the flowLayerName to set
	 */
	public void setFlowLayerName(String flowLayerName) {
		this.flowLayerName = flowLayerName;
	}

	/**
	 * @return the catchLayerName
	 */
	public String getCatchLayerName() {
		return catchLayerName;
	}

	/**
	 * @param catchLayerName the catchLayerName to set
	 */
	public void setCatchLayerName(String catchLayerName) {
		this.catchLayerName = catchLayerName;
	}
	
}
