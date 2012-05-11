package gov.usgswim.sparrow.service.deliveryreport;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;
import gov.usgswim.sparrow.SparrowTestBase;

import org.apache.log4j.Level;
import org.junit.Test;

import com.meterware.httpunit.*;
import org.junit.Ignore;

public class ReportServiceLongRunTest extends SparrowServiceTestBaseWithDB {

	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String REPORT_SERVICE_URL = "http://localhost:8088/sp_deliveryreport";

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
	}
	
	/**
	 * Values containing commas should be escaped
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void model50NoAdjustCVSExportCheckContextTerminalReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");
		
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);
		
		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ID_SCRIPT, "false");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_TYPE, ReportRequest.ReportType.terminal.toString());

		WebResponse reportWebResponse = client. sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
		
	}
	
	@Test
	public void model50NoAdjustXMLStateReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");
		
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);
		
		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ID_SCRIPT, "false");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_TYPE, ReportRequest.ReportType.state.toString());

		WebResponse reportWebResponse = client. sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
		
	}
	
	@Ignore
	@Test
	public void model50NoAdjustCVSExportCheckContextStateReport() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");
		
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);
		
		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter(ReportRequest.ELEMENT_CONTEXT_ID, Integer.toString(id));
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_INCLUDE_ID_SCRIPT, "false");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REPORT_TYPE, ReportRequest.ReportType.state.toString());

		WebResponse reportWebResponse = client. sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
		
	}
	

	
}
