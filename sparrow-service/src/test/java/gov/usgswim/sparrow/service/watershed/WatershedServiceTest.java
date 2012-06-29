package gov.usgswim.sparrow.service.watershed;

import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;

import org.junit.Test;
import static org.junit.Assert.*;

import com.meterware.httpunit.*;

public class WatershedServiceTest extends SparrowServiceTestBaseWithDB {

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
	}
	
	@Test
	public void basicTestOfWatershedListingForAModel() throws Exception {
		
		//This is the URL we are requesting
		//Once actually running, the url would be:
		//[host and port]/sparrow/sp_watershed
		WebRequest webRequest = new GetMethodWebRequest("http://localhost:8088/sp_watershed?model-id=50&mime-type=xml");

		WebResponse reportWebResponse = client. sendRequest(webRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
		
		String metadataActualColCountStr = getXPathValue("count(//*[local-name()='metadata']//*[local-name()='col'])", actualReportResponse); 
		String metadataStatedColCountStr = getXPathValue("//*[local-name()='metadata']/@columnCount", actualReportResponse); 
		String metadataStatedRowCountStr = getXPathValue("//*[local-name()='metadata']/@rowCount", actualReportResponse); 
		String actualRowCountStr = getXPathValue("count(//*[local-name()='data']//*[local-name()='r'])", actualReportResponse);
		
		assertEquals("3", metadataActualColCountStr);
		assertEquals("3", metadataStatedColCountStr);
		assertEquals("16", actualRowCountStr);
		assertEquals("16", metadataStatedRowCountStr);
		
	}
	
	@Test
	public void basicTestOfReachListingForAWatershed() throws Exception {
		
		//This is the URL we are requesting
		WebRequest webRequest = new GetMethodWebRequest("http://localhost:8088/sp_watershed?watershed-id=335&mime-type=xml");

		WebResponse reportWebResponse = client. sendRequest(webRequest);
		String actualReportResponse = reportWebResponse.getText();
		
		System.out.println(actualReportResponse);
		
		String metadataActualColCountStr = getXPathValue("count(//*[local-name()='metadata']//*[local-name()='col'])", actualReportResponse); 
		String metadataStatedColCountStr = getXPathValue("//*[local-name()='metadata']/@columnCount", actualReportResponse); 
		String metadataStatedRowCountStr = getXPathValue("//*[local-name()='metadata']/@rowCount", actualReportResponse); 
		String actualRowCountStr = getXPathValue("count(//*[local-name()='data']//*[local-name()='r'])", actualReportResponse);
		
		assertEquals("1", metadataActualColCountStr);
		assertEquals("1", metadataStatedColCountStr);
		assertEquals("1", actualRowCountStr);
		assertEquals("1", metadataStatedRowCountStr);
		
	}
	
}
