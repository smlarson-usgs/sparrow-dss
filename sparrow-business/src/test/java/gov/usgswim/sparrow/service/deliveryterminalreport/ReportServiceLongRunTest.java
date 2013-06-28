package gov.usgswim.sparrow.service.deliveryterminalreport;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;
import gov.usgswim.sparrow.SparrowTestBase;

import org.junit.Test;
import static org.junit.Assert.*;

import com.meterware.httpunit.*;
import static gov.usgswim.sparrow.SparrowTestBase.getXPathValue;
import org.junit.Ignore;

public class ReportServiceLongRunTest extends SparrowServiceTestBaseWithDB {

	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String REPORT_SERVICE_URL = "http://localhost:8088/sp_delivery_terminalreport";

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
	}

	/**
	 * Values containing commas should be escaped
	 * @throws Exception
	 */
	@Test
	public void model50NoAdjustHTMLExportCheckContextTerminalReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");

		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();

		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);

		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml_table");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ID_SCRIPT, "false");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ZERO_TOTAL_ROWS, "true");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_TYPE, ReportRequest.ReportType.terminal.toString());
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_YIELD, "false");

		WebResponse reportWebResponse = client.sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();

//		System.out.println(actualReportResponse);

		String rowCountStr = gov.usgswim.sparrow.service.deliveryaggreport.ReportServiceLongRunTest.getXPathValue("count(//tbody/tr)", actualReportResponse);

		assertEquals("2", rowCountStr);

		String firstTerminalReachName = getXPathValue("//tbody/tr[th[a=9682]]/td[1]", actualReportResponse);
		String totalValue = getXPathValue("//tbody/tr[2]/td[.=40735550]", actualReportResponse);

		assertEquals("MOBILE R", firstTerminalReachName);
		assertEquals("40735550", totalValue);


	}

	@Test
	public void model50NoAdjustXMLExportCheckContextTerminalReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");

		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();

		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);

		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xml");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ID_SCRIPT, "false");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ZERO_TOTAL_ROWS, "true");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_TYPE, ReportRequest.ReportType.terminal.toString());
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_YIELD, "false");

		WebResponse reportWebResponse = client.sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();

//		System.out.println(actualReportResponse);

		String firstReachId = getXPathValue("//*[local-name()='data']/*[local-name()='r'][position()=1]/@id", actualReportResponse);
		String numberOfValues = getXPathValue("count(//*[local-name()='data']/*[local-name()='r'][position()=1]/*[local-name()='c'])", actualReportResponse);
		String declairedColCount = getXPathValue("//*[local-name()='metadata']/@columnCount", actualReportResponse);
		String firstGroupColCount = getXPathValue("//*[local-name()='group'][1]/@count", actualReportResponse);
		String secondGroupColCount = getXPathValue("//*[local-name()='group'][2]/@count", actualReportResponse);
		String declairedRowCount = getXPathValue("//*[local-name()='metadata']/@rowCount", actualReportResponse);

		assertEquals("9682", firstReachId);
		assertEquals("12", numberOfValues);
		assertEquals("12" ,declairedColCount);
		assertEquals("6", firstGroupColCount);
		assertEquals("6", secondGroupColCount);
		assertEquals("2" ,declairedRowCount);

	}

	@Test
	public void model50NoAdjustCSVExportCheckContextTerminalReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");

		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();

		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);

		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "csv");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ID_SCRIPT, "false");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ZERO_TOTAL_ROWS, "true");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_TYPE, ReportRequest.ReportType.terminal.toString());
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_YIELD, "false");

		WebResponse reportWebResponse = client.sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();

		//System.out.println(actualReportResponse);

	}



}
