package org.geoserver.sparrow.process;

import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;

import org.geoserver.wps.gs.GeoServerProcess;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType;
import org.geoserver.sparrow.util.GeoServerSparrowLayerSweeper;
import org.geoserver.sparrow.util.SweepResponse;
import org.geotools.process.factory.DescribeParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
		
/**
 * WPS to invoke the layer sweep process
 * 
 * @author eeverman
 */
@DescribeProcess(title="SweepOldLayers", version="1.0.0",
		description="WPS to invoke the layer sweep process.  NOTE:  To use from GeoServer WPS demo page, you must check the Authenticate option and login as admin.")
public class SweepOldLayers implements SparrowWps, GeoServerProcess {
	Logger log = LoggerFactory.getLogger(SweepOldLayers.class);
	
	//Set on construction
	private GeoServerSparrowLayerSweeper sweeper;
	

	
	public SweepOldLayers(GeoServerSparrowLayerSweeper sweeper) {
		this.sweeper = sweeper;
	}
	
	/**
	 * Requests that the layer be created if it does not already exist.
	 * 
	 * @param maxAgeSeconds Age in seconds allowed for a layer.  Layers older will be deleted.
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="Sweeps old layers", type=ServiceResponseWrapper.class)
	public ServiceResponseWrapper execute(
			@DescribeParameter(name="maxAgeSeconds", description="Sweep dynamic layers that are older than the specified number of seconds.", min = 0) Long maxAgeSeconds
		) throws Exception {
		
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper();
		wrap.setEntityClass(SweepResponse.class);
		wrap.setMimeType(ServiceResponseMimeType.XML);
		wrap.setOperation(ServiceResponseOperation.DELETE);
		wrap.setStatus(ServiceResponseStatus.OK);
		
		
		Object userObj = null;
		String userName = null;
		
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			userObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		
		if (userObj instanceof UserDetails) {
			UserDetails userDetails = (UserDetails)userObj;
			userName = userDetails.getUsername();
		} else {
			userName = userObj.toString();
		}
		
		if (userName == null || !(userName.equals("admin"))) {
			wrap.setStatus(ServiceResponseStatus.FAIL);
			wrap.setMessage("You are not logged in or are not the admin user.  " +
					"To use from GeoServer WPS demo page, you must check the Authenticate option and login as admin.");
			return wrap;
		}

		

		
		SweepResponse response = null;
		
	

		try {
			
			if (maxAgeSeconds != null) {
				log.debug("Request to sweep old layers received for layers older than " + (maxAgeSeconds * 1000L) + " seconds.");
				response = sweeper.runSweep(maxAgeSeconds * 1000L);
			} else {
				log.debug("Request to sweep old layers received for layers older than the default configured time.");
				response = sweeper.runSweep();
			}
			
			wrap.addEntity(response);
			
		} catch (Exception e) {
			//This is an unhandled error during create
			wrap.setStatus(ServiceResponseStatus.FAIL);
			wrap.setError(e);

			String msg = "FAILED:  An unexpected error happened while sweeping";
			wrap.setMessage(msg);
			log.error(msg, e);
			return wrap;
		}

		if (wrap.isOK()) {
			log.debug("Request COMPLETE OK to sweep old layers");
		} else {
			log.error("FAILED to sweep old layers");
			return wrap;
		}
		
		return wrap;
	}
	
	
}
