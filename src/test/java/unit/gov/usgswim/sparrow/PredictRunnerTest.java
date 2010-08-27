package gov.usgswim.sparrow;

import static gov.usgswim.sparrow.util.DataResourceLoaderTest.TEST_MODEL;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.action.PredictRunner;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.util.DataResourceLoader;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

import java.util.List;

import org.junit.Test;

public class PredictRunnerTest {

	@Test
	public void testDoPredictWithDataResourceLoader() throws Exception{
		PredictData predictData = DataResourceLoader.loadModelData(TEST_MODEL);

		PredictRunner runner = new PredictRunner(predictData);

		PredictResultImm results = runner.doPredict();
		//DataTableUtils.printDataTable(results, "");

		DataTableWritable expected = DataTableUtils.cloneBaseStructure(results);
		String previousPredictResultsFile = SparrowResourceUtils.getModelResourceFilePath(TEST_MODEL, "simplePredictResult.txt");
		expected = DataTableUtils.fill(expected, previousPredictResultsFile, false, "\t", true);

		List<String> comparisonResults = DataTableUtils.compareColumnStructure(expected, results);

		assertTrue("No errors expected", comparisonResults.isEmpty());
		
		
	}

	// TODO check the headings

}
