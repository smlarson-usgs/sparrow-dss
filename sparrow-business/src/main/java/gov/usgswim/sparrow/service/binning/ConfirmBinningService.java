package gov.usgswim.sparrow.service.binning;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.usgswim.sparrow.action.VerifyBinningResolution;
import gov.usgswim.sparrow.action.VerifyInclusiveBinning;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.SharedApplication;

//TODO this service returns two booleans, might want to make it return something more description
/**
 * service returns two booleans, first one indicates if all values are in at least one bin, second indicates if all fall into one bin when more than 2 bins are used
 */
public class ConfirmBinningService extends AbstractSparrowServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doActualGet(HttpServletRequest req, HttpServletResponse resp) {
		doActualPost(req, resp);
	}

	public void doActualPost(HttpServletRequest req, HttpServletResponse resp) {
		Integer contextId = req.getParameter("context-id")==null ? Integer.valueOf(0) : Integer.parseInt((String)req.getParameter("context-id"));
		String[] binHighList = req.getParameter("binHighList")==null ? new String[]{} : ((String)req.getParameter("binHighList")).split(",");
		String[] binLowList = req.getParameter("binLowList")==null ? new String[]{} : ((String)req.getParameter("binLowList")).split(",");
		
		try {
			SparrowColumnSpecifier data = SharedApplication.getInstance().getPredictionContext(contextId).getDataColumn();
			
			VerifyInclusiveBinning action = new VerifyInclusiveBinning(data, binHighList, binLowList);
			Boolean result = action.run();
			
			ServiceResponseWrapper out = new ServiceResponseWrapper(result, Boolean.class, null, ServiceResponseStatus.OK,
					ServiceResponseOperation.GET);
			
			VerifyBinningResolution action2 = new VerifyBinningResolution(data, binHighList, binLowList);
			Boolean result2 = action2.run();
			out.addEntity(result2);
			
			sendResponse(resp, out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
