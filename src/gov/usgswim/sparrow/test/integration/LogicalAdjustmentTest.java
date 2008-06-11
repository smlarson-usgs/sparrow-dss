package gov.usgswim.sparrow.test.integration;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ReachGroup;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.parsers.PredictionContextTest;
import gov.usgswim.sparrow.test.parsers.ReachGroupTest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


import junit.framework.TestCase;

public class LogicalAdjustmentTest extends TestCase {

	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	LifecycleListener lifecycle = new LifecycleListener();

	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null, true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		lifecycle.contextDestroyed(null, true);
	}

	public void testReachGroupLoading() throws XMLStreamException, XMLParseValidationException {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(ReachGroupTest.testRequest1));
		ReachGroup rg = new ReachGroup(1);
		reader.next();
		rg.parse(reader);

		// huc 8
		List<Long> reaches8 = rg.getLogicalReachIDs(0);
		assertTrue(reaches8 != null);
		assertTrue(reaches8.size() > 0);

		// huc6
		List<Long> reaches6 = rg.getLogicalReachIDs(1);
		assertTrue(reaches6 != null);
		assertTrue(reaches6.size() > 0);

		// huc4
		List<Long> reaches4 = rg.getLogicalReachIDs(1);
		assertTrue(reaches4 != null);
		assertTrue(reaches4.size() > 0);

		// huc2
		List<Long> reaches2 = rg.getLogicalReachIDs(1);
		assertTrue(reaches2 != null);
		assertTrue(reaches2.size() > 0);

	}

	public void testHuc4_6_8Adjustment() throws Exception {

		PredictContextRequest contextReq = buildPredictContext5();	//Build a context from a canned file

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline pipe = new PredictContextPipeline();
		pipe.dispatch(contextReq, out);
		StringBuilder sb = new StringBuilder(out.toString());

		int CONTEXT_ID = PredictionContextTest.extractContextIDFromPredictionContextResponse(sb.toString());

		//Get the prediction context and the nominal context;
		PredictionContext userContext = SharedApplication.getInstance().getPredictionContext(CONTEXT_ID);
		PredictionContext nomContext = new PredictionContext(userContext.getModelID(), null, null, null, null);

		//Do a test of the hashcodes
		assertEquals(contextReq.getPredictionContext().hashCode(), userContext.hashCode());
		assertEquals(contextReq.getPredictionContext().hashCode(), userContext.clone().hashCode());
		assertEquals(CONTEXT_ID, contextReq.getPredictionContext().hashCode());

		//Get source values, both adjusted and nominal
		PredictData nomData = SharedApplication.getInstance().getPredictData(userContext.getModelID());
		DataTable nomSrc = nomData.getSrc();
		DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(userContext.getAdjustmentGroups());

		//Constants for tests
		//Reaches in Group1: huc6
		int RCH_ROW_21580 = nomData.getRowForReachID(21580);
		int RCH_ROW_21581 = nomData.getRowForReachID(21581);
		int RCH_ROW_21578 = nomData.getRowForReachID(21578);
		int RCH_ROW_21579 = nomData.getRowForReachID(21579);
		//Reaches in Group2: huc4 
		int RCH_ROW_1638 = nomData.getRowForReachID(1638);
		int RCH_ROW_2157 = nomData.getRowForReachID(2157);
		int RCH_ROW_1655 = nomData.getRowForReachID(1655);
		int RCH_ROW_65179 = nomData.getRowForReachID(65179);
		//Reaches in Group2: huc2
		int RCH_ROW_38707 = nomData.getRowForReachID(38707);
		int RCH_ROW_38711 = nomData.getRowForReachID(38711);
		int RCH_ROW_38533 = nomData.getRowForReachID(38533);
		int RCH_ROW_38666 = nomData.getRowForReachID(38666);

		int SRC_COL_1 = nomData.getSourceIndexForSourceID(1);
		int SRC_COL_2 = nomData.getSourceIndexForSourceID(2);
		int SRC_COL_3 = nomData.getSourceIndexForSourceID(3);
		int SRC_COL_4 = nomData.getSourceIndexForSourceID(4);
		int SRC_COL_5 = nomData.getSourceIndexForSourceID(5);

		{
			//Test Group1
			double SRC_COEF_4 = .8D;
			double SRC_COEF_5 = .6D;
			// test first row item
			assertEquals(nomSrc.getDouble(RCH_ROW_21580, SRC_COL_1), adjSrc.getDouble(RCH_ROW_21580, SRC_COL_1));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_21580, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_21580, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_21580, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_21580, SRC_COL_5));
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_21581, SRC_COL_1), adjSrc.getDouble(RCH_ROW_21581, SRC_COL_1));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_21581, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_21581, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_21581, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_21581, SRC_COL_5));
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_21578, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_21578, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_21578, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_21578, SRC_COL_5));
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_21579, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_21579, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_21579, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_21579, SRC_COL_5));
		}

		{
			//Test Group2: huc4
			double SRC_COEF_1 = 2D;
			// test all the columns of the first row item
			assertEquals(nomSrc.getDouble(RCH_ROW_1638, SRC_COL_2), adjSrc.getDouble(RCH_ROW_1638, SRC_COL_2));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_1638, SRC_COL_3), adjSrc.getDouble(RCH_ROW_1638, SRC_COL_3));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_1638, SRC_COL_4), adjSrc.getDouble(RCH_ROW_1638, SRC_COL_4));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_1638, SRC_COL_5), adjSrc.getDouble(RCH_ROW_1638, SRC_COL_5));	//not adjusted

			// 1st logical set
			assertEquals(nomSrc.getDouble(RCH_ROW_1638, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_1638, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_2157, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_2157, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_1655, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_1655, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_65179, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_65179, SRC_COL_1));
			//2nd logical set
			assertEquals(nomSrc.getDouble(RCH_ROW_38707, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_38707, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_38711, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_38711, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_38533, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_38533, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_38666, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_38666, SRC_COL_1));
		}


	}



	public void testBasicPredictionValues() throws Exception {

		int CONTEXT_ID = -1504305838;

		PredictContextRequest contextReq = buildPredictContext4();	//Build a context from a canned file

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PredictContextPipeline pipe = new PredictContextPipeline();
		pipe.dispatch(contextReq, out);

		System.out.println("***");
		System.out.println("Response: " + out.toString());
		System.out.println("PredictContextID: " + contextReq.getPredictionContext().hashCode());
		System.out.println("***");

		//Confirm that the response xml doc contains the correct context-id,
		//which is and must be repeatable, so as long as the request doesn't change, this number is fixed.
		assertTrue(
				"this will likely fail initially.  Update context id to reflect new parsing.",
				out.toString().contains( Integer.toString(CONTEXT_ID) ));


		//Get the prediction context and the nominal context;
		PredictionContext userContext = SharedApplication.getInstance().getPredictionContext(CONTEXT_ID);
		PredictionContext nomContext = new PredictionContext(userContext.getModelID(), null, null, null, null);

		//Do a test of the hashcodes
		assertEquals(contextReq.getPredictionContext().hashCode(), userContext.hashCode());
		assertEquals(contextReq.getPredictionContext().hashCode(), userContext.clone().hashCode());
		assertEquals(CONTEXT_ID, contextReq.getPredictionContext().hashCode());

		//Get source values, both adjusted and nominal
		PredictData nomData = SharedApplication.getInstance().getPredictData(userContext.getModelID());
		DataTable nomSrc = nomData.getSrc();
		DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(userContext.getAdjustmentGroups());

		//Constants for tests
		//Reaches in Group1
		int RCH_ROW_36347 = nomData.getRowForReachID(36347);
		int RCH_ROW_36346 = nomData.getRowForReachID(36346);
		int RCH_ROW_36344 = nomData.getRowForReachID(36344);
		int RCH_ROW_36345 = nomData.getRowForReachID(36345);
		//Reaches in Group2, logical set 1
		int RCH_ROW_39529 = nomData.getRowForReachID(39529);
		int RCH_ROW_39526 = nomData.getRowForReachID(39526);
		int RCH_ROW_39528 = nomData.getRowForReachID(39528);
		int RCH_ROW_39527 = nomData.getRowForReachID(39527);
		//Reaches in Group2, logical set 2
		int RCH_ROW_39966 = nomData.getRowForReachID(39966);
		int RCH_ROW_39968 = nomData.getRowForReachID(39968);
		int RCH_ROW_39967 = nomData.getRowForReachID(39967);
		int RCH_ROW_39965 = nomData.getRowForReachID(39965);
		//Reaches in Group2, individually added reaches
		//int RCH_ROW_39529 = nomData.getRowForReachID(39529);	//override adjust - already defined above.
		int RCH_ROW_11861 = nomData.getRowForReachID(11861);	//additional reach for this group
		int RCH_ROW_11878 = nomData.getRowForReachID(11878);	//additional reach for this group

		int SRC_COL_1 = nomData.getSourceIndexForSourceID(1);
		int SRC_COL_2 = nomData.getSourceIndexForSourceID(2);
		int SRC_COL_3 = nomData.getSourceIndexForSourceID(3);
		int SRC_COL_4 = nomData.getSourceIndexForSourceID(4);
		int SRC_COL_5 = nomData.getSourceIndexForSourceID(5);

		{
			//Test Group1
			double SRC_COEF_4 = .75D;
			double SRC_COEF_5 = .5D;
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_36347, SRC_COL_1), adjSrc.getDouble(RCH_ROW_36347, SRC_COL_1));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_36347, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_36347, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_36347, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_36347, SRC_COL_5));
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_36346, SRC_COL_1), adjSrc.getDouble(RCH_ROW_36346, SRC_COL_1));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_36346, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_36346, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_36346, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_36346, SRC_COL_5));
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_36344, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_36344, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_36344, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_36344, SRC_COL_5));
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_36345, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_36345, SRC_COL_4));
			assertEquals(nomSrc.getDouble(RCH_ROW_36345, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_36345, SRC_COL_5));
		}

		{
			//Test Group2
			double SRC_COEF_1 = 2D;
			//Test all sources for the 1st reach, which has an override adjustment
			assertEquals(99d, adjSrc.getDouble(RCH_ROW_39529, SRC_COL_1));	//an override adj
			assertEquals(nomSrc.getDouble(RCH_ROW_39529, SRC_COL_2), adjSrc.getDouble(RCH_ROW_39529, SRC_COL_2));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_39529, SRC_COL_3), adjSrc.getDouble(RCH_ROW_39529, SRC_COL_3));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_39529, SRC_COL_4), adjSrc.getDouble(RCH_ROW_39529, SRC_COL_4));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_39529, SRC_COL_5), adjSrc.getDouble(RCH_ROW_39529, SRC_COL_5));	//not adjusted
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_39526, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_39526, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_39528, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_39528, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_39527, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_39527, SRC_COL_1));
			//2nd logical set
			assertEquals(nomSrc.getDouble(RCH_ROW_39966, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_39966, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_39968, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_39968, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_39967, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_39967, SRC_COL_1));
			assertEquals(nomSrc.getDouble(RCH_ROW_39965, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_39965, SRC_COL_1));
			//Individually added reach
			assertEquals(nomSrc.getDouble(RCH_ROW_11861, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_11861, SRC_COL_1));
			//Individually added reach w/ override of a different source - test all sources
			assertEquals(nomSrc.getDouble(RCH_ROW_11878, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_11878, SRC_COL_1));
			assertEquals(88d, adjSrc.getDouble(RCH_ROW_11878, SRC_COL_2));	//override
			assertEquals(nomSrc.getDouble(RCH_ROW_11878, SRC_COL_3), adjSrc.getDouble(RCH_ROW_11878, SRC_COL_3));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_11878, SRC_COL_4), adjSrc.getDouble(RCH_ROW_11878, SRC_COL_4));	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_11878, SRC_COL_5), adjSrc.getDouble(RCH_ROW_11878, SRC_COL_5));	//not adjusted
		}


	}

	public PredictContextRequest buildPredictContext4() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-4.xml");
		String xml = readToString(is);

		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}

	public PredictContextRequest buildPredictContext5() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-5.xml");
		String xml = readToString(is);

		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}

	public String readToString(InputStream is) {
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			isr = new InputStreamReader(is);
			System.err.println("unable to specify file encoding in input stream reader");
			e.printStackTrace();
		}
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

