package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.*;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelationsBuilder;
import gov.usgswim.sparrow.domain.reacharearelation.ReachAreaRelations;
import gov.usgswim.sparrow.service.SharedApplication;


import org.junit.Test;

/**
 * @author eeverman
 */
public class LoadModelReachAreaRelationsTest extends SparrowTestBaseWithDBandCannedModel50 {

	
	static final double COMP_ERROR = .0000001d;
	
	
	@Test
	public void dataSanityCheck() throws Exception {
		
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		LoadModelReachAreaRelations action = new LoadModelReachAreaRelations(pd);

		ModelReachAreaRelations result = action.run();
		
		assertNotNull(result);
		assertEquals(pd.getTopo().getRowCount(), result.getRowCount());
		assertFalse(result instanceof ModelReachAreaRelationsBuilder);
		
		dumpModelReachAreaRelations(result, result.getRowCount(), true, false, false);
		
		
		//Build some basic stats
		int zeroRelationCnt = 0;
		int singleRelationCnt = 0;
		int doubleRelationCnt = 0;
		int tripleRelationCnt = 0;
		int otherRelationCnt = 0;
		
		for (int row=0; row<result.getRowCount(); row++) {
			ReachAreaRelations rar = result.getRelationsForReachRow(row);
			int cnt = rar.getRelations().size();
			if (cnt == 0) {
				zeroRelationCnt++;
			} else if (cnt == 1) {
				singleRelationCnt++;
			} else if (cnt == 2) {
				doubleRelationCnt++;
			} else if (cnt == 3) {
				tripleRelationCnt++;
			} else {
				otherRelationCnt++;
			}
			
			assertEquals(pd.getIdForRow(row), new Long(rar.getReachId()));
		}
		
		//assertTrue(zeroRelationCnt == 0);
		assertTrue(singleRelationCnt > 0);
		assertTrue(doubleRelationCnt > 0);
		assertTrue(tripleRelationCnt > 0);
		assertTrue(otherRelationCnt == 0);
		
		
	}
	
	@Test
	public void cacheTest() throws Exception {
		
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		ModelReachAreaRelations result = SharedApplication.getInstance().getModelReachAreaRelations(TEST_MODEL_ID);
		
		long startTime = System.currentTimeMillis();
		result = SharedApplication.getInstance().getModelReachAreaRelations(TEST_MODEL_ID);
		long endTime = System.currentTimeMillis();
		double totalSeconds = (endTime - startTime) / 1000d;
		
		//Should be instant
		assertTrue(totalSeconds < 1);
		
		//data should be the same
		assertNotNull(result);
		assertEquals(pd.getTopo().getRowCount(), result.getRowCount());
		assertFalse(result instanceof ModelReachAreaRelationsBuilder);
		
	}
	
	public void dumpModelReachAreaRelations(ModelReachAreaRelations result,
					int rows, boolean printZeros, boolean printOnes, boolean printOthers) {
		for (int row=0; row<rows; row++) {
			ReachAreaRelations rar = result.getRelationsForReachRow(row);
			int relationCnt = rar.getRelations().size();
			
			if (relationCnt == 0 && printZeros) {
				System.out.println("" + rar.getReachId() + ": No relations");
			} else if (relationCnt == 1 && printOnes) {
				System.out.println("" + rar.getReachId() + ": " +
								rar.getRelations().get(0).getAreaId() + ": " + 
								rar.getRelations().get(0).getFraction());
			} else if (printOthers) {
				System.out.print("" + rar.getReachId() + ": ");
				for (int ii=0; ii<relationCnt; ii++) {
				System.out.println("     " +
								rar.getRelations().get(ii).getAreaId() + ": " + 
								rar.getRelations().get(ii).getFraction());
				}
			}

		}
	}
	

	
}

