package gov.usgswim.sparrow.revised;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class CalculationResultTest {
	public static void assertResultComponentsNullness(CalculationResult result, boolean shouldPDataBeNull, boolean shouldColDataBeNull, boolean shouldTableBeNull, boolean shouldWeightsBeNull, boolean shouldNSDataSetBeNull, boolean shouldPrevResultBeNull ) {
		assertNotNull(result);
		
		if (shouldPDataBeNull) {
			assertNull(result.predictData);
		} else {
			assertNotNull(result.predictData);
		}
		if (shouldTableBeNull) {
			assertNull(result.table);
		} else {
			assertNotNull(result.table);
		}
		if (shouldColDataBeNull) {
			assertNull(result.column);
		} else {
			assertNotNull(result.column);
		}
		if (shouldWeightsBeNull) {
			assertNull(result.weights);
		} else {
			assertNotNull(result.weights);
		}
		if (shouldNSDataSetBeNull) {
			assertNull(result.nsDataSet);
		} else {
			assertNotNull(result.nsDataSet);
		}
		if (shouldPrevResultBeNull) {
			assertNull(result.prevResult);
		} else {
			assertNotNull(result.prevResult);
		}
	}
	

	@Test @Ignore
	public void testClear() {
		fail("Not yet implemented");
	}

}
