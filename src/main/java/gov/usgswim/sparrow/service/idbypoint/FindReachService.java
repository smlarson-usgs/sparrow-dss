package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class FindReachService extends HttpServlet {


	public static String sampleResponse="<sparrow-reach-response xmlns=\"http://www.usgs.gov/sparrow/id-response-schema/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" model-id=\"22\">"
		+ "    <status>OK</status>"
		+ "   	<reach>"
		+ "      	<id>3541</id>"
		+ "        <name>WESTERN RUN</name>"
		+ "		<meanq>1234</meanq>"
		+ "		<state>WI</state>"
		+ "		"
		+ "		<cumulative-catch-area>2345</cumulative-catch-area>"
		+ "        <bbox min-long=\"-76.840216\" min-lat=\"39.492299\" max-long=\"-76.626801\" max-lat=\"39.597698\" marker-long=\"-76.7584575\" marker-lat=\"39.505502\" />"
		+ "        <hucs>"
		+ "            <huc8 id=\"02060003\" name=\"GUNPOWDER-PATAPSCO\" />"
		+ "            <huc6 id=\"020600\" name=\"UPPER CHESAPEAKE\" />"
		+ "            <huc4 id=\"0206\" name=\"UPPER CHESAPEAKE\" />"
		+ "            <huc2 id=\"02\" name=\"MID ATLANTIC\" />"
		+ "        </hucs>"
		+ "        "
		+ "        <!-- would contain attributes and predicted if specified-->"
		+ "		<!-- <attributes/> -->"
		+ "		<!-- <predicted/> -->"
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
		resp.setContentType("text/xml");

		// parse findReach request

		String inputXML = req.getParameter("xmlreq");
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		FindReachRequest frReq = new FindReachRequest();

		try {
			XMLStreamReader in = inFact.createXMLStreamReader(new StringReader(inputXML));
			in.next();
			frReq = frReq.parse(in);
		} catch (Exception e) {
			e.printStackTrace();
		}


		if (!cleanAndCheckValidityFindReachRequest(frReq)) {

		}


		String whereClause = createFindReachWhereClause(frReq);
		StringBuilder outputXML = new StringBuilder();
		try {
			Connection conn = SharedApplication.getInstance().getConnection();

			String sql = "Select model_reach_id, reach_name, meanq, catch_area, huc2, huc4, huc6, huc8 from model_attrib_vw "
				+ "where sparrow_model_id = " + frReq.modelID
				+ whereClause;
			System.out.println(sql);
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);

			outputXML.append("<sparrow-reach-response xmlns=\"http://www.usgs.gov/sparrow/id-response-schema/v0_2\" ");
			outputXML.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
			outputXML.append("model-id=\"" + frReq.modelID + "\">");
			outputXML.append("<status>OK</status>");

			while (rset.next()) {
				outputXML.append("<reach>");
				{
					outputXML.append("<id>" + rset.getString("MODEL_REACH_ID") + "</id>");
					outputXML.append("<name>" + rset.getString("REACH_NAME") + "</name>");
					outputXML.append("<meanq>" + rset.getString("MEANQ") + "</meanq>");
					//outputXML.append("<state>" + rset.getString("REACH_NAME") + "</state>");
					outputXML.append("<catch-area>" + rset.getString("CATCH_AREA") + "</catch-area>");
					outputXML.append("<hucs>");
					{
						outputXML.append("<huc8 id=\"" + rset.getString("HUC8") + "\" name=\"\" />");
						outputXML.append("<huc6 id=\"" + rset.getString("HUC6") + "\" name=\"\" />");
						outputXML.append("<huc4 id=\"" + rset.getString("HUC4") + "\" name=\"\" />");
						outputXML.append("<huc2 id=\"" + rset.getString("HUC2") + "\" name=\"\" />");
					}
					outputXML.append("</hucs>");
				}
				outputXML.append("</reach>");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		outputXML.append("</sparrow-reach-response>");
		ServletOutputStream out = resp.getOutputStream();
		out.print(outputXML.toString());
	}

	private boolean cleanAndCheckValidityFindReachRequest(FindReachRequest frReq) {
		frReq.basinAreaHi = trimToNull(frReq.basinAreaHi);
		frReq.basinAreaLo = trimToNull(frReq.basinAreaLo);
		frReq.meanQHi = trimToNull(frReq.meanQHi);
		frReq.meanQLo = trimToNull(frReq.meanQLo);
		frReq.reachName = trimToNull(frReq.reachName);
		frReq.huc = trimToNull(frReq.huc);
		if (frReq.basinAreaHi == null && frReq.basinAreaLo == null
				&& frReq.meanQHi == null && frReq.meanQLo == null
				&& frReq.reachName == null
				&& frReq.huc == null) {
			return false;
		}
		// TODO check for sql injection
		frReq.huc = cleanForSQLInjection(frReq.huc);
		if (frReq.huc != null) {
			frReq.huc = cleanForSQLInjection(frReq.huc);
		}
		return true;
	}

	public static String cleanForSQLInjection(String value) {
		// TODO fill this in
		return value;
	}

	public static String trimToNull(String value) {
		if (value == null) return null;
		value = value.trim();
		return (value.length() == 0)? null: value;
	}

	public String createFindReachWhereClause(FindReachRequest frReq) {
		String whereClause = "";
		if (frReq.reachName != null) {
			whereClause += " and UPPER(reach_name) like '%" + frReq.reachName.toUpperCase() + "%' ";
		}
		{ // basin area
			if (frReq.basinAreaHi != null) {
				whereClause += " and CATCH_AREA < " + frReq.basinAreaHi;
			}
			if (frReq.basinAreaLo != null) {
				whereClause += " and CATCH_AREA > " + frReq.basinAreaLo;
			}
		}
		{ // basin area
			if (frReq.meanQHi != null) {
				whereClause += " and MEANQ < " + frReq.meanQHi;
			}
			if (frReq.meanQLo != null) {
				whereClause += " and MEANQ > " + frReq.meanQLo;
			}
		}
		{	// hucs
			if (frReq.huc != null) {
				whereClause += " and HUC8 like '" + frReq.huc + "%'";
			}
		}
		whereClause += " and rownum < 10";
		return whereClause;
	}


}
