package gov.usgswim.sparrow.test;

import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ComparePercentageView;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.predict.PredictParser;
import gov.usgswim.sparrow.service.predict.PredictService;
import gov.usgswim.sparrow.service.predict.PredictServiceRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.print.DocFlavor.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.codehaus.stax2.XMLInputFactory2;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ContextToPredictionTest extends TestCase {

	LifecycleListener lifecycle = new LifecycleListener();
	
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		lifecycle.contextDestroyed(null);
	}
	
	public void testBasicPredictionValues() throws Exception {

		PredictContextRequest contextReq = buildRequest();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline pipe = new PredictContextPipeline();
		pipe.dispatch(contextReq, out);
		
		/*
		System.out.println("***");
		System.out.println("Response: " + out.toString());
		System.out.println("PredictContextID: " + contextReq.getPredictionContext().hashCode());
		System.out.println("***");
		*/
		
		assertTrue(out.toString().contains(new Integer(contextReq.getPredictionContext().hashCode()).toString() ));
		assertTrue(out.toString().contains("753450665"));
	}
	
	public void testHashCode() throws Exception {
		PredictionContext context1 = buildRequest().getPredictionContext();
		PredictionContext context2 = buildRequest().getPredictionContext();
		
		assertEquals(context1.hashCode(), context2.hashCode());
		assertEquals(context1.getId(), context2.getId());
		assertEquals(context1.getId().intValue(), context2.hashCode());
	}
	
	
	public PredictContextRequest buildRequest() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-1.xml");
		String xml = readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	
	public String readToString(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		StringBuffer sb = new StringBuffer();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception ex) {
			ex.getMessage();
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}


}

