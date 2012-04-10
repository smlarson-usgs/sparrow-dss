package gov.usgswim.sparrow.service.deliveryreport;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;
import gov.usgswim.sparrow.SparrowTestBase;

import org.apache.log4j.Level;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

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
	@Test
	public void model50NoAdjustCVSExportCheckContext() throws Exception {
		String contextRequestText = SparrowTestBase.getXmlAsString(this.getClass(), "context1");
		
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);
		
		WebRequest reportWebRequest = new GetMethodWebRequest(REPORT_SERVICE_URL);
		reportWebRequest.setParameter("context-id", Integer.toString(id));
		reportWebRequest.setParameter("mime-type", "html");
		WebResponse reportWebResponse = client.sendRequest(reportWebRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
	}
	

	
}
