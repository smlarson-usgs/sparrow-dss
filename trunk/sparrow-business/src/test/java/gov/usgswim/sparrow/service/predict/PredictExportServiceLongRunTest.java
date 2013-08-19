package gov.usgswim.sparrow.service.predict;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;

import org.apache.log4j.Level;
import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class PredictExportServiceLongRunTest extends SparrowServiceTestBaseWithDB {

	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String EXPORT_SERVICE_URL = "http://localhost:8088/sp_predict";

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
		String contextRequestText = getSharedTestResource("predict-context-no-adj.xml");
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();

		//log.debug("context: " + actualContextResponse);

		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);

		String exportRequestText = getAnyResourceWithSubstitutions(
				"exportCvsReq.xml",
				this.getClass(),
				new Object[] {"context-id", Integer.toString(id)});

		WebRequest exportWebRequest = new PostMethodWebRequest(EXPORT_SERVICE_URL);
		exportWebRequest.setParameter("xmlreq", exportRequestText);
		WebResponse exportWebResponse = client.sendRequest(exportWebRequest);
		String exportContextResponse = exportWebResponse.getText();


		log.debug("export: " + exportContextResponse);

		//Ensure this stream name has quotes
		assertTrue(exportContextResponse.contains("\"SAVANNAH R, S CHANNEL\""));

	}

	/**
	 * All rows should have the same number of values, even if some are empty due to null values.
	 * @throws Exception
	 */
	@Test
	public void model50NoAdjustXMLExportCheckContext() throws Exception {
		String contextRequestText = getSharedTestResource("predict-context-no-adj.xml");
		WebRequest contextWebRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextWebRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextWebResponse = client.sendRequest(contextWebRequest);
		String actualContextResponse = contextWebResponse.getText();

		//log.debug("context: " + actualContextResponse);

		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualContextResponse);
		int id = getContextIdFromContext(actualContextResponse);

		String exportRequestText = getAnyResourceWithSubstitutions(
				"exportXmlReq.xml",
				this.getClass(),
				new Object[] {"context-id", Integer.toString(id)});

		WebRequest exportWebRequest = new PostMethodWebRequest(EXPORT_SERVICE_URL);
		exportWebRequest.setParameter("xmlreq", exportRequestText);
		WebResponse exportWebResponse = client.sendRequest(exportWebRequest);
		String exportContextResponse = exportWebResponse.getText();


		log.debug("export: " + exportContextResponse);

		String strRowCount = getXPathValue("count(//*[local-name()='r'])", exportContextResponse);
		int rowCount = Integer.parseInt(strRowCount);

		assertEquals(41, rowCount);

		//Each row should contain the specified number of columns, even though some data is null
		for (int i=1; i <= rowCount; i++) {
			String strColCount = getXPathValue("count(//*[local-name()='r' and position() =" + i + "]/*[local-name() = 'c'])", exportContextResponse);
			assertEquals("44", strColCount);
		}
	}


	@Test
	public void model50ExportFromInlineContext1() throws Exception {

		String exportRequestText = getXmlAsString(this.getClass(), "exportCvsReqWContext1");

		WebRequest exportWebRequest = new PostMethodWebRequest(EXPORT_SERVICE_URL);
		exportWebRequest.setParameter("xmlreq", exportRequestText);
		WebResponse exportWebResponse = client.sendRequest(exportWebRequest);
		String exportContextResponse = exportWebResponse.getText();


		log.debug("export: " + exportContextResponse);

		String strRowCount = getXPathValue("count(//*[local-name()='r']/*[local-name() = 'c' and .=100]/../@id)", exportContextResponse);
		int rowCount = Integer.parseInt(strRowCount);

		//Should be exactly two rows w/ a 100 value.
		assertEquals(2, rowCount);

	}


}
