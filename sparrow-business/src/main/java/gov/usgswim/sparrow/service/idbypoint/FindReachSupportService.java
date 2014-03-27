package gov.usgswim.sparrow.service.idbypoint;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.service.ServiceServlet;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgs.cida.sparrow.service.util.ReturnStatus;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
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
 */
public class FindReachSupportService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void getEdaAttribs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/xml");
		String modelID = req.getParameter("model");
		String attrib = req.getParameter("get");
		StringBuilder outputXML = new StringBuilder();
		ReturnStatus status = ReturnStatus.ERROR;
		String message = "";
		String rootElement = "eda" + attrib + "s-response";
		try {
			ColumnData edaAttrib = null;
			Long mId = Long.parseLong(modelID);

			if ("code".equals(attrib)) {
				edaAttrib = (ColumnData) ConfiguredCache.EDACodeColumn.get(mId);
			} else if ("name".equals(attrib)) {
				edaAttrib = (ColumnData) ConfiguredCache.EDANameColumn.get(mId);
			} else {
				throw new Exception("Unrecognized attribute code: " + attrib);
			}


			boolean hasFoundResults = false;
			String openTag = "<" + attrib + ">";
			String closeTag = "</" + attrib + ">";
			for (int r = 0; r < edaAttrib.getRowCount(); r++) {
				hasFoundResults = true;
				outputXML.append(openTag).append(edaAttrib.getString(r)).append(closeTag);
			}
			if (hasFoundResults) {
				status = ReturnStatus.OK;
			} else {
				status = ReturnStatus.ERROR;
				message = "No eda " + attrib + "s found for model " + modelID;
			}
		} catch (Exception e) {
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		getEdaAttribs(req, resp);

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
			frReq.totContributingAreaHi = trimToNull(frReq.totContributingAreaHi);
			frReq.totContributingAreaLo = trimToNull(frReq.totContributingAreaLo);
			frReq.meanQHi = trimToNull(frReq.meanQHi);
			frReq.meanQLo = trimToNull(frReq.meanQLo);
			frReq.reachName = trimToNull(frReq.reachName);
			frReq.huc = trimToNull(frReq.huc);
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
			frReq.totContributingAreaHi = cleanFloat(frReq.totContributingAreaHi, "catch area high", "catchAreaHi", errors);
			frReq.totContributingAreaLo = cleanFloat(frReq.totContributingAreaLo, "catch area low", "basinAreaLo", errors);
			frReq.meanQHi = cleanFloat(frReq.meanQHi, "mean flow high", "meanQHi", errors);
			frReq.meanQLo = cleanFloat(frReq.meanQLo, "mean flow low", "meanQLo", errors);
			if (frReq.isEmptyRequest()) {
				errors.add("at least one search parameter is required");
				return errors;
			}
			frReq.edaCode = cleanForSQLInjection(frReq.edaCode);
			frReq.edaName = cleanForSQLInjection(frReq.edaName);

		}

		errors = checkHiLo(errors, frReq.totContributingAreaHi, frReq.totContributingAreaLo, "catch area");
		errors = checkHiLo(errors, frReq.meanQHi, frReq.meanQLo, "flux");
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
			if (frReq.totContributingAreaHi != null) {
				whereClause += " and CATCH_AREA < " + frReq.totContributingAreaHi;
			}
			if (frReq.totContributingAreaLo != null) {
				whereClause += " and CATCH_AREA > " + frReq.totContributingAreaLo;
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
//		{	// bounding box
//			if (frReq.boundingBox != null) {
//				whereClause += " and SDO_FILTER(reach_geom, SDO_GEOMETRY(2003, 8307, NULL, SDO_ELEM_INFO_ARRAY(1,1003,3), SDO_ORDINATE_ARRAY("
//					+ frReq.boundingBox + "))) = 'TRUE' ";
//			}
//		}
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
