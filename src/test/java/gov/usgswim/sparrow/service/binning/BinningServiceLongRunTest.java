package gov.usgswim.sparrow.service.binning;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertTrue;

import javax.xml.stream.XMLStreamReader;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.ParserHelper;

import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.GetMethodWebRequest;
import org.apache.log4j.Level;

public class BinningServiceLongRunTest extends SparrowServiceTestBaseWithDBandCannedModel50 {

	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String BINNING_SERVICE_URL = "http://localhost:8088/sp_binning";

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		setLogLevel(Level.DEBUG);
	}
	
	@Test
	public void incrementalDeliveredYieldContext() throws Exception {
		String contextRequestText = getXmlAsString(this.getClass(), "context");
		WebRequest contextRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextResponse = client.sendRequest(contextRequest);
		String contextResponseString = contextResponse.getText();
		log.debug("context actual 1: " + contextResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", contextResponseString);
		
		Integer contextId = getContextIdFromContext(contextResponseString);
		
		
		WebRequest binRequest = new GetMethodWebRequest(BINNING_SERVICE_URL);
		binRequest.setParameter("context-id", contextId.toString());
		binRequest.setParameter("bin-count", "5");
		binRequest.setParameter("bin-type", "EQUAL_RANGE");
		WebResponse binResponse = client.sendRequest(binRequest);
		String binResponseString = binResponse.getText();
		log.debug("bin actual 1: " + binResponseString);
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", binResponseString);
	}
	
	@Test
	public void equalCountBinningForModel50WOAdjustments() throws Exception {

		XMLStreamReader contextReader = getSharedXMLAsReader("predict-context-no-adj.xml");
		ParserHelper.parseToStartTag(contextReader, PredictionContext.MAIN_ELEMENT_NAME);
		PredictionContext pc = PredictionContext.parseStream(contextReader);
		SharedApplication.getInstance().putPredictionContext(pc);
		String id = pc.getId().toString();	//ID of our context
		
		ColumnData cd = pc.getDataColumn().getTable().getColumn(pc.getDataColumn().getColumn());
		log.debug("min double: " + cd.getMinDouble());
		log.debug("max double: " + cd.getMaxDouble());
		
		WebRequest binRequest = new GetMethodWebRequest(BINNING_SERVICE_URL);
		binRequest.setParameter("context-id", id);
		binRequest.setParameter("bin-count", "5");
		binRequest.setParameter("bin-type", "EQUAL_COUNT");
		WebResponse binResponse = client.sendRequest(binRequest);
		String binResponseString = binResponse.getText();
		log.debug("bin actual 1: " + binResponseString);
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", binResponseString);
	}
	
	/**
	 * Concentration uses detection limits, which were bombing.
	 * @throws Exception
	 */
	@Test
	public void concentrationBugTest() throws Exception {

		String contextRequestText = getXmlAsString(this.getClass(), "concentration");
		WebRequest contextRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextResponse = client.sendRequest(contextRequest);
		String contextResponseString = contextResponse.getText();
		log.debug("context actual conc: " + contextResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", contextResponseString);
		
		Integer contextId = getContextIdFromContext(contextResponseString);
		
		WebRequest binRequest = new GetMethodWebRequest(BINNING_SERVICE_URL);
		binRequest.setParameter("context-id", contextId.toString());
		binRequest.setParameter("bin-count", "5");
		binRequest.setParameter("bin-type", "EQUAL_COUNT");
		WebResponse binResponse = client.sendRequest(binRequest);
		String binResponseString = binResponse.getText();
		
		log.debug("bin actual for concentration: " + binResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", binResponseString);
	}
	
	/**
	 * Discovered a bug in which negative comparison values would throw an error.
	 * @throws Exception
	 */
	@Test
	public void comparisonReductionBugTest() throws Exception {

		String contextRequestText = getXmlAsString(this.getClass(), "comp_reduction");
		WebRequest contextRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextResponse = client.sendRequest(contextRequest);
		String contextResponseString = contextResponse.getText();
		log.debug("context actual comp_reduction: " + contextResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", contextResponseString);
		
		Integer contextId = getContextIdFromContext(contextResponseString);
		
		WebRequest binRequest = new GetMethodWebRequest(BINNING_SERVICE_URL);
		binRequest.setParameter("context-id", contextId.toString());
		binRequest.setParameter("bin-count", "5");
		binRequest.setParameter("bin-type", "EQUAL_COUNT");
		WebResponse binResponse = client.sendRequest(binRequest);
		String binResponseString = binResponse.getText();
		
		log.debug("bin actual for comp reduction: " + binResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", binResponseString);
	}
	
	/**
	 * Discovered an exception condition where yeild would result in an infinate
	 * value which could not be binned.
	 * @throws Exception
	 */
	@Test
	public void incrementalDeliveredYieldBugTest() throws Exception {

		String contextRequestText = getXmlAsString(this.getClass(), "inc_del_yield");
		WebRequest contextRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextResponse = client.sendRequest(contextRequest);
		String contextResponseString = contextResponse.getText();
		log.debug("context actual comp_reduction: " + contextResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", contextResponseString);
		
		Integer contextId = getContextIdFromContext(contextResponseString);
		
		WebRequest binRequest = new GetMethodWebRequest(BINNING_SERVICE_URL);
		binRequest.setParameter("context-id", contextId.toString());
		binRequest.setParameter("bin-count", "5");
		binRequest.setParameter("bin-type", "EQUAL_COUNT");
		WebResponse binResponse = client.sendRequest(binRequest);
		String binResponseString = binResponse.getText();
		
		log.debug("bin actual for inc del yeild: " + binResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", binResponseString);
	}
	
	/**
	 * Concentration binning that results in a 500 server error due to the 
	 * SparrowModelImm returning null for the detection limit (correct) but
	 * then trying to use the null detect limit value to determine the max
	 * number of decimal places (wrong).
	 * @throws Exception
	 */
	@Test
	public void concentration2BugTest() throws Exception {

		String contextRequestText = getXmlAsString(this.getClass(), "concentration2");
		WebRequest contextRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextRequest.setParameter("xmlreq", contextRequestText);
		WebResponse contextResponse = client.sendRequest(contextRequest);
		String contextResponseString = contextResponse.getText();
		log.debug("context actual conc: " + contextResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", contextResponseString);
		
		Integer contextId = getContextIdFromContext(contextResponseString);
		
		WebRequest binRequest = new GetMethodWebRequest(BINNING_SERVICE_URL);
		binRequest.setParameter("context-id", contextId.toString());
		binRequest.setParameter("bin-count", "5");
		binRequest.setParameter("bin-type", "EQUAL_COUNT");
		WebResponse binResponse = client.sendRequest(binRequest);
		String binResponseString = binResponse.getText();
		
		log.debug("bin actual for concentration: " + binResponseString);
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", binResponseString);
	}

	
}
