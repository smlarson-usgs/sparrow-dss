package gov.usgswim.sparrow.service.watershed;

import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;

import org.junit.Test;
import static org.junit.Assert.*;

import com.meterware.httpunit.*;
import gov.usgswim.sparrow.test.SparrowTestBase;
import org.w3c.dom.Document;

public class WatershedServiceTest extends SparrowServiceTestBaseWithDB {

	@Test
	public void basicTestOfWatershedListingForAModel() throws Exception {
		
		//This is the URL we are requesting
		//Once actually running, the url would be:
		//[host and port]/sparrow/sp_watershed
		WebRequest webRequest = new GetMethodWebRequest("http://localhost:8088/sp_watershed?model-id=50&mime-type=xml");

		WebResponse reportWebResponse = client. sendRequest(webRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		//System.out.println(actualReportResponse);
		Document xmlDoc = SparrowTestBase.getW3cXmlDocumentFromString(actualReportResponse);
		
		String metadataActualColCountStr = getXPathValue("count(//*[local-name()='metadata']//*[local-name()='col'])", xmlDoc); 
		String metadataStatedColCountStr = getXPathValue("//*[local-name()='metadata']/@columnCount", xmlDoc); 
		String metadataStatedRowCountStr = getXPathValue("//*[local-name()='metadata']/@rowCount", xmlDoc); 
		String actualRowCountStr = getXPathValue("count(//*[local-name()='data']//*[local-name()='r'])", xmlDoc);
		
		assertEquals("3", metadataActualColCountStr);
		assertEquals("3", metadataStatedColCountStr);
		assertEquals("16", actualRowCountStr);
		assertEquals("16", metadataStatedRowCountStr);
		
	}
	
	@Test
	public void basicTestOfWatershedListingForAModelAsJSON() throws Exception {
		
		//This is the URL we are requesting
		//Once actually running, the url would be:
		//[host and port]/sparrow/sp_watershed
		WebRequest webRequest = new GetMethodWebRequest("http://localhost:8088/sp_watershed?model-id=50&mime-type=json");

		WebResponse reportWebResponse = client. sendRequest(webRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		//System.out.println(actualReportResponse);
		
		assertTrue(actualReportResponse.contains("\"@rowCount\": \"16\", \"@columnCount\": \"3\""));
		assertTrue(actualReportResponse.contains("{ \"r\": [{ \"@id\": \"325\", \"c\": [\"Altamaha River Watershed\", \"Altamaha R\", \"1\"] }"));
	}
	
	@Test
	public void basicTestOfReachListingForAWatershed() throws Exception {
		
		//This is the URL we are requesting
		WebRequest webRequest = new GetMethodWebRequest("http://localhost:8088/sp_watershed?watershed-id=335&mime-type=xml");

		WebResponse reportWebResponse = client. sendRequest(webRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		//System.out.println(actualReportResponse);
		Document xmlDoc = SparrowTestBase.getW3cXmlDocumentFromString(actualReportResponse);
		
		String metadataActualColCountStr = getXPathValue("count(//*[local-name()='metadata']//*[local-name()='col'])", xmlDoc); 
		String metadataStatedColCountStr = getXPathValue("//*[local-name()='metadata']/@columnCount", xmlDoc); 
		String metadataStatedRowCountStr = getXPathValue("//*[local-name()='metadata']/@rowCount", xmlDoc); 
		String actualRowCountStr = getXPathValue("count(//*[local-name()='data']//*[local-name()='r'])", xmlDoc);
		
		assertEquals("1", metadataActualColCountStr);
		assertEquals("1", metadataStatedColCountStr);
		assertEquals("1", actualRowCountStr);
		assertEquals("1", metadataStatedRowCountStr);
		
	}
	
	@Test
	public void basicTestOfReachListingForAWatershedAsJSON() throws Exception {
		
		//This is the URL we are requesting
		WebRequest webRequest = new GetMethodWebRequest("http://localhost:8088/sp_watershed?watershed-id=335&mime-type=json");

		WebResponse reportWebResponse = client. sendRequest(webRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		//System.out.println(actualReportResponse);
		
		assertTrue(actualReportResponse.contains("\"@rowCount\": \"1\", \"@columnCount\": \"1\""));
		assertTrue(actualReportResponse.contains("\"r\": [{ \"@id\": \"18082\", \"c\": [\"TENNESSEE R\"] }]"));
	}
	
}
