package gov.usgs.webservices.framework.formatter;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.SparrowServiceTestWithCannedModel50;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class AbstractFormatterTest extends SparrowServiceTestWithCannedModel50 {

	//Should be prepended on downloaded files to ensure they are marked as
	//being unicode.
	private static final int UNICODE_BOM = 0xFEFF;	//Unicode Byte Order Marker
	
	
	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String EXPORT_SERVICE_URL = "http://localhost:8088/sp_predict";
	
	@Test
	public void ensureExportFileHasUnicodeBOM() throws Exception {

		String requestText = getSharedTestResource("predict-context-no-adj.xml");
		WebRequest request = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
		//System.out.println(actualResponse);
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualResponse);
		Integer contextId = getContextIdFromContext(actualResponse);
		
		String exportReqText = getAnyResourceWithSubstitutions("req1.xml", this.getClass(), "contextId", contextId.toString());
		WebRequest exportRequest = new PostMethodWebRequest(EXPORT_SERVICE_URL);
		exportRequest.setParameter("xmlreq", exportReqText);
		WebResponse exportResponse = client.sendRequest(exportRequest);
		String exportResponseText = exportResponse.getText();
		
		assertEquals(
				"The first character should be the Unicode Byte Order Marker.",
				UNICODE_BOM, exportResponseText.codePointAt(0));
		
//		for (int i=0; i<10; i++) {
//			System.out.println(i + " : " + exportResponseText.charAt(i) + " : " + exportResponseText.codePointAt(i));
//		}
		
		//printToFile(exportResponseText);
	}
	
    public void printToFile(String text) throws Exception {


        File outFile = File.createTempFile("export", ".csv");
        FileOutputStream fos = new FileOutputStream(outFile);

        fos.write(text.getBytes());

        fos.close();
        System.out.println("Result of model request written to: "
                + outFile.getAbsolutePath());

    }

}
