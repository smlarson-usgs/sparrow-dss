package gov.usgswim.sparrow.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ReachGroup;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

public class LogicalAdjustmentIntegrationLongRunTest extends SparrowDBTest {

	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	static final double VARIANCE = .00000000001d;
	
	@Override
	public boolean loadModelDataFromFile() {
		return true;
	}

	// ============
	// TEST METHODS
	// ============
	@Test
	public void testReachGroupHUCLoading() throws XMLStreamException, XMLParseValidationException, IOException {
		String xmlReq = getXmlAsString(this.getClass(), "req1");
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(xmlReq));
		ReachGroup rg = new ReachGroup(TEST_MODEL_ID);
		reader.next();
		rg.parse(reader);

		// test huc 8 retrieval
		List<Long> reaches8 = rg.getLogicalReachIDs(0);
		assertTrue(reaches8 != null);
		assertTrue(reaches8.size() > 0);

		// test huc 6 retrieval
		List<Long> reaches6 = rg.getLogicalReachIDs(1);
		assertTrue(reaches6 != null);
		assertTrue(reaches6.size() > 0);

		// test huc 4 retrieval
		List<Long> reaches4 = rg.getLogicalReachIDs(2);
		assertTrue(reaches4 != null);
		assertTrue(reaches4.size() > 0);

		// test huc 2 retrieval
		List<Long> reaches2 = rg.getLogicalReachIDs(3);
		assertTrue(reaches2 != null);
		assertTrue(reaches2.size() > 0);

	}

	@Test
	public void testHuc4_6_8Adjustment() throws Exception {
		String xmlReq = getXmlAsString(this.getClass(), "req2");
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlReq);
		

		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		int CONTEXT_ID = Integer.parseInt(SparrowUnitTest.getAttributeValue(response, "context-id"));

		//Get the prediction context and the nominal context;
		PredictionContext userContext = SharedApplication.getInstance().getPredictionContext(CONTEXT_ID);
		//PredictionContext nomContext = new PredictionContext(userContext.getModelID(), null, null, null, null);

		//Do a test of the hashcodes
		assertEquals(contextReq.getPredictionContext().hashCode(), userContext.clone().hashCode());
		assertEquals(CONTEXT_ID, contextReq.getPredictionContext().hashCode());

		//Get source values, both adjusted and nominal
		PredictData nomData = SharedApplication.getInstance().getPredictData(userContext.getModelID());
		DataTable nomSrc = nomData.getSrc();
		DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(userContext.getAdjustmentGroups());

		//Constants for tests
		//Reaches in Group1: huc4
		int RCH_ROW_8166 = nomData.getRowForReachID(8166);
		int RCH_ROW_8167 = nomData.getRowForReachID(8167);
		int RCH_ROW_8171 = nomData.getRowForReachID(8171);
		int RCH_ROW_8172 = nomData.getRowForReachID(8172);
		
		//Reaches in Group2: huc8
		int RCH_ROW_7764 = nomData.getRowForReachID(7764);
		int RCH_ROW_7765 = nomData.getRowForReachID(7765);
		int RCH_ROW_7766 = nomData.getRowForReachID(7766);
		int RCH_ROW_7767 = nomData.getRowForReachID(7767);
		//Reaches in Group2: huc6
		//Same as group1 huc4 (they overlap)
		
		//Reaches in Group3 (huc8)
		//This group overlaps Group2 huc6 above and should accumulate a
		//coef for source 1.
		int RCH_ROW_8236 = nomData.getRowForReachID(8236);
		int RCH_ROW_8237 = nomData.getRowForReachID(8237);
		int RCH_ROW_8238 = nomData.getRowForReachID(8238);
		int RCH_ROW_8239 = nomData.getRowForReachID(8239);
		
		//This reach is in group2, but has its source 1 overridden to 99.
		int RCH_ROW_7775 = nomData.getRowForReachID(7775);
		
		//This reach is in no other group and had its source 2 overridden to 88
		int RCH_ROW_8572 = nomData.getRowForReachID(8572);
		
		//Reaches not in any Group
		int RCH_ROW_8569 = nomData.getRowForReachID(8569);
		int RCH_ROW_8570 = nomData.getRowForReachID(8570);

		//sources
		int SRC_COL_1 = nomData.getSourceIndexForSourceID(1);
		int SRC_COL_2 = nomData.getSourceIndexForSourceID(2);
		int SRC_COL_3 = nomData.getSourceIndexForSourceID(3);
		int SRC_COL_4 = nomData.getSourceIndexForSourceID(4);
		int SRC_COL_5 = nomData.getSourceIndexForSourceID(5);

		
		{
			//Test reaches that should have no adjustments at all
			
			assertEquals(nomSrc.getDouble(RCH_ROW_8569, SRC_COL_1), adjSrc.getDouble(RCH_ROW_8569, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8569, SRC_COL_2), adjSrc.getDouble(RCH_ROW_8569, SRC_COL_2), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8569, SRC_COL_3), adjSrc.getDouble(RCH_ROW_8569, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8569, SRC_COL_4), adjSrc.getDouble(RCH_ROW_8569, SRC_COL_4), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8569, SRC_COL_5), adjSrc.getDouble(RCH_ROW_8569, SRC_COL_5), VARIANCE);
			
			assertEquals(nomSrc.getDouble(RCH_ROW_8570, SRC_COL_1), adjSrc.getDouble(RCH_ROW_8570, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8570, SRC_COL_2), adjSrc.getDouble(RCH_ROW_8570, SRC_COL_2), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8570, SRC_COL_3), adjSrc.getDouble(RCH_ROW_8570, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8570, SRC_COL_4), adjSrc.getDouble(RCH_ROW_8570, SRC_COL_4), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8570, SRC_COL_5), adjSrc.getDouble(RCH_ROW_8570, SRC_COL_5), VARIANCE);
		}
		
		{
			//Test Group1
			double SRC_COEF_4 = .8D;
			double SRC_COEF_5 = .6D;
			// test first row item
			assertEquals(nomSrc.getDouble(RCH_ROW_8166, SRC_COL_3), adjSrc.getDouble(RCH_ROW_8166, SRC_COL_3), VARIANCE);	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_8166, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_8166, SRC_COL_4), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8166, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_8166, SRC_COL_5), VARIANCE);
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_8167, SRC_COL_3), adjSrc.getDouble(RCH_ROW_8167, SRC_COL_3), VARIANCE);	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_8167, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_8167, SRC_COL_4), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8167, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_8167, SRC_COL_5), VARIANCE);
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_8171, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_8171, SRC_COL_4), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8171, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_8171, SRC_COL_5), VARIANCE);
			//
			assertEquals(nomSrc.getDouble(RCH_ROW_8172, SRC_COL_4) * SRC_COEF_4, adjSrc.getDouble(RCH_ROW_8172, SRC_COL_4), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8172, SRC_COL_5) * SRC_COEF_5, adjSrc.getDouble(RCH_ROW_8172, SRC_COL_5), VARIANCE);

		}

		{
			//Test Group2: huc4
			double SRC_COEF_1 = 2D;
			//non-adjusted vales for first reach
			assertEquals(nomSrc.getDouble(RCH_ROW_7764, SRC_COL_2), adjSrc.getDouble(RCH_ROW_7764, SRC_COL_2), VARIANCE);	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_7764, SRC_COL_3), adjSrc.getDouble(RCH_ROW_7764, SRC_COL_3), VARIANCE);	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_7764, SRC_COL_4), adjSrc.getDouble(RCH_ROW_7764, SRC_COL_4), VARIANCE);	//not adjusted
			assertEquals(nomSrc.getDouble(RCH_ROW_7764, SRC_COL_5), adjSrc.getDouble(RCH_ROW_7764, SRC_COL_5), VARIANCE);	//not adjusted

			// 1st logical set
			assertEquals(nomSrc.getDouble(RCH_ROW_7764, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_7764, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_7765, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_7765, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_7766, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_7766, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_7767, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_7767, SRC_COL_1), VARIANCE);
			//2nd logical set
			assertEquals(nomSrc.getDouble(RCH_ROW_8166, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8166, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8167, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8167, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8171, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8171, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8172, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8172, SRC_COL_1), VARIANCE);
		}
		
		{
			//Test Group3: huc6
			//This group has a cumulative adjustment on source 1
			double SRC_COEF_1 = 4D; //2 X 2
			double SRC_COEF_3 = 9D;

			//Source 1 for all reaches
			assertEquals(nomSrc.getDouble(RCH_ROW_8236, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8236, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8237, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8237, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8238, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8238, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8239, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8239, SRC_COL_1), VARIANCE);
			
			
			//Source 3 for all reaches
			assertEquals(nomSrc.getDouble(RCH_ROW_8236, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8236, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8237, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8237, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8238, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8238, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8239, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8239, SRC_COL_3), VARIANCE);
		}
		
		{
			//Test the two override reaches
			assertEquals(99d, adjSrc.getDouble(RCH_ROW_7775, SRC_COL_1), VARIANCE);
			assertEquals(88d, adjSrc.getDouble(RCH_ROW_8572, SRC_COL_2), VARIANCE);
		}


	}


}

