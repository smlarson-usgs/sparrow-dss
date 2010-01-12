package gov.usgswim.sparrow.revised.engine;

import static gov.usgswim.sparrow.revised.ProductTypes.PREDICT_DATA;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.revised.CalculationResult;
import gov.usgswim.sparrow.revised.SparrowContext;
import gov.usgswim.sparrow.revised.engine.ComputationEngineBase;

import org.junit.Test;

public class ComputationEngineBaseTest {
	int TEST_MODEL_ID = -1;

	@Test
	public void testLoad_PREDICT_DATA() {
		SparrowContext context = new SparrowContext(TEST_MODEL_ID);

		ComputationEngineBase engine = new ComputationEngineBase(context);

		CalculationResult result = engine.load(PREDICT_DATA.get());
		PredictData pData = null;
		//CalculationResultTest.assertResultComponentsNullness(result, false, true, true, true, true, true);
		{ // simple assertions
			assertNotNull(result);
			assertNotNull(result.predictData);
			assertNull(result.column);
			assertNull(result.table);
			assertNull(result.weights);
			assertNull(result.nsDataSet);
			assertNull(result.prevResult);
		}
	}

	
	@Test
	public void testLoad_PREDICT_DATA_TOPO() {
		SparrowContext context = new SparrowContext(TEST_MODEL_ID);

		ComputationEngineBase engine = new ComputationEngineBase(context);

		CalculationResult result = engine.load(PREDICT_DATA.get("topo"));
		PredictData pData = null;
		//CalculationResultTest.assertResultComponentsNullness(result, false, true, true, true, true, true);
		{ // simple assertions
			assertNotNull(result);
			assertNotNull(result.predictData);
			assertNull(result.column);
			assertNotNull(result.table);
			assertNull(result.weights);
			assertNull(result.nsDataSet);
			assertNull(result.prevResult);
		}
		assertEquals("topo", result.table.getName());
	}
	@Test
	public void testLoad_PREDICT_DATA_COEF() {
		SparrowContext context = new SparrowContext(TEST_MODEL_ID);

		ComputationEngineBase engine = new ComputationEngineBase(context);

		CalculationResult result = engine.load(PREDICT_DATA.get("coef"));
		PredictData pData = null;
		//CalculationResultTest.assertResultComponentsNullness(result, false, true, true, true, true, true);
		{ // simple assertions
			assertNotNull(result);
			assertNotNull(result.predictData);
			assertNull(result.column);
			assertNotNull(result.table);
			assertNull(result.weights);
			assertNull(result.nsDataSet);
			assertNull(result.prevResult);
		}
		assertEquals("coef", result.table.getName());
	}
	@Test
	public void testLoad_PREDICT_DATA_SOURCE_METADATA() {
		SparrowContext context = new SparrowContext(TEST_MODEL_ID);

		ComputationEngineBase engine = new ComputationEngineBase(context);

		CalculationResult result = engine.load(PREDICT_DATA.get("sourceMetadata"));
		PredictData pData = null;
		//CalculationResultTest.assertResultComponentsNullness(result, false, true, true, true, true, true);
		{ // simple assertions
			assertNotNull(result);
			assertNotNull(result.predictData);
			assertNull(result.column);
			assertNotNull(result.table);
			assertNull(result.weights);
			assertNull(result.nsDataSet);
			assertNull(result.prevResult);
		}
		assertEquals("sourceMetadata", result.table.getName());
	}
	@Test
	public void testLoad_PREDICT_DATA_DECAY() {
		SparrowContext context = new SparrowContext(TEST_MODEL_ID);

		ComputationEngineBase engine = new ComputationEngineBase(context);

		CalculationResult result = engine.load(PREDICT_DATA.get("decay"));
		PredictData pData = null;
		//CalculationResultTest.assertResultComponentsNullness(result, false, true, true, true, true, true);
		{ // simple assertions
			assertNotNull(result);
			assertNotNull(result.predictData);
			assertNull(result.column);
			assertNotNull(result.table);
			assertNull(result.weights);
			assertNull(result.nsDataSet);
			assertNull(result.prevResult);
		}
		assertEquals("decay", result.table.getName());
	}
	@Test
	public void testLoad_PREDICT_DATA_SOURCE() {
		SparrowContext context = new SparrowContext(TEST_MODEL_ID);

		ComputationEngineBase engine = new ComputationEngineBase(context);

		CalculationResult result = engine.load(PREDICT_DATA.get("source"));
		PredictData pData = null;
		//CalculationResultTest.assertResultComponentsNullness(result, false, true, true, true, true, true);
		{ // simple assertions
			assertNotNull(result);
			assertNotNull(result.predictData);
			assertNull(result.column);
			assertNotNull(result.table);
			assertNull(result.weights);
			assertNull(result.nsDataSet);
			assertNull(result.prevResult);
		}
		assertEquals("source", result.table.getName());
	}
	
	@Test
	public void testCalculate_INCREMENTAL_FLUX() {
		SparrowContext context = new SparrowContext(TEST_MODEL_ID);

		ComputationEngineBase engine = new ComputationEngineBase(context);

		CalculationResult result = engine.load(PREDICT_DATA.get());
		PredictData pData = null;
		//CalculationResultTest.assertResultComponentsNullness(result, false, true, true, true, true, true);
		{ // simple assertions
			assertNotNull(result);
			assertNotNull(result.predictData); 
			assertNull(result.column);
			assertNull(result.table);
			assertNull(result.weights);
			assertNull(result.nsDataSet);
			assertNull(result.prevResult);
		}

	}
}
