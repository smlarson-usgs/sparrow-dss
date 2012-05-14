package gov.usgswim.sparrow.service.deliveryaggreport;

import gov.usgswim.sparrow.service.deliveryterminalreport.ReportRequest;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;
import gov.usgswim.sparrow.SparrowTestBase;

import org.junit.Test;

import com.meterware.httpunit.*;

public class ReportServiceLongRunTest extends SparrowServiceTestBaseWithDB {

	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String REPORT_SERVICE_URL = "http://localhost:8088/sp_delivery_aggreport";

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
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
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml_table");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REGION_TYPE, ReportRequest.RegionType.state.name());

		WebResponse reportWebResponse = client. sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
		
	}
	
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
		reportWebRequest.setParameter(ReportRequest.ELEMENT_MIME_TYPE, "xhtml_table");
		reportWebRequest.setParameter(ReportRequest.ELEMENT_REGION_TYPE, ReportRequest.RegionType.state.name());

		WebResponse reportWebResponse = client. sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
		
	}
	

	
}
