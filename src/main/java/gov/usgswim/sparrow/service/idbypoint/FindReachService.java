package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.ServiceServlet;
import gov.usgswim.sparrow.action.FindReaches;
import gov.usgswim.sparrow.service.ReturnStatus;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * @author ilinkuo
 * TODO This service was not implemented via the Pipeline idiom like the others. Decide whether or not to maintain the idiom.
 */
public class FindReachService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String sampleResponse="<sparrow-reach-response xmlns=\"http://www.usgs.gov/sparrow/id-response-schema/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" model-id=\"22\">"
		+ "    <status>OK</status>"
		+ "    <status>message</status>"
		+ "   	<reach>"
		+ "      	<id>3541</id>"
		+ "        <name>WESTERN RUN</name>"
		+ "		<meanq>1234</meanq>"
		+ "		<state>WI</state>"
		+ "		<watershed-area>2345</watershed-area>"
		+ "        <bbox min-long=\"-76.840216\" min-lat=\"39.492299\" max-long=\"-76.626801\" max-lat=\"39.597698\" marker-long=\"-76.7584575\" marker-lat=\"39.505502\" />"
		+ "        <hucs>"
		+ "            <huc8 id=\"02060003\" name=\"GUNPOWDER-PATAPSCO\" />"
		+ "            <huc6 id=\"020600\" name=\"UPPER CHESAPEAKE\" />"
		+ "            <huc4 id=\"0206\" name=\"UPPER CHESAPEAKE\" />"
		+ "            <huc2 id=\"02\" name=\"MID ATLANTIC\" />"
		+ "        </hucs>"
		+ "   	</reach>"
		+ "</sparrow-reach-response>";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		resp.setContentType("application/xml");
		FindReachRequest frReq = parseRequest(req);
		StringBuilder outputXML = new StringBuilder();
		ReturnStatus status = ReturnStatus.ERROR;
		String message = "";
		DataTable result = null;

		FindReaches findReachesAction = new FindReaches();
		findReachesAction.setReachRequest(frReq);
		findReachesAction.setPageSize((req.getParameter("limit")==null) ? 50: Integer.valueOf(req.getParameter("limit")));
		findReachesAction.setRecordStart((req.getParameter("start")==null) ? 0: Integer.valueOf(req.getParameter("start")));
		findReachesAction.setSort(req.getParameter("sort"));
		findReachesAction.setSortDir(req.getParameter("dir"));
		
		int resultSize = 0;
		
		try {
			result = findReachesAction.run();
		} catch (Exception e) {
			message = "An error occured running your request: " + e.getMessage();
		}
		
		if (result != null) {
			if (result.getRowCount() > 0) {
				status = ReturnStatus.OK;
				resultSize = result.getInt(0, result.getColumnByName("TOTAL_COUNT"));
				for (int row = 0; row < result.getRowCount(); row++) {
					outputXML.append("<reach>");
					{
						outputXML.append("<id>" + result.getString(row, result.getColumnByName("FULL_IDENTIFIER")) + "</id>");
						outputXML.append("<name>" + result.getString(row, result.getColumnByName("REACH_NAME")) + "</name>");
						outputXML.append("<meanq>" + result.getString(row, result.getColumnByName("MEANQ")) + "</meanq>");
						//outputXML.append("<state>" + rset.getString("REACH_NAME") + "</state>");
						outputXML.append("<catch-area>" + result.getString(row, result.getColumnByName("CATCH_AREA")) + "</catch-area>");
						outputXML.append("<watershed-area>" + result.getString(row, result.getColumnByName("CUM_CATCH_AREA")) + "</watershed-area>");
						outputXML.append("<hucs>");
						{
							outputXML.append("<huc8 id=\"" + result.getString(row, result.getColumnByName("HUC8")) + "\" name=\"\" />");
							outputXML.append("<huc6 id=\"" + result.getString(row, result.getColumnByName("HUC6")) + "\" name=\"\" />");
							outputXML.append("<huc4 id=\"" + result.getString(row, result.getColumnByName("HUC4")) + "\" name=\"\" />");
							outputXML.append("<huc2 id=\"" + result.getString(row, result.getColumnByName("HUC2")) + "\" name=\"\" />");
						}
						outputXML.append("</hucs>");
					}
					outputXML.append("</reach>");
				}
			} else {
				status = ReturnStatus.OK_EMPTY;
				message = "Sorry, no reaches were found matching your criteria";
			}
			

		} else { // return error response
			status = ReturnStatus.ERROR;
			for (String error: findReachesAction.getErrors()) {
				message += "\n\r" + error;
			}
		}

		outputXML = getResponseXMLHeader(frReq.modelID, status, message, resultSize)
			.append(outputXML).append("</sparrow-reach-response>");
		ServletOutputStream out = resp.getOutputStream();
		out.print(outputXML.toString());
	}

	public static StringBuilder getResponseXMLHeader(String modelID, ReturnStatus status, String message, int resultSize) {
		StringBuilder result = new StringBuilder();
		result.append("<sparrow-reach-response xmlns=\"http://www.usgs.gov/sparrow/id-response-schema/v0_2\"");
		result.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		result.append("model-id=\"" + modelID + "\">");
		result.append("<success>"+((status == ReturnStatus.OK || status == ReturnStatus.OK_EMPTY || status == ReturnStatus.OK_PARTIAL) ? "true":"false") +"</success>");
		result.append("<results>"+resultSize+"</results>");
		result.append("<status>" + status + "</status>");
		result.append("<message>" + message + "</message>");
		return result;
	}


	public FindReachRequest parseRequest(HttpServletRequest req)
			throws FactoryConfigurationError {
		String inputXML = req.getParameter(ServiceServlet.DEFAULT_XML_PARAM_NAME);
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		FindReachRequest frReq = new FindReachRequest();

		try {
			XMLStreamReader in = inFact.createXMLStreamReader(new StringReader(inputXML));
			in.next();
			frReq = frReq.parse(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return frReq;
	}


}
