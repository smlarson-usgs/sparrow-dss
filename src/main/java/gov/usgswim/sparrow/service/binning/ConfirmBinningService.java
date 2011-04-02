package gov.usgswim.sparrow.service.binning;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.usgswim.sparrow.action.ConfirmBinning;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;

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
			ConfirmBinning action = new ConfirmBinning(contextId, binHighList, binLowList);
			String result = action.run();
			PrintWriter out = resp.getWriter();
			out.write("<confirm-bin-response><status>"+result+"</status><message></message></confirm-bin-response>");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
