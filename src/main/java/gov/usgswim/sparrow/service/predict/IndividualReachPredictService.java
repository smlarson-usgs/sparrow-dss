package gov.usgswim.sparrow.service.predict;

import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndividualReachPredictService extends HttpServlet{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		String contextId = req.getParameter("context-id");
		String model = req.getParameter("model");
		String reachId = req.getParameter("reachID");
		IndividualReachPredictionRequest request = new IndividualReachPredictionRequest(contextId, model, reachId);
		
		PredictionContext context = request.retrievePredictionContext();
		PredictResult result = SharedApplication.getInstance().getPredictResult(context.getAdjustmentGroups());

		resp.setContentType("text/xml");
		PrintWriter writer = resp.getWriter();
		String responseHeadFormat = "<Sparrow-Individual-Reach-Prediction-Response reachId=\"%s\" model=\"%s\" contextId=\"%s\">";
		writer.write(String.format(responseHeadFormat, reachId, model, contextId));

		{
			int columns = result.getColumnCount();
			String resultFormat = "<result name=\"%s\">%s</result>";
			Integer row = result.getRowForId(request.reachId);
			if (row != null) {
				for (int col = 0; col < columns; col++) {
					String output = String.format(resultFormat, result.getName(col), result.getDouble(row, col));
					writer.write(output.toCharArray());
				}
			} else {
				writer.write("<error>No reach found with reachId=" + reachId + "</error>");
			}

		}

		writer.write("</Sparrow-Individual-Reach-Prediction-Response>");
		writer.flush();
	}
}
