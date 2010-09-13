package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.domain.SparrowModel;

import java.util.List;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.util.DataLoader loadModelsMetaData methods.
 * 
 * This test validates some of the data returned by the action, but does not
 * fully verify that the correct models are returned based on the specified
 * criteria.
 * TODO:  Clearly, to check that we get the right models back.
 * 
 * @author eeverman
 */
public class LoadModelMetadataTest extends SparrowDBTest {

	/**
	 * Loads all public models (1) from the test db.
	 * @throws Exception
	 */
	@Test
	public void testLoadPublicModels() throws Exception {
		
		List<SparrowModel> models = new LoadModelMetadata().run();
		
		SparrowModel m = models.get(0);
		checkTestModel(m);

	}
	
	/**
	 * This test currently returns the same single model as the All test.
	 * To really finish this, we should add a non-public test model to the db,
	 * as well as an archived and non-approved example.  Could probably skip
	 * the sources for those in the db.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLoadNonPublicModels() throws Exception {
		
		List<SparrowModel> models = new LoadModelMetadata(true, false, false, true).run();
		
		SparrowModel m = models.get(0);
		checkTestModel(m);

	}
	
	@Test
	public void testLoadSpecificModel() throws Exception {
		List<SparrowModel> models = new LoadModelMetadata(TEST_MODEL_ID, true).run();
		
		assertEquals(1, models.size());
		
		SparrowModel model = models.get(0);
		
		checkTestModel(model);
	}
	
	/**
	 * Check the standard test model.
	 * @param model
	 */
	public void checkTestModel(SparrowModel model) {
		Source s1 = model.getSource(1);	//get by identifier
		Source s2 = model.getSource(5);	//get by identifier

		//test that we get the first and last sources
		assertEquals(s1, model.getSources().get(0));	//get via list index
		assertEquals(s2, model.getSources().get(4));	//get via list index


		//model data
		assertEquals(TEST_MODEL_ID.longValue(), model.getId().longValue());
		assertEquals("MRB02 Nitrogen", model.getName());
		assertEquals("Nitrogen", model.getConstituent());
		assertEquals("kg/year", model.getUnits());
		assertEquals(5, model.getSources().size());

		//1st source
		assertEquals(1, s1.getId().intValue());
		assertEquals("kgn_02", s1.getName());
		assertEquals(1, s1.getSortOrder());
		assertEquals(TEST_MODEL_ID.intValue(), s1.getModelId().intValue());
		assertEquals(1, s1.getIdentifier());

		//last source
		assertEquals(5, s2.getId().intValue());
		assertEquals("man02_01nlcd", s2.getName());
		assertEquals(5, s2.getSortOrder());
		assertEquals(TEST_MODEL_ID.intValue(), s2.getModelId().intValue());
		assertEquals(5, s2.getIdentifier());
	}
	
}

