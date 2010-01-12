package gov.usgswim.sparrow.revised;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.*;
import gov.usgswim.sparrow.revised.SparrowRequestProcessor.DataSeriesTypes;
import gov.usgswim.sparrow.revised.request.SparrowRequest;

import org.junit.Test;


public class SparrowRequestProcessorTest {
	public static final int TEST_MODEL_ID = -1;

	@Test
	public void testLoadSource() {
		SparrowRequest sourceRequest= new SparrowRequest(DataSeriesTypes.SOURCE, TEST_MODEL_ID, "Atm. deposition");

		SparrowRequestProcessor processor = new SparrowRequestProcessor();
		CalculationResult result = processor.process(sourceRequest);

		{
			assertNotNull(result);
			assertNotNull("Currently, predictData may be non-null for a SOURCE request. May change in the future", result.predictData);
			assertNull(result.column);
			assertNull(result.table);
			assertNull(result.weights);
			assertNotNull(result.nsDataSet);
			assertNull(result.prevResult);
		}
		assertTrue("There better be some results coming back", result.nsDataSet.size() > 0);
		assertEquals("The NSDataSet transformed result should have the same number of rows as the original",
				result.predictData.getSrc().getRowCount(), result.nsDataSet.size());
	}
}
