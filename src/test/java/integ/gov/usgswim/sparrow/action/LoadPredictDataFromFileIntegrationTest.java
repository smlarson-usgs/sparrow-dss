package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowDBTest;

import org.junit.Test;

public class LoadPredictDataFromFileIntegrationTest extends SparrowDBTest {

	static PredictData dbPredictData;
	static PredictData filePredictData;
	
	@Override
	public void doSetup() throws Exception {
		
		//Normally we would load via the SharedApplication, which uses the cache.
		//However, since we want to ensure we are comparing db values to the text
		//file value, we call the Actions directly.
		LoadModelPredictData loadFromDb = new LoadModelPredictData(TEST_MODEL_ID);
		LoadModelPredictDataFromFile loadFromFile = new LoadModelPredictDataFromFile(TEST_MODEL_ID);
		dbPredictData = loadFromDb.doAction();
		filePredictData = loadFromFile.doAction();
	}
	
	@Test
	public void testTopo() throws Exception {
		DataTable db = dbPredictData.getTopo();
		DataTable file = filePredictData.getTopo();
		
		//compare, skipping column zero, which has db row ids.
		assertTrue(compareTables(db, file, new int[] {0}, true, 0d));
	}
	
	@Test
	public void testCoef() throws Exception {
		DataTable db = dbPredictData.getCoef();
		DataTable file = filePredictData.getCoef();
		assertTrue(compareTables(db, file));
	}
	
	@Test
	public void testDelivery() throws Exception {
		DataTable db = dbPredictData.getDelivery();
		DataTable file = filePredictData.getDelivery();
		
		//The text file version has row IDs, but we don't care.
		assertTrue(compareTables(db, file, null, false, 0d));
	}
	
	@Test
	public void testSrc() throws Exception {
		DataTable db = dbPredictData.getSrc();
		DataTable file = filePredictData.getSrc();
		assertTrue(compareTables(db, file));
	}

	@Test
	public void testSrcMetadata() throws Exception {
		DataTable db = dbPredictData.getSrcMetadata();
		DataTable file = filePredictData.getSrcMetadata();
		
		//Ignore column 0, which is the db id column
		assertTrue(compareTables(db, file, new int[]{0}, true, 0d));
	}

}
