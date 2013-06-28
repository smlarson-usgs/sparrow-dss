package gov.usgswim.sparrow.service.idbypoint;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.usgswim.sparrow.action.LoadCalibrationSite;
import gov.usgswim.sparrow.domain.CalibrationSite;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.ServiceResponseOperation;
import gov.usgswim.sparrow.service.ServiceResponseStatus;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;

public class FindCalibrationSiteService extends AbstractSparrowServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doActualGet(HttpServletRequest req, HttpServletResponse resp) {
		doActualPost(req, resp);
	}

	public void doActualPost(HttpServletRequest req, HttpServletResponse resp) {
		Double lat = getDouble(req.getParameterMap(), "lat");
		Double lon = getDouble(req.getParameterMap(), "lon");
		Long modelId = getLong(req.getParameterMap(), "model_id");
		
		
		ServiceResponseWrapper out = new ServiceResponseWrapper(CalibrationSite.class, ServiceResponseOperation.GET);

		try {
			if (lat != null && lon != null && modelId != null) {
				LoadCalibrationSite action = new LoadCalibrationSite(lat, lon, modelId);
				CalibrationSite result = action.run();
				
	
				out.addEntity(result);
				out.setEntityId(result.getModelReachId());
				out.setStatus(ServiceResponseStatus.OK);
			} else {
				out.setStatus(ServiceResponseStatus.FAIL);
				out.setMessage("Numerical values for 'lat', 'lon' and 'model_id' are all required to call this service.");
			}
			
		} catch (Exception e) {
			out.setStatus(ServiceResponseStatus.FAIL);
			out.setError(e);
			log.error("Error trying to load calibration site by lat/long", e);
		}
		
		
		try {
			sendResponse(resp, out);
		} catch (IOException e) {
			log.error("Unable to send find calibration site service response.", e);
			e.printStackTrace();
		}
	}
}
