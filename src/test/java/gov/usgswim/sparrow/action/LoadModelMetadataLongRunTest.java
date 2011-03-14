package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.domain.SparrowModel;

import java.util.List;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.util.DataLoader loadModelsMetaData methods.
 * 
 * This test validates some of the criteria combination available to the action,
 * but is not exhaustive.  It checks that public vs non-public are returned
 * correctly, which is probably most important.
 * 
 * @author eeverman
 */
public class LoadModelMetadataLongRunTest extends SparrowDBTestBaseClass {

	/**
	 * Model 50 is public, model 49 is non-public, so only 50 should be returned.
	 * @throws Exception
	 */
	@Test
	public void testLoadPublicModels() throws Exception {
		
		List<SparrowModel> models = new LoadModelMetadata().run();
		
		assertTrue(models.size() == 1);
		SparrowModel m = models.get(0);
		checkTestModel(m);

	}
	
	/**
	 * Model 50 is public, model 49 is non-public, so BOTH should be returned.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLoadNonPublicModels() throws Exception {
		
		List<SparrowModel> models = new LoadModelMetadata(true, false, false, true).run();
		
		assertTrue(models.size() == 2);
		assertTrue(models.get(0).getId() == 49L);
		assertTrue(models.get(1).getId() == 50L);

		checkTestModel(models.get(1));

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
		assertEquals(SparrowUnits.KG_PER_YEAR, model.getUnits());
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

