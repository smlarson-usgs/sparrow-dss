package gov.usgswim.sparrow.service.binning;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.usgswim.sparrow.action.VerifyInclusiveBinning;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.ServiceResponseOperation;
import gov.usgswim.sparrow.service.ServiceResponseStatus;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.SharedApplication;

public class ConfirmBinningService extends AbstractSparrowServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		doPost(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		Integer contextId = req.getParameter("context-id")==null ? Integer.valueOf(0) : Integer.parseInt((String)req.getParameter("context-id"));
		String[] binHighList = req.getParameter("binHighList")==null ? new String[]{} : ((String)req.getParameter("binHighList")).split(",");
		String[] binLowList = req.getParameter("binLowList")==null ? new String[]{} : ((String)req.getParameter("binLowList")).split(",");
		
		try {
			SparrowColumnSpecifier data = SharedApplication.getInstance().getPredictionContext(contextId).getDataColumn();
			
			VerifyInclusiveBinning action = new VerifyInclusiveBinning(data, binHighList, binLowList);
			Boolean result = action.run();
			
			ServiceResponseWrapper out = new ServiceResponseWrapper(result, Boolean.class, null, ServiceResponseStatus.OK,
					ServiceResponseOperation.GET);
			sendResponse(resp, out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
