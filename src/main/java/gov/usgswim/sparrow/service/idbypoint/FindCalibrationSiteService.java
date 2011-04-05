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

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		doPost(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		Double lat = req.getParameter("lat")==null ? null : Double.valueOf((String)req.getParameter("lat"));
		Double lon = req.getParameter("lon")==null ? null : Double.valueOf((String)req.getParameter("lon"));
		
		try {
			LoadCalibrationSite action = new LoadCalibrationSite(lat, lon);
			CalibrationSite result = action.run();
			
			ServiceResponseWrapper out = new ServiceResponseWrapper(result, CalibrationSite.class, ((result!=null) ? result.getModelReachId() : null), ServiceResponseStatus.OK,
					ServiceResponseOperation.GET);
			sendResponse(resp, out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
