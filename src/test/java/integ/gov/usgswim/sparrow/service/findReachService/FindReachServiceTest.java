package gov.usgswim.sparrow.service.findReachService;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.SparrowServiceTest;
import gov.usgswim.sparrow.service.ReturnStatus;

import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class FindReachServiceTest extends SparrowServiceTest {

	private static final String FINDREACH_SERVICE_URL = "http://localhost:8088/sp_findReach";


	@Test
	public void FindByMultipleQualifiers() throws IOException, SAXException {
		String requestText = getXmlAsString(this.getClass(), "req1");
		String expectedResponse = getXmlAsString(this.getClass(), "resp1_2");
		WebRequest request = new PostMethodWebRequest(FINDREACH_SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
		//System.out.println(actualResponse);
		
        Diff diff = new Diff(expectedResponse, actualResponse);
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual("Order doesn't matter...", diff, true);
		
		assertEquals("text/xml", response.getContentType());
	}
	
	@Test
	public void FindByFourIDs() throws IOException, SAXException {
		String requestText = getXmlAsString(this.getClass(), "req2");
		String expectedResponse = getXmlAsString(this.getClass(), "resp1_2");
		WebRequest request = new PostMethodWebRequest(FINDREACH_SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
		System.out.println(actualResponse);
		
        Diff diff = new Diff(expectedResponse, actualResponse);
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual("Order doesn't matter...", diff, true);
		
		assertEquals("text/xml", response.getContentType());
	}
	
	/**
	 * This test doesn't compare a response xml document, it just looks for the
	 * partial status flag and counts the actual results.
	 * @throws IOException
	 * @throws SAXException
	 * @throws XpathException
	 */
	@Test
	public void OverTheLimitNumberOfReachesReturned() throws IOException, SAXException, XpathException {
		String requestText = getXmlAsString(this.getClass(), "req3");
		

		WebRequest request = new PostMethodWebRequest(FINDREACH_SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
		//System.out.println(actualResponse);
		
		//These XPath expressions are nasty b/c the xml has a namespace and the
		//XPath processor is apparently XPath 1.0.
		assertXpathEvaluatesTo("50", "/*[local-name()='sparrow-reach-response']/@model-id", actualResponse);
		assertXpathEvaluatesTo("200", "count(//*[local-name()='reach'])", actualResponse);
		assertXpathEvaluatesTo(ReturnStatus.OK_PARTIAL.toString(),
				"/*[local-name()='sparrow-reach-response']/*[local-name()='status']", actualResponse);
	}
	
	/**
	 * This test doesn't compare a response xml document, it just looks for the
	 * partial status flag and counts the actual results.
	 * @throws IOException
	 * @throws SAXException
	 * @throws XpathException
	 */
	@Test
	public void MultipleEDANames() throws IOException, SAXException, XpathException {
		String requestText = getXmlAsString(this.getClass(), "req4");
		
		WebRequest request = new PostMethodWebRequest(FINDREACH_SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
		System.out.println(actualResponse);
		
		//These XPath expressions are nasty b/c the xml has a namespace and the
		//XPath processor is apparently XPath 1.0.
		assertXpathEvaluatesTo("50", "/*[local-name()='sparrow-reach-response']/@model-id", actualResponse);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualResponse);
		assertXpathEvaluatesTo("4909", "(//*[local-name() = 'id' ])[position() = 1]", actualResponse);
		assertXpathEvaluatesTo("4911", "(//*[local-name() = 'id' ])[position() = 2]", actualResponse);
		assertXpathEvaluatesTo("4854", "(//*[local-name() = 'id' ])[position() = 3]", actualResponse);
	}
	
}
