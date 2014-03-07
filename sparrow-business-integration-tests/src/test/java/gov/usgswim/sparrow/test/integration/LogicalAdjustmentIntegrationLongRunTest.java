package gov.usgswim.sparrow.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.ReachGroup;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

public class LogicalAdjustmentIntegrationLongRunTest extends SparrowTestBaseWithDB {

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
		long[] reaches8 = rg.getLogicalReachIDs(0);
		assertTrue(reaches8 != null);
		assertTrue(reaches8.length == 25);
		assertTrue(Arrays.binarySearch(reaches8, 7764L) > -1);	//first
		assertTrue(Arrays.binarySearch(reaches8, 661840L) > -1);	//last
		assertTrue(Arrays.binarySearch(reaches8, 7763L) < 0);	//doesn't contain
		assertTrue(Arrays.binarySearch(reaches8, 661841L) < 0);	//doesn't contain

		// test huc 6 retrieval
		long[] reaches6 = rg.getLogicalReachIDs(1);
		assertTrue(reaches6 != null);
		assertTrue(reaches6.length == 167);
		assertTrue(Arrays.binarySearch(reaches6, 8164L) > -1);	//first
		assertTrue(Arrays.binarySearch(reaches6, 661700L) > -1);	//last

		// test huc 4 retrieval
		long[] reaches4 = rg.getLogicalReachIDs(2);
		assertTrue(reaches4 != null);
		assertTrue(reaches4.length == 454);
		assertTrue(Arrays.binarySearch(reaches4, 8164L) > -1);	//first
		assertTrue(Arrays.binarySearch(reaches4, 661700L) > -1);	//last

		// test huc 2 retrieval
		long[] reaches2 = rg.getLogicalReachIDs(3);
		assertTrue(reaches2 != null);
		assertTrue(reaches2.length == 6850);
		assertTrue(Arrays.binarySearch(reaches2, 4557L) > -1);	//first
		assertTrue(Arrays.binarySearch(reaches2, 664620L) > -1);	//last
		
		// test reaches upstream
		long[] reachesUp = rg.getLogicalReachIDs(4);
		assertEquals(17142, reachesUp[0]);
		assertEquals(17143, reachesUp[1]);
		assertEquals(17144, reachesUp[2]);
		assertEquals(17145, reachesUp[3]);
		assertEquals(17146, reachesUp[4]);
		assertEquals(5, reachesUp.length);
	}

	@Test
	public void testHucAndUpstreamAdjustments() throws Exception {
		String xmlReq = getXmlAsString(this.getClass(), "req2");
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xmlReq);
		

		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		int CONTEXT_ID = Integer.parseInt(SparrowTestBase.getAttributeValue(response, "context-id"));

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
		
		//Reaches in Group3 (huc8 & upstream of 17142)
		//This group overlaps Group2 huc6 above and should accumulate a
		//coef for source 1.
		//huc8 reaches
		int RCH_ROW_8236 = nomData.getRowForReachID(8236);
		int RCH_ROW_8237 = nomData.getRowForReachID(8237);
		int RCH_ROW_8238 = nomData.getRowForReachID(8238);
		int RCH_ROW_8239 = nomData.getRowForReachID(8239);
		//upstream reaches
		int RCH_ROW_17142 = nomData.getRowForReachID(17142);
		int RCH_ROW_17143 = nomData.getRowForReachID(17143);
		int RCH_ROW_17144 = nomData.getRowForReachID(17144);
		int RCH_ROW_17145 = nomData.getRowForReachID(17145);
		int RCH_ROW_17146 = nomData.getRowForReachID(17146);
		
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
			//Test Group1: huc4 0314
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
			//Test Group2: huc8 03110206 and huc6 031401
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
			//Test Group3: huc8 03140104 & upstream of 17142
			//The HUC8 src 1 has a cumulative adjustment (2X2) from group 2
			double HUC8_SRC_COEF_1 = 4D; //2 X 2
			double SRC_COEF_3 = 9D;
			double SRC_COEF_1 = 2D;	//src 1 has no overlay for the upstream reaches
			

			//Source 1 for only 4 of the huc reaches
			assertEquals(nomSrc.getDouble(RCH_ROW_8236, SRC_COL_1) * HUC8_SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8236, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8237, SRC_COL_1) * HUC8_SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8237, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8238, SRC_COL_1) * HUC8_SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8238, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8239, SRC_COL_1) * HUC8_SRC_COEF_1, adjSrc.getDouble(RCH_ROW_8239, SRC_COL_1), VARIANCE);
			
			
			//Source 3 for only 4 of the huc reaches
			assertEquals(nomSrc.getDouble(RCH_ROW_8236, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8236, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8237, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8237, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8238, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8238, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_8239, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_8239, SRC_COL_3), VARIANCE);
		
		
			//All 5 reaches in the upstream set, src 1
			assertEquals(nomSrc.getDouble(RCH_ROW_17142, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_17142, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17143, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_17143, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17144, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_17144, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17145, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_17145, SRC_COL_1), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17146, SRC_COL_1) * SRC_COEF_1, adjSrc.getDouble(RCH_ROW_17146, SRC_COL_1), VARIANCE);
			//All 5 reaches in the upstream set, src 3
			assertEquals(nomSrc.getDouble(RCH_ROW_17142, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_17142, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17143, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_17143, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17144, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_17144, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17145, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_17145, SRC_COL_3), VARIANCE);
			assertEquals(nomSrc.getDouble(RCH_ROW_17146, SRC_COL_3) * SRC_COEF_3, adjSrc.getDouble(RCH_ROW_17146, SRC_COL_3), VARIANCE);

		
		
		}
		
		{
			//Test the two override reaches
			assertEquals(99d, adjSrc.getDouble(RCH_ROW_7775, SRC_COL_1), VARIANCE);
			assertEquals(88d, adjSrc.getDouble(RCH_ROW_8572, SRC_COL_2), VARIANCE);
		}


	}


}

