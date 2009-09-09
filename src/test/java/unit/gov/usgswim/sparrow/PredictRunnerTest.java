package gov.usgswim.sparrow;

import static org.junit.Assert.fail;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.util.DataResourceLoader;

import org.junit.Test;


public class PredictRunnerTest {

	public static final int TEST_MODEL = -1;

	@Test
	public void testDoPredict() throws Exception {
		// TODO This test doesn't work yet.
		DataTableWritable src_metadata = DataResourceLoader.loadSourceMetadata(TEST_MODEL);
		DataTableWritable topo = DataResourceLoader.loadTopo(TEST_MODEL);
		DataTableWritable deliveryCoef = DataResourceLoader.loadSourceReachCoef(TEST_MODEL, src_metadata);
		DataTableWritable sourceValues = DataResourceLoader.loadSourceValues(TEST_MODEL, src_metadata, topo);
		DataTableWritable decayCoefficients = DataResourceLoader.loadDecay(TEST_MODEL);

		PredictData predictData = new PredictDataImm(topo, deliveryCoef, sourceValues, null, decayCoefficients,
				null, null);
		PredictRunner runner = new PredictRunner(predictData);

		PredictResultImm results = runner.doPredict();

		fail();
	}

}
