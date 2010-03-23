package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.service.ServiceServlet;
import gov.usgswim.sparrow.cachefactory.AbstractCacheFactory;
import gov.usgswim.sparrow.service.ReturnStatus;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
public class FindReachSupportService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String sampleResponse="<sparrow-reach-response xmlns=\"http://www.usgs.gov/sparrow/id-response-schema/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" model-id=\"22\">"
		+ "    <status>OK</status>"
		+ "   	<reach>"
		+ "      	<id>3541</id>"
		+ "        <name>WESTERN RUN</name>"
		+ "		<meanq>1234</meanq>"
		+ "		<state>WI</state>"
		+ "		<cumulative-catch-area>2345</cumulative-catch-area>"
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

	public static final String EDANAME_QUERY = "SELECT  distinct EDANAME FROM MODEL_ATTRIB_VW  WHERE SPARROW_MODEL_ID=%s and EDANAME is not null";
	public static final String EDACODE_QUERY = "SELECT  distinct EDACODE FROM MODEL_ATTRIB_VW  WHERE SPARROW_MODEL_ID=%s and EDACODE is not null";
	public static final Map<String, String> QUERIES = new HashMap<String, String>();
	{	// populate the queries map
		QUERIES.put("name", EDANAME_QUERY);
		QUERIES.put("code", EDACODE_QUERY);
	}
	protected void getEdaAttribs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/xml");
		String modelID = req.getParameter("model");
		String attrib = req.getParameter("get");
		StringBuilder outputXML = new StringBuilder();
		ReturnStatus status = ReturnStatus.ERROR;
		String message = "";
		String rootElement = "eda" + attrib + "s-response";
		try {
			// TODO need to put this in properties file using getText per conventions
			String edanameQuery = String.format(QUERIES.get(attrib), modelID);
			Connection conn = SharedApplication.getInstance().getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(edanameQuery);

			boolean hasFoundResults = false;
			String openTag = "<" + attrib + ">";
			String closeTag = "</" + attrib + ">";
			while (rset.next()) {
				hasFoundResults = true;
				outputXML.append(openTag).append(rset.getString(1)).append(closeTag);
			}
			if (hasFoundResults) {
				status = ReturnStatus.OK;
			} else {
				status = ReturnStatus.ERROR;
				message = "No eda " + attrib + "s found for model " + modelID;
			}
		} catch (SQLException e) {
			status = ReturnStatus.ERROR;
			message = e.getMessage();
			outputXML = new StringBuilder(); // clear the output
			e.printStackTrace();
		}
		outputXML = getResponseXMLHeader(rootElement, modelID, status, message).append(outputXML);
		outputXML.append("</").append(rootElement).append(">");
		ServletOutputStream out = resp.getOutputStream();
		out.print(outputXML.toString());
	}

//	protected void getEdaNames(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//		resp.setContentType("text/xml");
//		String modelID = req.getParameter("model");
//		StringBuilder outputXML = new StringBuilder();
//		String status = "error";
//
//		outputXML.append("<edanames-response>");
//		try {
//			// TODO need to put this in properties file using getText per conventions
//			String edanameQuery = String.format(EDANAME_QUERY, modelID);
//			Connection conn = SharedApplication.getInstance().getConnection();
//			Statement stmt = conn.createStatement();
//			ResultSet rset = stmt.executeQuery(edanameQuery);
//			boolean hasFoundResults = false;
//			while (rset.next()) {
//				hasFoundResults = true;
//				outputXML.append("<name>").append(rset.getString(1)).append("</name>");
//			}
//			if (hasFoundResults) {
//				status = "OK";
//			} else {
//				status = "ERROR: SORRY. No edanames found for model " + modelID;
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		outputXML = getResponseXMLHeader(modelID, status).append(outputXML).append("</edanames-response>");
//		ServletOutputStream out = resp.getOutputStream();
//		out.print(outputXML.toString());
//	}
//
//	protected void getEdaCodes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//		resp.setContentType("text/xml");
//		String modelID = req.getParameter("model");
//		StringBuilder outputXML = new StringBuilder();
//		String status = "error";
//
//		outputXML.append("<edacodes-response>");
//		try {
//			// TODO need to put this in properties file using getText per conventions
//			String edacodeQuery = String.format(EDACODE_QUERY, modelID);
//			Connection conn = SharedApplication.getInstance().getConnection();
//			Statement stmt = conn.createStatement();
//			ResultSet rset = stmt.executeQuery(edacodeQuery);
//			boolean hasFoundResults = false;
//			while (rset.next()) {
//				hasFoundResults = true;
//				outputXML.append("<code>").append(rset.getString(1)).append("</code>");
//			}
//			if (hasFoundResults) {
//				status = "OK";
//			} else {
//				status = "ERROR: SORRY. No edacodes found for model " + modelID;
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		outputXML = getResponseXMLHeader(modelID, status).append(outputXML).append("</edacodes-response>");
//		ServletOutputStream out = resp.getOutputStream();
//		out.print(outputXML.toString());
//	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		getEdaAttribs(req, resp);
//		resp.setContentType("text/xml");
//		FindReachRequest frReq = parseRequest(req);
//		StringBuilder outputXML = new StringBuilder();
//		String status = "error";
//
//		List<String> errors = cleanAndCheckValidityFindReachRequest(frReq);
//		if (errors.size() == 0) {
//			String whereClause = createFindReachWhereClause(frReq);
//
//			try {
//				Connection conn = SharedApplication.getInstance().getConnection();
//
//				String sql = "Select full_identifier, reach_name, meanq, catch_area, huc2, huc4, huc6, huc8 from model_attrib_vw a "
//					+ ((frReq.boundingBox == null)? "": "join model_geom_vw g on a.sparrow_model_id = g.sparrow_model_id and a.identifier = g.identifier ")
//					+ "where a.sparrow_model_id = " + frReq.modelID
//					+ whereClause
//					+ " order by reach_name";
//				//System.out.println(sql);
//				Statement stmt = conn.createStatement();
//				ResultSet rset = stmt.executeQuery(sql);
//
//				boolean hasFoundResults = false;
//				while (rset.next()) {
//					hasFoundResults = true;
//					outputXML.append("<reach>");
//
//					{
//						outputXML.append("<id>" + rset.getString("FULL_IDENTIFIER") + "</id>");
//						outputXML.append("<name>" + rset.getString("REACH_NAME") + "</name>");
//						outputXML.append("<meanq>" + rset.getString("MEANQ") + "</meanq>");
//						//outputXML.append("<state>" + rset.getString("REACH_NAME") + "</state>");
//						outputXML.append("<catch-area>" + rset.getString("CATCH_AREA") + "</catch-area>");
//						outputXML.append("<hucs>");
//						{
//							outputXML.append("<huc8 id=\"" + rset.getString("HUC8") + "\" name=\"\" />");
//							outputXML.append("<huc6 id=\"" + rset.getString("HUC6") + "\" name=\"\" />");
//							outputXML.append("<huc4 id=\"" + rset.getString("HUC4") + "\" name=\"\" />");
//							outputXML.append("<huc2 id=\"" + rset.getString("HUC2") + "\" name=\"\" />");
//						}
//						outputXML.append("</hucs>");
//					}
//					outputXML.append("</reach>");
//				}
//				if (hasFoundResults) {
//					status = "OK";
//				} else {
//					status = "ERROR: SORRY. No reaches were found matching your criteria";
//				}
//			} catch (SQLException e) {
//				status = "ERROR: " + e.getMessage();
//				outputXML = new StringBuilder(); // clear the output
//				e.printStackTrace();
//			}
//		} else { // return error response
//			status = "ERROR";
//			for (String error: errors) {
//				status += ";" + error;
//			}
//		}
//
//		outputXML = getResponseXMLHeader(frReq.modelID, status).append(outputXML).append("</sparrow-reach-response>");
//		ServletOutputStream out = resp.getOutputStream();
//		out.print(outputXML.toString());
	}


	public static void getEDACodes(String modelID) {

	}

	public static void getEDANames(String modelID) {

	}

	public static final String RESPONSE_HEADER = "<%s model-id=\"%s\"><status>%s</status><message>%s</message>";
	public static StringBuilder getResponseXMLHeader(String rootElement,
			String modelID, ReturnStatus status, String message) {
		
		StringBuilder result = new StringBuilder();
		result.append(String.format(RESPONSE_HEADER, rootElement, modelID, status, message));
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

	public static Pattern hucRegEx = Pattern.compile("[0-9]+");
	public static List<String> cleanAndCheckValidityFindReachRequest(FindReachRequest frReq) {
		List<String> errors = new ArrayList<String>();
		{ // trim each field
			frReq.reachIDs = trimToNull(frReq.reachIDs);
			frReq.basinAreaHi = trimToNull(frReq.basinAreaHi);
			frReq.basinAreaLo = trimToNull(frReq.basinAreaLo);
			frReq.meanQHi = trimToNull(frReq.meanQHi);
			frReq.meanQLo = trimToNull(frReq.meanQLo);
			frReq.reachName = trimToNull(frReq.reachName);
			frReq.huc = trimToNull(frReq.huc);
			frReq.boundingBox = trimToNull(frReq.boundingBox);
			frReq.edaCode = trimToNull(frReq.edaCode);
			frReq.edaName = trimToNull(frReq.edaName);
		}

		{	// clean each field
			frReq.reachIDs = cleanForSQLInjection(frReq.reachIDs);
			frReq.reachName = cleanForSQLInjection(frReq.reachName);
			frReq.huc = cleanForSQLInjection(frReq.huc);
			if (frReq.huc != null) {
				frReq.huc = cleanForSQLInjection(frReq.huc);
				if (!hucRegEx.matcher(frReq.huc).matches()) errors.add("<hucErr>" + frReq.huc + " is not a valid beginning of a huc</hucErr>");
			}
			frReq.basinAreaHi = cleanFloat(frReq.basinAreaHi, "catch area high", "catchAreaHi", errors);
			frReq.basinAreaLo = cleanFloat(frReq.basinAreaLo, "catch area low", "basinAreaLo", errors);
			frReq.meanQHi = cleanFloat(frReq.meanQHi, "mean flow high", "meanQHi", errors);
			frReq.meanQLo = cleanFloat(frReq.meanQLo, "mean flow low", "meanQLo", errors);
			if (frReq.isEmptyRequest()) {
				errors.add("at least one search parameter is required");
				return errors;
			}
			frReq.edaCode = cleanForSQLInjection(frReq.edaCode);
			frReq.edaName = cleanForSQLInjection(frReq.edaName);

		}

		errors = checkHiLo(errors, frReq.basinAreaHi, frReq.basinAreaLo, "catch area");
		errors = checkHiLo(errors, frReq.meanQHi, frReq.meanQLo, "catch area");
		// TODO check bbox
		return errors;
	}

	public static List<String> checkHiLo(List<String> errors, String hiValue,
			String loValue, String valueName) {
		if (hiValue == null || loValue == null) return errors;
		float hi = Float.parseFloat(hiValue);
		float lo = Float.parseFloat(loValue);
		if (hi < lo){
			errors.add(valueName + " high " + hiValue + " is less than " + valueName + " low " + loValue);
		}
		return errors;
	}

	private static String cleanFloat(String value, String name, String tag, List<String> errors) {
		if (value != null) {
			value = cleanForSQLInjection(value);
			try {
				Float.parseFloat(value);
			} catch (NumberFormatException e) {
				String.format("<%s>%s is not a valid number for %s</%s>", tag, value, name, tag);
				errors.add(String.format("<%s>%s is not a valid number for %s</%s>", tag, value, name, tag));
			}
		}
		return value;
	}

	public static String cleanForSQLInjection(String value) {
		if (value == null) return null;
		value = value.replace("=<>!()+\"", ""); // remove operators
		value = value.replaceAll("'", "''"); // escape single quotes
		return (value.length() == 0)? null: value;
	}

	public static String trimToNull(String value) {
		if (value == null) return null;
		value = value.trim();
		return (value.length() == 0)? null: value;
	}

	public String createFindReachWhereClause(FindReachRequest frReq) {
		String whereClause = "";
		if (frReq.reachIDs != null) {
			String reachIds = frReq.reachIDs.replaceAll("[\\D]+", ",");
			whereClause += " and full_identifier IN (" + reachIds + ")";
		}
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
		{	// bounding box
			if (frReq.boundingBox != null) {
				whereClause += " and SDO_FILTER(reach_geom, SDO_GEOMETRY(2003, 8307, NULL, SDO_ELEM_INFO_ARRAY(1,1003,3), SDO_ORDINATE_ARRAY("
					+ frReq.boundingBox + "))) = 'TRUE' ";
			}
		}
		{	// eda code
			if (frReq.edaCode != null) {
				whereClause += " and EDACODE like '" + frReq.edaCode + "%'";
			}
		}
		{	// eda name
			if (frReq.edaName != null) {
				whereClause += " and EDANAME like '" + frReq.edaName + "%'";
			}
		}
		whereClause += " and rownum <= 1000";
		return whereClause;
	}


}
