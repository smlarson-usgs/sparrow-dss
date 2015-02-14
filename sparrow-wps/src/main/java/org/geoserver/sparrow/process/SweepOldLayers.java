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
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang.StringUtils;
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
	 * @param maxAgeMinutes Age in seconds allowed for a layer.  Layers older will be deleted.
	 * @param workspaces
	 * @param modelId
	 * @param nameRegex
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="Sweeps old layers", type=ServiceResponseWrapper.class)
	public ServiceResponseWrapper execute(
			@DescribeParameter(name="maxAgeMinutes", 
					description="Sweep layers that are older than the specified number of minutes.  If unspecified, a default value is used, approx 24 hours.", 
					min = 0, max = 1) Long maxAgeMinutes,
			@DescribeParameter(name="workspaces", 
					description="Optional.  One or more workspaces to sweep (separate w/ comma and/or spaces)  If unspecified, the standard 'non-reusable' workspaces will be swept.", 
					min = 0) String[] workspaces,
			@DescribeParameter(name="modelId", 
					description="Optional.  If specified, only datastores and layers of this model will be swept.  This param and nameRegex are mutually exclusive.  Combined w/ the workspace name (or the default non-reusable workspaces)", 
					min = 0, max = 1) Integer modelId,
			@DescribeParameter(name="nameRegex", 
					description="Optional.  If specified, only datastores and layers matching this regex will be swept.  This param and modelId are mutually exclusive.  Combined w/ the workspace name (or the default non-reusable workspaces)", 
					min = 0, max = 1) String nameRegex
		) throws Exception {
		
		//New wrapper
		ServiceResponseWrapper wrap = new ServiceResponseWrapper();
		wrap.setEntityClass(SweepResponse.class);
		wrap.setMimeType(ServiceResponseMimeType.XML);
		wrap.setOperation(ServiceResponseOperation.DELETE);
		wrap.setStatus(ServiceResponseStatus.OK);
		
		//state
		UserState state = new UserState();
		state.maxAgeMinutes = maxAgeMinutes;
		state.workspaces = workspaces;
		state.modelId = modelId;
		state.nameRegex = nameRegex;
		
		init(state, wrap);
		
		
		SweepResponse response = null;

		if (wrap.isOK()) {
			try {

				
				log.debug("Sweep request received for stores older than {} minutes in workspaces {}, modelId {}, regex matching {}",
						new Object[]{state.maxAgeMinutes, state.workspaces, state.modelId, state.nameRegex});
				
				if (state.modelId != null) {
					response = sweeper.runSweep(state.workspaces, state.modelId, state.maxAgeMs);
				} else if (state.nameRegex != null) {
					response = sweeper.runSweep(state.workspaces, state.nameRegex, state.maxAgeMs);
				} else {
					response = sweeper.runSweep(state.workspaces, (String)null, state.maxAgeMs);
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
		}

		if (wrap.isOK()) {
			log.debug("Request COMPLETE OK to sweep old layers");
		} else {
			log.error("FAILED to sweep old layers.  Message: " + wrap.getMessage());
			return wrap;
		}
		
		return wrap;
	}
	
		/**
	 * Initiate the self-initialized params from the user params and does validation.
	 * @param state
	 * @param wrap
	 */
	private void init(UserState state, ServiceResponseWrapper wrap) {
		
		//
		//Check credentials
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
			return;
		}
		
		
		//
		//Validation
		if (state.modelId != null && state.nameRegex != null) {
			wrap.setMessage("The parameters modelId and nameRegex cannot both be specified.");
			wrap.setStatus(ServiceResponseStatus.FAIL);
			return;
		}
		
		if (state.nameRegex != null) {
			try {
				Pattern p = Pattern.compile(state.nameRegex);
			} catch (PatternSyntaxException e) {
				wrap.setMessage("The nameRegex '" + state.nameRegex + "' is not a valid regex expression");
				wrap.setStatus(ServiceResponseStatus.FAIL);
				return;
			}
		}
		
		
		//
		//initiation
		if (state.workspaces != null) {
			if (state.workspaces.length == 0) {
				state.workspaces = null;
			} else {
				
				//parse workspaces, splitting on comma and space
				ArrayList<String> wks = new ArrayList(1);
				
				for (String s : state.workspaces) {
					String[] ss = StringUtils.split(s, ", ");
					
					for (String tt : ss) {
						wks.add(tt);
					}
				}
				
				state.workspaces = wks.toArray(new String[wks.size()]);
			}
		}
		
		if (state.maxAgeMinutes != null) {
			state.maxAgeMs = state.maxAgeMinutes * 60L * 1000L;
		}
	}
	
	/**
	 * The WPS is single instance, so a state class for each user request holds
	 * all info for a single execution.
	 */
	private class UserState {
		//User params, set for each invocation
		Long maxAgeMinutes;
		Long maxAgeMs;	//calculated
		String[] workspaces;
		Integer modelId;
		String nameRegex;
	}
	
	
}
